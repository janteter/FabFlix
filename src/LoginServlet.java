import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.servlet.http.HttpSession;
import org.jasypt.util.password.StrongPasswordEncryptor;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import com.google.gson.JsonObject;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    QueryBuilder queryBuilder = new QueryBuilder();

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("LoginServlet doPost is a go");

        String typeOfLogin = request.getParameter("type");

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        PrintWriter out = response.getWriter();
        JsonObject responseJsonObject = new JsonObject();
        HttpSession session = request.getSession();
        String checkingCaptcha = request.getParameter("captcha");

        if (checkingCaptcha == null || checkingCaptcha.equals("yes")) {
            try {
                if (RecaptchaVerifyUtils.verify(gRecaptchaResponse)) {
                    // Retrieve the email and password from the login form
                    String email = request.getParameter("email");
                    String plaintextPassword = request.getParameter("password");

                    try (Connection conn = dataSource.getConnection()) {
                        boolean success = false;
                        if (typeOfLogin.equals("user")) {
                            String loginQuery = queryBuilder.getVerifyEmailQuery();
                            PreparedStatement loginStatement = conn.prepareStatement(loginQuery);
                            loginStatement.setString(1, email);
                            ResultSet userResultSet = loginStatement.executeQuery();
                            success = verifyCredentials(plaintextPassword, userResultSet);
                            userResultSet.close();
                            loginStatement.close();
                        } else if (typeOfLogin.equals("employee")) {
                            String employeeLoginQuery = queryBuilder.getVerifyEmployeeQuery();
                            PreparedStatement employeeLoginStatement = conn.prepareStatement(employeeLoginQuery);
                            employeeLoginStatement.setString(1, email);
                            ResultSet employeeRS = employeeLoginStatement.executeQuery();
                            success = verifyCredentials(plaintextPassword, employeeRS);
                            employeeRS.close();
                            employeeLoginStatement.close();
                        }

                        if (success) {
                            switch (typeOfLogin) {
                                case ("user"): {
                                    successLoginUser(responseJsonObject, request);
                                    session.setAttribute("user", new User(email));
                                    break;
                                } case ("employee"): {
                                    successLoginEmployee(responseJsonObject, request);
                                    session.setAttribute("employee", new Employee(email));
                                    break;
                                }
                            }
                        } else {
                            failLogin(responseJsonObject, request, false);
                        }
                        // Write our JSON output to the response
                        out.write(responseJsonObject.toString());

                        // Close all structures
                        out.close();


                    } catch (Exception e) {
                        request.getServletContext().log("Error: ", e);
                        responseJsonObject.addProperty("error", e.getMessage());
                        System.out.println("Error Occurred " + e.getMessage());
                        out.write(responseJsonObject.toString());
                        out.close();
                    }
                } else {
                    failLogin(responseJsonObject, request, true);
                    out.write(responseJsonObject.toString());
                    out.close();
                }
            } catch (Exception e) {
                request.getServletContext().log("Error: ", e);
                responseJsonObject.addProperty("error", e.getMessage());
                System.out.println("Error Occurred " + e.getMessage());
                out.write(responseJsonObject.toString());
                out.close();
            }
        } else {
            String email = request.getParameter("email");
            String plaintextPassword = request.getParameter("password");

            try (Connection conn = dataSource.getConnection()) {
                boolean success = false;
                if (typeOfLogin.equals("user")) {
                    String loginQuery = queryBuilder.getVerifyEmailQuery();
                    PreparedStatement loginStatement = conn.prepareStatement(loginQuery);
                    loginStatement.setString(1, email);
                    ResultSet userResultSet = loginStatement.executeQuery();
                    success = verifyCredentials(plaintextPassword, userResultSet);
                    userResultSet.close();
                    loginStatement.close();
                } else if (typeOfLogin.equals("employee")) {
                    String employeeLoginQuery = queryBuilder.getVerifyEmployeeQuery();
                    PreparedStatement employeeLoginStatement = conn.prepareStatement(employeeLoginQuery);
                    employeeLoginStatement.setString(1, email);
                    ResultSet employeeRS = employeeLoginStatement.executeQuery();
                    success = verifyCredentials(plaintextPassword, employeeRS);
                    employeeRS.close();
                    employeeLoginStatement.close();
                }

                if (success) {
                    switch (typeOfLogin) {
                        case ("user"): {
                            successLoginUser(responseJsonObject, request);
                            session.setAttribute("user", new User(email));
                            break;
                        } case ("employee"): {
                            successLoginEmployee(responseJsonObject, request);
                            session.setAttribute("employee", new Employee(email));
                            break;
                        }
                    }
                } else {
                    failLogin(responseJsonObject, request, false);
                }
                // Write our JSON output to the response
                out.write(responseJsonObject.toString());

                // Close all structures
                out.close();


            } catch (Exception e) {
                request.getServletContext().log("Error: ", e);
                responseJsonObject.addProperty("error", e.getMessage());
                System.out.println("Error Occurred " + e.getMessage());
                out.write(responseJsonObject.toString());
                out.close();
            }
        }

    }

    private static boolean verifyCredentials(String plaintextPassword, ResultSet loginResultSet) throws SQLException {
        boolean success = false;
        System.out.println("Verifying creds with plaintextpassword as " + plaintextPassword);
        // If we found the email
        if (loginResultSet.next()) {
            // get the encrypted password from the database
            String encryptedPassword = loginResultSet.getString("password");

            success = new StrongPasswordEncryptor().checkPassword(plaintextPassword, encryptedPassword);
        }

        return success;
    }

    private void successLoginUser(JsonObject jsonObject, HttpServletRequest request)
    {
        jsonObject.addProperty("permission", "user");

        jsonObject.addProperty("status", "success");
        jsonObject.addProperty("message", "Successfully logged in!");
        request.getServletContext().log("Login success");
        System.out.println("Login success");
    }

    private void successLoginEmployee(JsonObject jsonObject, HttpServletRequest request)
    {
        jsonObject.addProperty("permission", "employee");

        jsonObject.addProperty("status", "success");
        jsonObject.addProperty("message", "Successfully logged in!");
        request.getServletContext().log("Login success");
        System.out.println("Login success");
    }

    private void failLogin(JsonObject jsonObject, HttpServletRequest request, boolean captcha) {
        jsonObject.addProperty("status", "fail");
        request.getServletContext().log("Login failed");
        System.out.println("Login failed");
        if (captcha)
        {
            jsonObject.addProperty("message",
                    "Failed CAPTCHA");
        } else {
            jsonObject.addProperty("message",
                    "Incorrect email or password. Please try again.");
        }
    }
}