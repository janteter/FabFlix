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
import java.util.StringTokenizer;


@WebServlet(name = "AutoCompleteServlet", urlPatterns = "/api/autocomplete")
public class AutoCompleteServlet extends HttpServlet {
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
        String textInSearchBox = request.getParameter("query");
        PrintWriter out = response.getWriter();
        if (textInSearchBox.strip().length() >= 3) {
            String fulltextSearchQuery = queryBuilder.getFulltextSearchQuery();
            fulltextSearchQuery = QueryBuilder.addSorting(fulltextSearchQuery, 0);
            fulltextSearchQuery = queryBuilder.addLimit(fulltextSearchQuery, 10);
            // System.out.println("autocomplete full text query is " + fulltextSearchQuery);
            StringTokenizer tokenizer = new StringTokenizer(textInSearchBox);
            String patternToInsertIntoQuery = createBooleanSearchPattern(tokenizer);
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement autocompleteStatement = connection.prepareStatement(fulltextSearchQuery);
                autocompleteStatement.setString(1, patternToInsertIntoQuery);
                ResultSet autocompleteResults = autocompleteStatement.executeQuery();

                JsonArray resultsArr = new JsonArray();
                while (autocompleteResults.next()) {
                    String movieId = autocompleteResults.getString("id");
                    String movieYear = autocompleteResults.getString("year");
                    String movieTitle = autocompleteResults.getString("title");
                    JsonObject movieJson = new JsonObject();
                    String valueString = movieTitle + " (" + movieYear + ")";
                    movieJson.addProperty("value", valueString);

                    JsonObject movieIdJsonObject = new JsonObject();
                    movieIdJsonObject.addProperty("movie_id", movieId);
                    movieJson.add("data", movieIdJsonObject);

                    resultsArr.add(movieJson);
                }
                out.write(resultsArr.toString());
                autocompleteResults.close();
                autocompleteStatement.close();
                out.close();
            } catch (Exception e) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage", e.getMessage());
                out.write(jsonObject.toString());
                out.close();
                // Set response status to 500 (Internal Server Error)
                response.setStatus(500);
            }
        } else {
            throw new RuntimeException("Given query " + textInSearchBox + " is not at least length 3!");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

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
}
