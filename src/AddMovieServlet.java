import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
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
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.Gson;
import java.util.Iterator;
import java.util.Set;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Declaring a WebServlet called MoviePageServlet, which maps to url "/movies"
@WebServlet(name = "AddMovieServlet", urlPatterns = "/add-movie")
public class AddMovieServlet extends HttpServlet {

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
        String  movieTitle = request.getParameter("movie_title");
        int movieYear = Integer.parseInt(request.getParameter("movie_year"));
        String  directorName = request.getParameter("director_name");
        String  starName = request.getParameter("star_name");
        int starDob = -1;
        try {
            starDob = Integer.parseInt(request.getParameter("star_dob"));
        }
        catch(Exception ignored){ }
        String  movieGenre = request.getParameter("movie_genre");

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement addNewMovieStatement = conn.prepareStatement(queryBuilder.getAddMovieToDB());

            addNewMovieStatement.setString(1, movieTitle);
            addNewMovieStatement.setInt(2, movieYear);
            addNewMovieStatement.setString(3, directorName);
            addNewMovieStatement.setString(4, starName);
            if (starDob == -1) { // null/empty birth year
                addNewMovieStatement.setNull(5, Types.INTEGER);
            } else {
                addNewMovieStatement.setInt(5, starDob);
            }
            addNewMovieStatement.setString(6, movieGenre);
            ResultSet newMovieResultSet = addNewMovieStatement.executeQuery();
            newMovieResultSet.next();

            JsonObject addedMovieOutput = new JsonObject();
            String error = null;
            String addedMovieId = null;
            String addedStarId = null;
            int addedGenreId = 0;
            try{
                error = newMovieResultSet.getString("error");

            }
            catch (Exception noError){
                addedMovieId = newMovieResultSet.getString("id");
                addedStarId = newMovieResultSet.getString("starID");
                addedGenreId = newMovieResultSet.getInt("genreID");
            }

            if(error != null){
                addedMovieOutput.addProperty("error", true);
            }
            else {
                addedMovieOutput.addProperty("id", addedMovieId);
                addedMovieOutput.addProperty("starID", addedStarId);
                addedMovieOutput.addProperty("genreID", addedGenreId);
                addedMovieOutput.addProperty("error", false);
            }


            PrintWriter out = response.getWriter();
            out.write(addedMovieOutput.toString());
            out.close();
            addNewMovieStatement.close();
            newMovieResultSet.close();
        }
        catch (Exception e) {
            System.out.println("Error occurred " + e);
            request.getServletContext().log("Error: ", e);
        }
    }
}