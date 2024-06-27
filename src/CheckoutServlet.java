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
@WebServlet(name = "CheckoutServlet", urlPatterns = "/checkout")
public class CheckoutServlet extends HttpServlet {

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
    private final String getCustomerInfomationQuery =
            "SELECT cc.id, cc.firstName, cc.lastName, cc.expiration " +
                    "FROM creditcards cc " +
                    "WHERE cc.firstName = ? AND cc.lastName = ? AND cc.id = ? AND cc.expiration = ? ";


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("doGet in CheckoutSErvlet");
        HttpSession session = request.getSession();
        JsonObject cartGetJson = new JsonObject();
        cartGetJson.addProperty("cart_total_price", (Integer) session.getAttribute("cart_total_price"));
        response.getWriter().write(cartGetJson.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String fname = request.getParameter("fname");
        String lname = request.getParameter("lname");
        String credit_info = request.getParameter("credit_info");
        String exp_date = request.getParameter("exp_date");

        JsonObject checkoutDetails = checkIfValidPaymentInfo(fname, lname, credit_info, exp_date);

        if (checkoutDetails.get("failure_message") != null) // if we have a message, then return
        {
            response.getWriter().write(checkoutDetails.toString());
            response.getWriter().close();
            return;
        }

        HashMap<String, Integer> currentCartContents = (HashMap<String, Integer>) session.getAttribute("currentCartContents");

        if (currentCartContents == null || currentCartContents.isEmpty()) {
            currentCartContents = new HashMap<String, Integer>();
            session.setAttribute("currentCartContents", currentCartContents);
            checkoutDetails.addProperty("valid_checkout", false);
            checkoutDetails.addProperty("failure_message",
                    "There is nothing to checkout, please add some movies to your cart");

            response.getWriter().write(checkoutDetails.toString());
            response.getWriter().close();
            return;
        }

        try (Connection conn = dataSource.getConnection()) {

            PreparedStatement paymentStatement = conn.prepareStatement(queryBuilder.getCheckoutGetCreditCardInfoQuery());
            paymentStatement.setString(1, fname);
            paymentStatement.setString(2, lname);
            paymentStatement.setString(3, credit_info);
            paymentStatement.setString(4, exp_date);

            ResultSet paymentResultSet = paymentStatement.executeQuery();

            if (!paymentResultSet.isBeforeFirst()) {
                checkoutDetails.addProperty("valid_checkout", false);
                checkoutDetails.addProperty("failure_message", "Information not found, please try again");
            }
            else {
                checkoutDetails.addProperty("valid_checkout", true);

                PreparedStatement necessaryInputForSalesStatement = conn.prepareStatement(queryBuilder.getNecessaryInputForSalesTable());
                necessaryInputForSalesStatement.setString(1, credit_info);
                ResultSet customerInfoResultSet = necessaryInputForSalesStatement.executeQuery();
                customerInfoResultSet.next();
                int customer_id = customerInfoResultSet.getInt("id");

                Set<String> setOfMovieKeys = currentCartContents.keySet();
                Iterator<String> keysInCartIterator = setOfMovieKeys.iterator();
                PreparedStatement updatingSalesTableStatement = conn.prepareStatement(queryBuilder.getRequestForUpdateSalesTable());
                while (keysInCartIterator.hasNext()) {
                    String keyValue = keysInCartIterator.next(); //can only call .next once after .hasNext()

                    updatingSalesTableStatement.clearParameters();
                    updatingSalesTableStatement.setInt(1, customer_id);
                    updatingSalesTableStatement.setString(2, keyValue);
                    updatingSalesTableStatement.setString(3, paymentDate());
                    updatingSalesTableStatement.setInt(4, currentCartContents.get(keyValue));

                    updatingSalesTableStatement.executeUpdate(); // NEED TO CLOSE!!!!!
                }
                customerInfoResultSet.close();
                updatingSalesTableStatement.close();
                necessaryInputForSalesStatement.close();
            }

            PrintWriter out = response.getWriter();
            out.write(checkoutDetails.toString());
            out.close();
            paymentResultSet.close();
            paymentStatement.close();
        }

        catch (Exception e) {
            System.out.println("Error occurred " + e);
            request.getServletContext().log("Error: ", e);
        }
    }

    private JsonObject checkIfValidPaymentInfo(String fname, String lname, String credit_info, String exp_date) {
        if (fname.isEmpty()) {
            return createFailureJson("Please enter in your first name");
        }

        else if (lname.isEmpty()) {
            return createFailureJson("Please enter in your last name");
        }

        else if (credit_info.isEmpty()) {
            return createFailureJson("Please enter in your credit card information");
        }

        else if (exp_date.isEmpty()) {
            return createFailureJson("Please enter in your expiration date");
        }

        return createSuccessJson();
    }

    private String paymentDate() {
        LocalDate dateObject = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = dateObject.format(formatter);
        return date;
    }

    private JsonObject createFailureJson(String message) {
        JsonObject checkoutJson = new JsonObject();
        checkoutJson.addProperty("valid_checkout", false);
        checkoutJson.addProperty("failure_message", message);
        return checkoutJson;
    }

    private JsonObject createSuccessJson() {
        JsonObject checkoutJson = new JsonObject();
        checkoutJson.addProperty("valid_checkout", true);
        return checkoutJson;
    }

}
