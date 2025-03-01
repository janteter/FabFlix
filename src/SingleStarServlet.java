import com.google.gson.JsonArray;
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

// Declaring a WebServlet called MoviePageServlet, which maps to url "/movies"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    QueryBuilder queryBuilder = new QueryBuilder();

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // PLAN: Get the name and birthYear of the star in one query
    // THEN get all the movies that the star has been in, using another query

    // Given the star's id, this query will get the star's name and birth year
    // and all movies they acted in
    // NOTE: birthYear may be NULL, be sure to check for that
    private final String starQuery =
            queryBuilder.getSingleStarSortedMoviesQuery();

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get the star's id
        String starIdParam = request.getParameter("id");
        System.out.println(starIdParam);

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statements
            PreparedStatement starStatement = conn.prepareStatement(this.starQuery);

            // Add our star's id into our statements
            starStatement.setString(1, starIdParam);

            // Make our jsonArray for output
            JsonArray jsonArray = new JsonArray();

            JsonObject outputJson = new JsonObject();
            JsonArray movieTitlesArray = new JsonArray();
            JsonArray movieIdsArray = new JsonArray();

            // Execute the query
            ResultSet rs = starStatement.executeQuery();

            while (rs.next())
            {
                String starId = rs.getString("starId");
                String starName = rs.getString("name");
                String starDob = rs.getString("birthYear");

                // If the star's DOB is null, then replace "null" with "N/A"
                if (rs.wasNull()) {
                    starDob = "N/A";
                }

                String movieId = rs.getString("movieId");
                String movieTitle = rs.getString("title");

                movieIdsArray.add(movieId);
                movieTitlesArray.add(movieTitle);

                outputJson.addProperty("star_id", starId);
                outputJson.addProperty("star_name", starName);
                // HAVE TO CHECK IF DOB IS NULL
                outputJson.addProperty("star_dob", starDob);
            }

            outputJson.add("movie_titles", movieTitlesArray);
            outputJson.add("movie_ids", movieIdsArray);
            jsonArray.add(outputJson);

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
