import com.google.gson.JsonArray;
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
import java.util.HashMap;

// Declaring a WebServlet called MoviePageServlet, which maps to url "/movies"
@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends HttpServlet {

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

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();

        JsonArray jsonArray = new JsonArray();


        try (Connection conn = dataSource.getConnection()) {

            PreparedStatement tablesStatement = conn.prepareStatement(queryBuilder.getListOfTables());
            ResultSet tablesDetails = tablesStatement.executeQuery();

            PreparedStatement tableMetadataStatement = conn.prepareStatement(queryBuilder.getTableMetadata());


            while(tablesDetails.next()) {
                tableMetadataStatement.clearParameters();

                String table_name = tablesDetails.getString("Tables_in_moviedb");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("table_name", table_name);

                tableMetadataStatement.setString(1, table_name);
                ResultSet tablesMetadataResultSet = tableMetadataStatement.executeQuery();

                JsonArray fieldArray = new JsonArray();
                JsonArray typeArray = new JsonArray();

                while(tablesMetadataResultSet.next()) {
                    String metadata_field = tablesMetadataResultSet.getString("COLUMN_NAME");
                    String metadata_type = tablesMetadataResultSet.getString("DATA_TYPE");

                    fieldArray.add(metadata_field);
                    typeArray.add(metadata_type);
                }
                jsonObject.add("field_array", fieldArray);
                jsonObject.add("type_array", typeArray);

                jsonArray.add(jsonObject);
                tablesMetadataResultSet.close();
            }
            tablesStatement.close();
            tablesDetails.close();
            tableMetadataStatement.close();


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