/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the employee to dashboard.html
    if (resultDataJson["status"] === "success" && resultDataJson["permission"] === "employee") {
        window.location.replace("dashboard.html");
    }
    else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        jQuery("#login_error_message").text(resultDataJson["message"]);
        grecaptcha.reset();
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitEmployeeLoginForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    let baseUrl = window.location.origin + "/" + window.location.pathname.split("/")[1];
    let servletMapping = "/login?type=employee";
    let fullUrl = baseUrl + servletMapping;
    // ex: full URL is https://localhost:8443/cs122b_fabflix_war/login?type=employee
    // (the url can't include the _dashboard since our LoginServlet doesn't know what _dashboard/login is,
    // they only know what /login is)
    jQuery.ajax(
        fullUrl, {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult
        }
    );
}


let login_form = jQuery("#login_form");
login_form.submit(submitEmployeeLoginForm);