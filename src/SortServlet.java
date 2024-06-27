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
import java.sql.PreparedStatement;

// Declaring a WebServlet called MoviePageServlet, which maps to url "/api/results"
@WebServlet(name = "SortServlet", urlPatterns = "/api/sort")
public class SortServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    // Class queryBuilder holds all the hardcoded SQL queries for searching/browsing and
    // (as the name suggests) can modify them
    private final static QueryBuilder queryBuilder = new QueryBuilder();


    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("SortServlet doPost is a go");

        response.setContentType("application/json");

        String sortIdxFromURL = request.getParameter("sort");
        String nFromURL = request.getParameter("n");

        String pageNumFromURL = request.getParameter("page_number");

        PrintWriter out = response.getWriter();
        JsonObject responseJsonObject = new JsonObject();

        if (sortIdxFromURL != null && nFromURL != null)
        {
            System.out.printf("Got sortIdx: %s + n movies per page: %s\n",
                    sortIdxFromURL, nFromURL);
            // Log what we got
            request.getServletContext().log(String.format("Got sortIdx: %s + n movies per page: %s\n",
                    sortIdxFromURL, nFromURL));

            responseJsonObject.addProperty("sort", sortIdxFromURL);
            responseJsonObject.addProperty("n", nFromURL);

            int currentSortIdx = (int) request.getSession().getAttribute("sort_index");
            int currentNumResultsOnPage = (int) request.getSession().getAttribute("num_results");

            System.out.printf("Current sort is %d and current n is %d\nNew sort is %s and new n is %s\n",
                    currentSortIdx, currentNumResultsOnPage, sortIdxFromURL, nFromURL);

            responseJsonObject.addProperty("different_sorting",
            currentSortIdx != Integer.parseInt(sortIdxFromURL) || currentNumResultsOnPage != Integer.parseInt(nFromURL));
        }

        else
        {
            System.out.println("Got current page number " + pageNumFromURL);

            int pageNumberIncrement = Integer.parseInt(request.getParameter("pageChangeValue"));

            if (pageNumberIncrement == -1)
            {
                if (Integer.parseInt(pageNumFromURL) + pageNumberIncrement <= 0)
                {
                    System.out.println("DECREMNTING PAGE");
                    responseJsonObject.addProperty("valid_page_change", false);
                }
                else
                {
                    responseJsonObject.addProperty("valid_page_change", true);
                }
            }
            else if (pageNumberIncrement == 1)
            {
                System.out.println("Incrementing PAGE");
                boolean onLastPage = (boolean) request.getSession().getAttribute("on_last_page");
                responseJsonObject.addProperty("valid_page_change", !onLastPage);
            }

            responseJsonObject.addProperty("new_page_num", Integer.parseInt(pageNumFromURL) + pageNumberIncrement);
        }

        out.write(responseJsonObject.toString());
        out.close();
    }

}
