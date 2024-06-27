import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.Gson;

// Declaring a WebServlet called MoviePageServlet, which maps to url "/movies"
@WebServlet(name = "SingleMoviePageServlet", urlPatterns = "/single-movie")
public class SingleMoviePageServlet extends HttpServlet {
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
    private final String getMoveDetailsQuery =
            queryBuilder.getMovieDetailsWithIDQuery();

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id"); //movie id

        System.out.println("id is: " + id);

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Declare our statement
            PreparedStatement movieStatement = conn.prepareStatement(getMoveDetailsQuery);

            String starsQuery = queryBuilder.getGeneralStarsQuery();
            PreparedStatement starsStatement = conn.prepareStatement(starsQuery);

            String genresQuery = queryBuilder.getGeneralGenreQuery();
            PreparedStatement genreStatement = conn.prepareStatement(genresQuery);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            movieStatement.setString(1, id);
            starsStatement.setString(1, id);
            genreStatement.setString(1, id);

            System.out.println("SQL QUERY executing");

            // Perform the query
            ResultSet movieRS = movieStatement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            JsonObject jsonObject = new JsonObject();

            // Iterate through each row of rs
            while (movieRS.next()) {
                String movie_name = movieRS.getString("title");
                int movie_year = movieRS.getInt("year");
                String movie_director = movieRS.getString("director");
                String movie_rating = movieRS.getString("rating");

                if (movie_rating == null)
                {
                    movie_rating = "N/A";
                }

                request.getServletContext().log("getting all info "  +
                        movie_year + " " + movie_director + " " + movie_rating);

                jsonObject.addProperty("movie_name", movie_name);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);
            }

            ResultSet starsRS = starsStatement.executeQuery();

            JsonArray starIds = new JsonArray();
            JsonArray starNames = new JsonArray();

            while (starsRS.next())
            {
                String star_name = starsRS.getString("name");
                String star_id = starsRS.getString("starId");

                starNames.add(star_name);
                starIds.add(star_id);
            }

            ResultSet genreRS = genreStatement.executeQuery();
            JsonArray genreNames = new JsonArray();
            JsonArray genreIds = new JsonArray();

            while (genreRS.next())
            {
                String genre_name = genreRS.getString("genre");
                String genre_id = genreRS.getString("genreId");

                genreNames.add(genre_name);
                genreIds.add(genre_id);
            }

            jsonObject.add("movie_stars", starNames);
            jsonObject.add("movie_stars_ids", starIds);
            jsonObject.add("movie_genres", genreNames);
            jsonObject.add("movie_genres_ids", genreIds);

            jsonArray.add(jsonObject);

            genreRS.close();
            movieStatement.close();
            starsStatement.close();
            genreStatement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            jsonObject.addProperty("errorLine", e.getStackTrace()[0].getLineNumber());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
