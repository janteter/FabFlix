import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        } else if (httpRequest.getRequestURI().contains("/_dashboard")) {
            if (httpRequest.getSession().getAttribute("employee") == null) {
                String baseUrl = httpRequest.getRequestURL().toString();
                baseUrl = baseUrl.substring(0, baseUrl.indexOf("_dashboard") + 10);
                System.out.println(baseUrl);
                // Add a "/" if it's missing in the URL
                String redirectUrl = baseUrl.endsWith("/") ?
                        baseUrl + "employeelogin.html" :
                        baseUrl + "/employeelogin.html";
                System.out.println(redirectUrl);
                httpResponse.sendRedirect(redirectUrl);
            } else {
                chain.doFilter(request, response);
            }
        } else {
            if (httpRequest.getSession().getAttribute("user") == null &&
                    httpRequest.getSession().getAttribute("employee") == null) {
                httpResponse.sendRedirect("login.html");
            }
            else {
                chain.doFilter(request, response);
            }
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("login");
        allowedURIs.add("login?type=user");
        allowedURIs.add("login?type=employee");
        allowedURIs.add("_dashboard/employeelogin.html");
        allowedURIs.add("_dashboard/employeelogin.js");
    }

    public void destroy() {
        // ignored.
    }

}
