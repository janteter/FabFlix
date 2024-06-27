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
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.Gson;
import java.util.Iterator;
import java.util.Set;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Declaring a WebServlet called MoviePageServlet, which maps to url "/movies"
@WebServlet(name = "AddStar", urlPatterns = "/add-star")
public class AddStar extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbmaster");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private QueryBuilder queryBuilder = new QueryBuilder();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String starName = request.getParameter("star_name");
        int starBirthdayYear = -1;
        try {
            starBirthdayYear = Integer.parseInt(request.getParameter("star_dob"));
        } catch (Exception ignored) {
            // do nothing? should work
        }
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement currentMaxStarID = conn.prepareStatement(queryBuilder.getMaxStarID());
            ResultSet currentMaxStarIdResultSet = currentMaxStarID.executeQuery();

            currentMaxStarIdResultSet.next();
            String currentMaxID = currentMaxStarIdResultSet.getString("maxStarID");

            // NEED TO DO THIS IN THE STORED PROCEDURE
            String newIdforNewStar = newStarID(currentMaxID);

            PreparedStatement insertNewStar = conn.prepareStatement(queryBuilder.getAddStarToDB());

            insertNewStar.setString(1, newIdforNewStar);
            insertNewStar.setString(2, starName);
            if (starBirthdayYear == -1) { // null/empty birth year
                insertNewStar.setNull(3, Types.INTEGER);
            } else {
                insertNewStar.setInt(3, starBirthdayYear);
            }
            insertNewStar.executeUpdate();

            currentMaxStarID.close();
            currentMaxStarIdResultSet.close();
            insertNewStar.close();

            JsonObject newlyAddStarDetails = new JsonObject();
            newlyAddStarDetails.addProperty("valid_checkout", true);
            newlyAddStarDetails.addProperty("new_star_id", newIdforNewStar);
            PrintWriter out = response.getWriter();
            out.write(newlyAddStarDetails.toString());
            out.close();

        }
        catch (Exception e) {
            System.out.println("Error occurred " + e);
            request.getServletContext().log("Error: ", e);
        }
    }

    // CANT DO ( I THINK ) NEED TO DO THIS IN THE STORED PROCEDURE
    private String newStarID(String originalMaxStarID) {

        String alphabeticalPortion = originalMaxStarID.substring(0, 2); //magic, should change somehow
        String numericalPortion = originalMaxStarID.substring(2); //magic, should change somehow

        int i = 0;
        int numberOfZerosInfront = 0;
        char letterZero = '0';
        while(i < numericalPortion.length()) {
            char c = numericalPortion.charAt(i);
            if(c == letterZero) {
                numberOfZerosInfront++;
                i++;
            }
            else { break; }
        }

        Integer numberOfIdOnly = Integer.valueOf(numericalPortion);
        numberOfIdOnly++;
        String newMaxNumberOfIdOnly = String.valueOf(numberOfIdOnly);
        if(numberOfZerosInfront == 0){ return alphabeticalPortion + newMaxNumberOfIdOnly; }

        String additionalZeros = "";
        for(int a = numberOfIdOnly; a > 0; a--){
            additionalZeros += '0';
        }

        return alphabeticalPortion + additionalZeros + newMaxNumberOfIdOnly;
    }

}


