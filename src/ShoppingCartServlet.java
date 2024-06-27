import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * This ShoppingCartServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /cart.
 */
@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/cart")
public class ShoppingCartServlet extends HttpServlet {

    /**
     * handles GET requests to store session information
     */

    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    private final String DECREMENT = "decrement";
    private final String INCREMENT = "increment";
    private final String DELETE = "delete";
    private final String ADD = "add";
    private QueryBuilder queryBuilder = new QueryBuilder();

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

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
            session.setAttribute("cart_total_price", 0);
            out.write(jsonArray.toString());
            out.close();
            return;
        }

        int totalPrice = 0;
        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statements
            PreparedStatement moviesStatement = conn.prepareStatement(queryBuilder.getMovieDetailsWithIDQuery());

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

                    totalPrice += movieQty * movie_price;
                    jsonObject.addProperty("movie_id", movie_id);
                    jsonObject.addProperty("movie_title", movie_title);
                    jsonObject.addProperty("movie_year", movie_year);
                    jsonObject.addProperty("quantity_of_movie", movieQty);
                    jsonObject.addProperty("movie_price", movie_price);
                    jsonArray.add(jsonObject);
                }

            }

            System.out.println("Total price is " + totalPrice);
            session.setAttribute("cart_total_price", totalPrice);

            moviesStatement.close();

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

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String id = request.getParameter("currentMovieId");
        System.out.println(id);

        String typeOfChange = request.getParameter("typeOfChange");
        System.out.println(typeOfChange);

        HashMap<String, Integer> currentCartContents = (HashMap<String, Integer>) session.getAttribute("currentCartContents");

        if (currentCartContents == null)
        {
            currentCartContents = new HashMap<>();
            session.setAttribute("currentCartContents", currentCartContents);
        }
        // Should I put a check for if currentCartContents == null? Think about it

        synchronized (currentCartContents) {
            switch (typeOfChange) {
                case DECREMENT:
                    System.out.println("The movie id:" + id + " has this a quantity of " + currentCartContents.get(id) + " before decrement");
                    int movieQuantity = 0;
                    movieQuantity = currentCartContents.get(id) - 1;

                    if (movieQuantity == 0) {
                        currentCartContents.remove(id);
                    } else {
                        currentCartContents.put(id, movieQuantity);
                        System.out.println("The movie id:" + id + " has this a quantity of " + movieQuantity + " after decrement");
                    }
                    break;
                case INCREMENT:
                    System.out.println("The movie id:" + id + " has this a quantity of" + currentCartContents.get(id) + " before increment");

                    currentCartContents.put(id, currentCartContents.get(id) + 1);

                    System.out.println("The movie id:" + id + " has this a quantity of " + currentCartContents.get(id) + " after increment");
                    break;
                case DELETE:
                    currentCartContents.remove(id);
                    if (!currentCartContents.containsKey(id)) {
                        System.out.println("id: " + id + " was deleted from the hashmap");
                    }
                    break;
                case ADD:
                    if (currentCartContents.containsKey(id)) {
                        currentCartContents.put(id, currentCartContents.get(id) + 1);
                    } else {
                        currentCartContents.put(id, 1);
                    }
                    break;
            }
        }
    }
}
