import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called Top20Servlet, which maps to url "/top20"
@WebServlet(name = "Top20Servlet", urlPatterns = "/top20")
public class Top20Servlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Get the top 20 movies by rating
    private final String getMoviesQuery =
            "SELECT m.id, m.title, m.director, m.year, r.rating " +
                    "FROM movies m, ratings r " +
                    "WHERE m.id = r.movieId " +
                    "ORDER BY r.rating DESC " +
                    "LIMIT 20;";

    // Get the first 3 genres of a movie given its id
    private final String getGenresQuery =
            "SELECT g.name AS genre " +
                    "FROM genres_in_movies gm " +
                    "JOIN genres g ON gm.genreId = g.id " +
                    "WHERE gm.movieId = ? " +
                    "ORDER BY g.name " +
                    "LIMIT 3; ";

    // Get the first 3 stars of a movie given its id
    private final String getStarsQuery =
            "SELECT s.id AS starID, s.name AS starName " +
                    "FROM stars_in_movies sm " +
                    "JOIN stars s on sm.starId = s.id " +
                    "WHERE sm.movieId = ? " +
                    "LIMIT 3;";

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statements
            PreparedStatement moviesStatement = conn.prepareStatement(this.getMoviesQuery);
            PreparedStatement genresStatement = conn.prepareStatement(this.getGenresQuery);
            PreparedStatement starsStatement = conn.prepareStatement(this.getStarsQuery);

            // Get the movies
            ResultSet movies = moviesStatement.executeQuery();

            // Initialize a new jsonArray
            JsonArray jsonArray = new JsonArray();

            // Get all the movie details
            // System.out.println("getting all movie info");
            request.getServletContext().log("getting all movie info");
            while (movies.next())
            {
                String movie_id = movies.getString("id");
                String movie_title = movies.getString("title");
                int movie_year = movies.getInt("year");
                String movie_director = movies.getString("director");
                float movie_rating = movies.getFloat("rating");

                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }

            request.getServletContext().log("looping over movies");
            // System.out.println("looping over movies");
            // Loop through all movies and get the genres and stars
            for (JsonElement movieJson : jsonArray)
            {
                // check if the movieJson is truly an JsonObject or not
                if (movieJson.isJsonObject()) {
                    String movieId = movieJson.getAsJsonObject().get("movie_id").getAsString();

                    JsonArray starIds = new JsonArray();
                    JsonArray starNames = new JsonArray();
                    JsonArray genreNames = new JsonArray();

                    starsStatement.setString(1, movieId);
                    ResultSet starsRS = starsStatement.executeQuery();

                    while (starsRS.next())
                    {
                        String star_name = starsRS.getString("starName");
                        String star_id = starsRS.getString("starID");

                        starNames.add(star_name);
                        starIds.add(star_id);
                    }

                    genresStatement.setString(1, movieId);
                    ResultSet genreRS = genresStatement.executeQuery();

                    while (genreRS.next())
                    {
                        String genre_name = genreRS.getString("genre");

                        genreNames.add(genre_name);
                    }

                    JsonObject movieJsonObj = movieJson.getAsJsonObject();
                    movieJsonObj.add("movie_stars_ids", starIds);
                    movieJsonObj.add("movie_stars", starNames);
                    movieJsonObj.add("movie_genres", genreNames);
                }
            }

            // Close our statements and the movies ResultSet
            movies.close();
            moviesStatement.close();
            genresStatement.close();
            starsStatement.close();

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
            out.write(jsonObject.toString());
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();

        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}