/**
 * This example is following frontend and backend separation.
 * single movie
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */
let cart = $("#cart");

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
function handleSinglePageMovieResult(resultData) {
    console.log("handleSinglePageMovieResult: populating star table from resultData");

    // Fill in all info
    jQuery("#single_movie_title").append(resultData[0]["movie_name"]);
    jQuery("#single_movie_header").append(resultData[0]["movie_name"]);

    jQuery("#movie_year").append(resultData[0]["movie_year"]);
    jQuery("#movie_director").append(resultData[0]["movie_director"]);
    jQuery("#movie_rating").append(resultData[0]["movie_rating"]);

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let singleMovieStarsTable = jQuery("#movie_stars_table_body");
    let singleMovieGenresTable = jQuery("#movie_genres_table_body");

    let movieStars = resultData[0]["movie_stars"];
    let movieStarsIds = resultData[0]["movie_stars_ids"];
    let movieGenres = resultData[0]["movie_genres"];
    let movieGenresIds = resultData[0]["movie_genres_ids"];

    let starHTML = "";
    for (let i = 0; i < movieStarsIds.length; ++i)
    {
        starHTML += "<tr><td><a href=" +
            generateSingleStarLink(movieStarsIds[i]) +
            ">" +
            movieStars[i] +
            "</td></a></tr>";
    }
    singleMovieStarsTable.append(starHTML);

    let genreHTML = "";
    for (let i = 0; i < movieGenresIds.length; ++i)
    {
        genreHTML += "<tr><td><a href=" +
            generateGenreLink(movieGenresIds[i]) +
            ">" +
            movieGenres[i] +
            "</td></a></tr>";
    }
    singleMovieGenresTable.append(genreHTML);
}

function generateSingleStarLink(star_id) {
    return "./single-star.html?id=" + star_id;
}

function generateGenreLink(genre_id)
{
    return "./results.html?genre=" + genre_id + "&" + "prefix=";
}

//function will print html to show success
function successfulCartUpdate(updateInfo) {
    window.alert("Successfully Added To Cart")
}

function failedCartUpdate(updateInfo) {
    window.alert("Failed To Add To Cart !")
}

function updateCartInfoEventHandler(newCartInformation) {
    console.log("update cart event handler triggered");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    newCartInformation.preventDefault();

    $.ajax(
        "cart", {
            method: "POST",
            data: {currentMovieId : movieId, typeOfChange : "add"},
            // currentMovieId will pass the parameter "currentMovieId" in to the SingleMovieServlet
            success: successfulCartUpdate,
            error: failedCartUpdate
        }
    );
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

let search_form = jQuery("#search")
search_form.submit(submitSearchForm);

let movieId = getParameterByName('id');

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleSinglePageMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

// Bind the submit action of the form to an event handler function
cart.submit(updateCartInfoEventHandler);