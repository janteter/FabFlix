import jakarta.servlet.annotation.WebServlet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
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
import java.util.HashMap;

@WebServlet(name = "ConfirmServlet", urlPatterns = "/confirm")
public class ConfirmationPageServlet extends HttpServlet {
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

    private QueryBuilder queryBuilder = new QueryBuilder();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        HashMap<String, Integer> currentCartContents = (HashMap<String, Integer>) session.getAttribute("currentCartContents");

        // Initialize a new jsonArray
        JsonArray jsonArray = new JsonArray();

        if (currentCartContents == null || currentCartContents.isEmpty())
        {
            currentCartContents = new HashMap<>();
            session.setAttribute("currentCartContents", currentCartContents);
            session.setAttribute("cart_total_price", 0.00);
            out.write(jsonArray.toString());
            out.close();
            return;
        }

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement moviesStatement = conn.prepareStatement(queryBuilder.getMovieDetailsWithIDQuery());
            PreparedStatement getSaleIdStatment = conn.prepareStatement(queryBuilder.getSaleIdQuery());
            ResultSet saleIdRS = getSaleIdStatment.executeQuery();
            saleIdRS.next();
            String saleId = saleIdRS.getString("saleId");
            jsonArray.add(saleId);

            for (HashMap.Entry<String, Integer> cartContents : currentCartContents.entrySet())
            {
                String movieId = cartContents.getKey();
                int movieQty = cartContents.getValue();

                moviesStatement.clearParameters();
                moviesStatement.setString(1, movieId);
                ResultSet movieDetails = moviesStatement.executeQuery();

                while (movieDetails.next())
                {
                    String movie_id = movieDetails.getString("id");
                    String movie_title = movieDetails.getString("title");
                    int movie_year = movieDetails.getInt("year");
                    int movie_price = movieDetails.getInt("price");

                    JsonObject jsonObject = new JsonObject();

                    jsonObject.addProperty("movie_id", movie_id);
                    jsonObject.addProperty("movie_title", movie_title);
                    jsonObject.addProperty("movie_year", movie_year);
                    jsonObject.addProperty("quantity_of_movie", movieQty);
                    jsonObject.addProperty("movie_price", movie_price);
                    jsonArray.add(jsonObject);
                }
            }

            moviesStatement.close();

            session.setAttribute("currentCartContents", new HashMap<>());
            session.setAttribute("cart_total_price", 0);
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
    }
}
