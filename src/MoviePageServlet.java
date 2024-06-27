import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;


// Declaring a WebServlet called MoviePageServlet, which maps to url "/api/results"
@WebServlet(name = "MoviePageServlet", urlPatterns = "/api/results")
public class MoviePageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    // Class queryBuilder holds all the hardcoded SQL queries for searching/browsing and
    // (as the name suggests) can modify them
    private final static QueryBuilder queryBuilder = new QueryBuilder();

    private final int DEFAULT_PAGE_NUM = 1;
    private final int DEFAULT_SORT_IDX = 0;
    private final int DEFAULT_NUM_RESULTS_PER_PAGE = 50;
    private enum MODE {
        SEARCHING,
        BROWSING_BY_GENRE,
        BROWSING_BY_PREFIX,
        FULLTEXT_SEARCH
    }

    public void init(ServletConfig config) throws ServletException {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        super.init(config);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime = System.nanoTime();
        long jdbcTime = 0;
        System.out.println("MoviePageServlet doGet is a go");

        HttpSession session = request.getSession();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        // When Sorting
        String pageNumParameter = request.getParameter("page");
        String sortParameter = request.getParameter("sort");
        String numResultsParameter = request.getParameter("n");

        // When Searching
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");

        // When Browsing
        // One of these should be null and the other should be valid
        String genre = request.getParameter("genre"); // This should be an integer
        String prefixChar = request.getParameter("prefix"); // Should be length 1 string

        // When doing fulltext search
        String fulltextSearchQuery = request.getParameter("fulltext");

        JsonObject totalResult = new JsonObject();
        JsonArray movie_results = new JsonArray();
        String searchQuery = "";

        // If this condition passes, we are using the search query
        if (title != null || year != null || director != null || star != null) {
            // System.out.printf("SEARCH MODE: %s %s %s %s\n", title, year, director, star);
            // Store all the parameters from the URL
            Map<String, String> searchParams = new LinkedHashMap<>();

            // Want to handle the star parameter first since it adds a complex part to the query
            searchParams.put("star", star);
            searchParams.put("title", title);
            searchParams.put("year", year);
            searchParams.put("director", director);

            searchQuery = queryBuilder.buildSearchQuery(searchParams);

            session.setAttribute("sort_index", DEFAULT_SORT_IDX);
            session.setAttribute("num_results", DEFAULT_NUM_RESULTS_PER_PAGE);
            session.setAttribute("page_number", DEFAULT_PAGE_NUM);
            session.setAttribute("mode", MODE.SEARCHING);
            session.setAttribute("params", searchParams);
        } else if (genre != null || prefixChar != null) { // Then we (should) have clicked on one of the browse links
            System.out.printf("Genre %s ; Prefix Char %s\n", genre, prefixChar);
            HashMap<String, String> browseParams = new HashMap<>();
            browseParams.put("genre", genre);
            browseParams.put("prefix", prefixChar);

            if (genre != null && !genre.isEmpty()) {
                searchQuery = queryBuilder.getBrowseByGenreQuery();
                session.setAttribute("mode", MODE.BROWSING_BY_GENRE);
            } else if (!prefixChar.isEmpty()) {
                searchQuery = queryBuilder.getBrowseByPrefixQuery();
                if (prefixChar.equals("*")) { searchQuery = searchQuery.replace("?", "NOT REGEXP ? "); }
                else { searchQuery = searchQuery.replace("?", "LIKE ? "); }
                session.setAttribute("mode", MODE.BROWSING_BY_PREFIX);
            } else {
                throw new IllegalArgumentException("Both genre and prefixChar are empty. Provide valid input.");
            }

            session.setAttribute("sort_index", DEFAULT_SORT_IDX);
            session.setAttribute("num_results", DEFAULT_NUM_RESULTS_PER_PAGE);
            session.setAttribute("page_number", DEFAULT_PAGE_NUM);
            session.setAttribute("params", browseParams);
        } else if ((sortParameter != null && numResultsParameter != null) || pageNumParameter != null) { // Then we should be in sorting mode
            // System.out.println("SORTING/PAGING MODE ACTIVATE");
            searchQuery = (String) session.getAttribute("previous_search_query");
            if (sortParameter != null && numResultsParameter != null) {
                // System.out.println("SORTING MODE ACTIVATE");
                session.setAttribute("sort_index", Integer.parseInt(sortParameter));
                session.setAttribute("num_results", Integer.parseInt(numResultsParameter));
                session.setAttribute("page_number", DEFAULT_PAGE_NUM);
                // session.setAttribute("mode", MODE.SORTING);
            } else {
                // System.out.println("PAGING MODE ACTIVATE with new page " + pageNumParameter);
                // session.setAttribute("mode", MODE.PAGING);
                session.setAttribute("page_number", Integer.parseInt(pageNumParameter));
            }
        } else if (fulltextSearchQuery != null) { // Fulltext searching
            // System.out.println("FULLTEXT MODE ACTIVATE");
            HashMap<String, String> fulltextParams = new HashMap<>();
            fulltextParams.put("fulltextSearchQuery", fulltextSearchQuery);
            searchQuery = queryBuilder.getFulltextSearchQuery();
            // System.out.println("Fulltext search query is " + fulltextSearchQuery);

            session.setAttribute("sort_index", DEFAULT_SORT_IDX);
            session.setAttribute("num_results", DEFAULT_NUM_RESULTS_PER_PAGE);
            session.setAttribute("page_number", DEFAULT_PAGE_NUM);
            session.setAttribute("mode", MODE.FULLTEXT_SEARCH);
            session.setAttribute("params", fulltextParams);
        } else { // Jumping functionality
            searchQuery = (String) session.getAttribute("previous_search_query");
        }
        // System.out.println("executing query " + searchQuery);
        totalResult.addProperty("page_number", (int) session.getAttribute("page_number"));

        session.setAttribute("previous_search_query", searchQuery);

        int sortingIndex = (int) session.getAttribute("sort_index");
        int numResultsOnPage = (int) session.getAttribute("num_results");
        int pageNumber = (int) session.getAttribute("page_number");

        searchQuery = QueryBuilder.addSorting(searchQuery, sortingIndex);
        // Add limit + 1 to check to see if we are on the last page or not
        searchQuery = queryBuilder.addLimit(searchQuery, numResultsOnPage + 1);
        searchQuery = queryBuilder.addOffset(searchQuery, numResultsOnPage, pageNumber);
        assert !searchQuery.isEmpty(); // Check to make sure we have an actual query

        long jdbcStartTime = System.nanoTime();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement searchStatement = createPreparedStatementForMovieListPage(conn, session, searchQuery);
            PreparedStatement genresStatement = conn.prepareStatement(queryBuilder.buildResultsThreeGenresQuery());
            PreparedStatement starsStatement = conn.prepareStatement(queryBuilder.buildResultsThreeStarsQuery());
            int numberOfResults = getMovieResults(movie_results, searchStatement, numResultsOnPage);
            for (JsonElement movie : movie_results) {
                JsonObject movieJson = movie.getAsJsonObject();

                String movieId = movieJson.get("movie_id").getAsString();

                // Get the genres and stars for each movie
                getGenres(movie, movieId, genresStatement);
                getStars(movie, movieId, starsStatement);
            }
            // System.out.println("ON last page?? " + session.getAttribute("on_last_page"));
            searchStatement.close();
            genresStatement.close();
            starsStatement.close();
            long jdbcEndTime = System.nanoTime();
            jdbcTime = jdbcEndTime - jdbcStartTime;

            if (numberOfResults == numResultsOnPage + 1) {
                session.setAttribute("on_last_page", false);
            } else {
                session.setAttribute("on_last_page", true);
            }
        } catch (Exception e) {
            System.out.println("Error occurred " + e);
            request.getServletContext().log("Error: ", e);
        }

        totalResult.add("results", movie_results);
        out.write(totalResult.toString());
        out.close();
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        writeTimesToFile(elapsedTime, jdbcTime);
    }

    void writeTimesToFile(Long timeTS, Long timeTJ) throws IOException {
        String contextPath = getServletContext().getRealPath("/");
        // System.out.println("Writing timelog.txt to directory " + contextPath);
        File logFile = new File(contextPath + "/timelog.txt");
        // System.out.println("File location " + logFile.getAbsolutePath());
        FileWriter logFileWriter = new FileWriter(logFile, true);
        BufferedWriter logBufferedWriter = new BufferedWriter(logFileWriter);

        try {
            logBufferedWriter.write("TS " + timeTS + " TJ " + timeTJ);
            logBufferedWriter.newLine();
            logBufferedWriter.close();
            // System.out.println("File written to " + logFile.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Could not write log to file!");
            e.printStackTrace();
        }
    }

    int getMovieResults(JsonArray resultsArr, PreparedStatement sqlStatement, int numResultsOnPage) throws SQLException {
        ResultSet resultSet = sqlStatement.executeQuery();
        int numberOfMovies = 0;
        while (resultSet.next()) {
            numberOfMovies += 1;
            // Can exit function as we don't need the n + 1th movie
            // but we can still return to see that we still have another
            // page after this one
            if (numberOfMovies == numResultsOnPage + 1) {
                return numberOfMovies;
            }
            JsonObject movie = new JsonObject();
            String movie_id = resultSet.getString("id");
            String movie_title = resultSet.getString("title");
            String movie_year = resultSet.getString("year");
            String movie_director = resultSet.getString("director");
            String movie_rating = resultSet.getString("rating");
            if (movie_rating == null)
            {
                movie_rating = "N/A";
            }
            movie.addProperty("movie_id", movie_id);
            movie.addProperty("movie_title", movie_title);
            movie.addProperty("movie_year", movie_year);
            movie.addProperty("movie_director", movie_director);
            movie.addProperty("movie_rating", movie_rating);
            resultsArr.add(movie);
        }
        return numberOfMovies;
    }

    private void getGenres(JsonElement jsonElem, String movieId, PreparedStatement sqlStatement) throws SQLException {
        sqlStatement.clearParameters();
        sqlStatement.setString(1, movieId);
        ResultSet genreResultSet = sqlStatement.executeQuery();
        JsonArray genres = new JsonArray();
        JsonArray genreIds = new JsonArray();
        while (genreResultSet.next()) {
            String genre = genreResultSet.getString("genre");
            String genreId = genreResultSet.getString("genreId");

            genres.add(genre);
            genreIds.add(genreId);
        }

        JsonObject jsonObject = jsonElem.getAsJsonObject();
        jsonObject.add("movie_genres", genres);
        jsonObject.add("movie_genres_ids", genreIds);
    }

    private void getStars(JsonElement jsonElem, String movieId, PreparedStatement sqlStatement) throws SQLException {
        sqlStatement.clearParameters();
        sqlStatement.setString(1, movieId);
        ResultSet starsResultSet = sqlStatement.executeQuery();
        JsonArray starNames = new JsonArray();
        JsonArray starsIds = new JsonArray();
        while (starsResultSet.next()) {
            String starName = starsResultSet.getString("name");
            String starId = starsResultSet.getString("starId");
            starNames.add(starName);
            starsIds.add(starId);
        }

        JsonObject jsonObject = jsonElem.getAsJsonObject();
        jsonObject.add("movie_stars", starNames);
        jsonObject.add("movie_stars_ids", starsIds);
    }

    private String createBooleanSearchPattern(StringTokenizer tokenizer) {
        StringBuilder result = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            result.append("+");
            result.append(tokenizer.nextToken());
            result.append("* ");
        }
        return result.toString();
    }

    private PreparedStatement createPreparedStatementForMovieListPage(Connection connection, HttpSession session,
                                                  String queryString) throws SQLException {
        MODE currentMode = (MODE) session.getAttribute("mode");
        PreparedStatement statementToReturn = null;
        switch (currentMode) {
            case SEARCHING: {
                // Order is star, title, year, director
                LinkedHashMap<String, String> searchParams = (LinkedHashMap<String, String>) session.getAttribute("params");
                int paramIdx = 1;
                statementToReturn = connection.prepareStatement(queryString);
                for (Map.Entry<String, String> entry : searchParams.entrySet()) {
                    String paramName = entry.getKey();
                    String paramVal = entry.getValue();
                    if (paramName.equals("star") && !paramVal.isEmpty()) { // star is always first
                        statementToReturn.setString(1, "%" + paramVal + "%");
                        paramIdx += 1;
                    } else if ((paramName.equals("title") || paramName.equals("director")) && !paramVal.isEmpty()) {
                        statementToReturn.setString(paramIdx, "%" + paramVal + "%");
                        paramIdx += 1;
                    } else if (paramName.equals("year") && !paramVal.isEmpty()) {
                        statementToReturn.setString(paramIdx, paramVal);
                        paramIdx += 1;
                    }
                }
                break;
            }
            case BROWSING_BY_PREFIX: {
                HashMap<String, String> browseParams = (HashMap<String, String>) session.getAttribute("params");
                for (Map.Entry<String, String> entry : browseParams.entrySet()) {
                    String paramName = entry.getKey();
                    String paramVal = entry.getValue();
                    if (paramName.equals("prefix") && !paramVal.isEmpty()) {
                        statementToReturn = connection.prepareStatement(queryString);
                        if (paramVal.equals("*")) {
                            statementToReturn.setString(1, "^[a-zA-Z0-9]");
                        }
                        else {
                            statementToReturn.setString(1, paramVal + "%");
                        }
                    }
                }
                break;
            }
            case BROWSING_BY_GENRE: {
                HashMap<String, String> browseParams = (HashMap<String, String>) session.getAttribute("params");
                for (Map.Entry<String, String> entry : browseParams.entrySet()) {
                    String paramName = entry.getKey();
                    String paramVal = entry.getValue();
                    if (paramName.equals("genre") && !paramVal.isEmpty()) {
                        statementToReturn = connection.prepareStatement(queryString);
                        statementToReturn.setString(1, paramVal);
                    }
                }
                break;
            }
            case FULLTEXT_SEARCH: {
                HashMap<String, String> fulltextParams = (HashMap<String, String>) session.getAttribute("params");
                StringTokenizer tokenizer = new StringTokenizer(fulltextParams.get("fulltextSearchQuery"));
                String booleanSearchPattern = createBooleanSearchPattern(tokenizer);
//                System.out.println("Fulltext search query pattern is " + booleanSearchPattern);
                statementToReturn = connection.prepareStatement(queryString);
                statementToReturn.setString(1, booleanSearchPattern);
                break;
            }
        }
        return statementToReturn;
    }
}
