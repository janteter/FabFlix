/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    console.log("handleResult: populating star info from resultData");

    let starId = getParameterByName("id");
    console.log("STAR ID IS " + starId)

    let starInfo = resultData[0]

    let starTitle = jQuery("#star_title");
    starTitle.append(starInfo["star_name"]);

    let movieTitles = starInfo["movie_titles"];
    let movieIds = starInfo["movie_ids"];

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#star_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>Star Name: " + starInfo["star_name"] + "</p>" +
        "<p style = \"font-size:75%\">Date Of Birth: " + starInfo["star_dob"] + "</p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    let rowHTML = "";
    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < movieIds.length; ++i)
    {
        rowHTML += "<tr>"
        rowHTML += "<th><a href= " +
            generateMovieLink(movieIds[i]) +
            ">"
            + movieTitles[i] + "</a></th>";
        rowHTML += "</tr>"
    }
    movieTableBodyElement.append(rowHTML);
}

function generateMovieLink(movie_id) {
    return "./single-movie.html?id=" + movie_id;
}

function handleSearchInput(resultData) {
    if (resultData['valid']) {
        let title = resultData['title'];
        let year = resultData['year'];
        let director = resultData['director'];
        let star = resultData['star'];

        function constructSearchRedirectURL(title, year, director, star) {
            return "results.html?title=" + title + "&"
                + "year=" + year + "&"
                + "director=" + director + "&"
                + "star=" + star;
        }

        window.location.href = constructSearchRedirectURL(title, year, director, star);
    }
}

function handleSearchInput(resultData) {
    if (resultData['valid']) {
        let title = resultData['title'];
        let year = resultData['year'];
        let director = resultData['director'];
        let star = resultData['star'];

        function constructSearchRedirectURL(title, year, director, star)
        {
            return "results.html?title=" + title + "&"
                + "year=" + year + "&"
                + "director=" + director + "&"
                + "star=" + star;
        }

        window.location.href = constructSearchRedirectURL(title, year, director, star);
    }
}

function handleSearchInput(resultData) {
    if (resultData['valid']) {
        let title = resultData['title'];
        let year = resultData['year'];
        let director = resultData['director'];
        let star = resultData['star'];

        function constructSearchRedirectURL(title, year, director, star)
        {
            return "results.html?title=" + title + "&"
                + "year=" + year + "&"
                + "director=" + director + "&"
                + "star=" + star;
        }

        window.location.href = constructSearchRedirectURL(title, year, director, star);
    }
}

function submitSearchForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    jQuery.ajax(
        "api/results", {
            method: "POST",
            data: search_form.serialize(),
            success: handleSearchInput
        }
    );
}
/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */
let search_form = jQuery("#search")
search_form.submit(submitSearchForm);


// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});