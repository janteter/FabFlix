import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

@WebServlet(name = "IndexServlet", urlPatterns = "/api/index")
public class IndexServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    private QueryBuilder queryBuilder = new QueryBuilder();

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonArray genres = new JsonArray();
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String genresQuery = queryBuilder.getAllGenresFromGenreTableQuery();
            PreparedStatement grabAllGenresStatement = conn.prepareStatement(genresQuery);
            ResultSet genresRS = grabAllGenresStatement.executeQuery();

            while (genresRS.next()) {
                JsonObject genreObject = new JsonObject();
                String genreName = genresRS.getString("name");
                String genreId = genresRS.getString("id");

                genreObject.addProperty("genre_name", genreName);
                genreObject.addProperty("genre_id", genreId);
                genres.add(genreObject);
            }

            out.write(genres.toString());
            genresRS.close();
            grabAllGenresStatement.close();
        }
        catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("IndexServlet doPost is a go");
        response.setContentType("application/json");
        JsonObject responseJsonObject = new JsonObject();
        String typeOfSearch = request.getParameter("typeOfSearch");
        PrintWriter out = response.getWriter();
        try {
            if (typeOfSearch.equals("advanced")) {
                String title = request.getParameter("title");
                String year = request.getParameter("year");
                String director = request.getParameter("director");
                String star = request.getParameter("star");

                ArrayList<String> allSearchItems = new ArrayList<>(Arrays.asList(title, year, director, star));

                // Check if at least one of the search terms are not blank
                boolean valid = allSearchItems.stream().anyMatch(str -> !str.isEmpty());

                System.out.printf("Got title: %s + year: %s + director: %s + star: %s\n",
                        title, year, director, star);

                responseJsonObject.addProperty("valid", valid);
                responseJsonObject.addProperty("title", title);
                responseJsonObject.addProperty("year", year);
                responseJsonObject.addProperty("director", director);
                responseJsonObject.addProperty("star", star);
            } else if (typeOfSearch.equals("fulltext")) {
                String fullTextSearchQuery = request.getParameter("fulltext-query");
                // System.out.println("GOT FULLTEXT SEARCH QUERY OF " + fullTextSearchQuery);
                if (fullTextSearchQuery == null || fullTextSearchQuery.isBlank()) {
                    responseJsonObject.addProperty("valid", false);
                    return;
                }
                try {
                    StringTokenizer tokenizer = new StringTokenizer(fullTextSearchQuery);
                    responseJsonObject.addProperty("movie_page_get_url", buildGetMoviePageURL(tokenizer));
                    tokenizer = new StringTokenizer(fullTextSearchQuery);
                    JsonArray tokenArr = new JsonArray();
                    while (tokenizer.hasMoreTokens()) {
                        tokenArr.add(tokenizer.nextToken());
                    }
                    responseJsonObject.add("tokenized_query", tokenArr);
                    responseJsonObject.addProperty("valid", true);
                } catch (Exception e) {
                    System.out.println("Error occurred " + e);
                    responseJsonObject.addProperty("status", "failure");
                    e.printStackTrace();
                }
            }
            out.write(responseJsonObject.toString());
            out.close();
        } catch (Exception e) {
            responseJsonObject.addProperty("Error", e.getMessage());
            out.write(responseJsonObject.toString());
            out.close();
        }
    }

    private String buildGetMoviePageURL(StringTokenizer tokenizer) throws UnsupportedEncodingException {
        String result = "api/results?fulltext=";
        while (tokenizer.hasMoreTokens()) {
            result += tokenizer.nextToken() + "+";
        }
        return result.substring(0, result.length() - 1);
    }
}
