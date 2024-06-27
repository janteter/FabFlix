function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    console.log("results for " + target + " is " + results);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleResultData(resultData) {
    console.log("Populating movie page table from results.js");

    let moviesTableBodyElement = jQuery("#results_table_body");

    let movieResults = resultData["results"];
    if (movieResults.length === 0) {
        jQuery("#results_text").append("No Results Found");
    }
    else {
        jQuery("#results_text").append("Results");
    }

    for (let i = 0; i < movieResults.length; i++) {
        let movieStars = movieResults[i]["movie_stars"];
        let movieStarsIds = movieResults[i]["movie_stars_ids"];
        let movieGenres = movieResults[i]["movie_genres"]
        let movieGenresIds = movieResults[i]["movie_genres_ids"]

        let rowHTML = "";
        rowHTML += "<tr>";

        rowHTML += "<th><a href=" + generateMovieLink(movieResults[i]["movie_id"]) + ">" +
            movieResults[i]["movie_title"] + "</a></th>";

        rowHTML += "<th>" + movieResults[i]["movie_year"] + "</th>";
        rowHTML += "<td>" + movieResults[i]["movie_director"] + "</td>";

        let movieGenreLinks = "<th>";
        for (let j = 0; j < movieGenres.length; ++j) {
            movieGenreLinks += "<a href=" + generateGenreLink(movieGenresIds[j])
                + ">";
            if (j === movieGenres.length - 1) {
                movieGenreLinks += movieGenres[j] + "</a>";
            }

            else {
                movieGenreLinks += movieGenres[j] + "," + "</a>";
            }
        }
        rowHTML += movieGenreLinks;

        let movieStarsLinks = "<th>";
        for (let k = 0; k < movieStars.length; ++k) {
            movieStarsLinks += "<a href=" + generateSingleStarLink(movieStarsIds[k])
                + ">"
                + movieStars[k]
                + "<br></a>";
        }
        movieStarsLinks += "</th>";

        rowHTML += movieStarsLinks;
        rowHTML += "<td>" + movieResults[i]["movie_rating"] + "</td>";
        rowHTML += "<td>" + writeGetToCartButtonHTML(movieResults[i]["movie_id"]) + "</td>";
        rowHTML += "</tr>";
        moviesTableBodyElement.append(rowHTML);
    }

    jQuery("#page_number_display").val(resultData["page_number"]);
}

function writeGetToCartButtonHTML(movieId)
{
    // should look like <button id="add_to_cart" type="submit" movie_id="id">Add To Cart</button>
    return "<button id=\"add_to_cart\" type=\"submit\" " + "movie_id=\"" + movieId + "\" class=\"add_to_cart_button\">" +
        "Add To Cart</button>";
}

function generateMovieLink(movie_id) {
    return "./single-movie.html?id=" + movie_id;
}

function generateSingleStarLink(star_id) {
    return "./single-star.html?id=" + star_id;
}

function generateGenreLink(genre_id) {
    return "./results.html?genre=" + genre_id + "&prefix=";
}


function submitSearchForm(formSubmitEvent) {
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    // for searching on the main page, all you need to do is check if at least one of the
    // search queries is not null and then redirect with the new url

    jQuery.ajax(
        "api/results", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: search_form.serialize(),
            success: handleSearchInput
        }
    );
}

function constructGETSearchURI(title, year, director, star)
{
    return "api/results?title=" +
        title + "&" +
        "year=" + year + "&" +
        "director=" + director + "&" +
        "star=" + star;
}

function constructGETBrowseURI(genreId, prefixChar)
{
    return "api/results?genre=" + genreId + "&"
        + "prefix=" + prefixChar;
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

function submitSortForm(formSubmitEvent)
{
    console.log("Submit sort form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    // for searching on the main page, all you need to do is check if at least one of the
    // search queries is not null and then redirect with the new url

    jQuery.ajax(
        "api/sort", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: sort_form.serialize(),
            success: handleSortFormInput
        }
    );
}

function handleSortFormInput(resultData) {
    console.log(resultData);
    if (resultData["different_sorting"])
    {
        window.location.href = constructRedirectSortURL(resultData["sort"], resultData["n"]);
    }

    else
    {
        console.log("Same sorting results, nothing is changing");
    }
}

function submitPageDec(formSubmitEvent)
{
    // formSubmitEvent.preventDefault();
    jQuery.ajax(
        "api/sort?page=", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: {pageChangeValue: "-1", page_number: jQuery("#page_number_display").val()},
            success: handlePageChangeInput
        }
    );
}

function submitPageInc(formSubmitEvent)
{
    // formSubmitEvent.preventDefault();
    jQuery.ajax(
        "api/sort", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: {pageChangeValue: "1", page_number : jQuery("#page_number_display").val()},
            success: handlePageChangeInput
        }
    );
}

function handlePageChangeInput(resultData) {
    if (resultData["valid_page_change"])
    {
        // change the page
        let newPageNum = resultData["new_page_num"]
        window.location.href = "results.html?page=" + newPageNum;
    }
    else
    {
        console.log("Invalid Page Change!");
    }
}

function constructRedirectSortURL(sortIndex, numResultsPerPage)
{
    return "results.html?sort=" +
        sortIndex + "&" +
        "n=" + numResultsPerPage;
}

jQuery("#results_table_body").on("click", ".add_to_cart_button",
    function() {
        let movieId = jQuery(this).attr("movie_id");

        // Make a post request to the shopping cart servlet
        jQuery.ajax(
            "cart", {
                method: "POST",
                // Serialize the login form to the data sent by POST request
                data: {currentMovieId : movieId, typeOfChange : "add"},
                success: function() {
                    alert("Successfully Added To Cart")
                }
            }
        );
    });

let sort_form = jQuery("#sort_form")
sort_form.submit(submitSortForm);

let sortIndex = getParameterByName("sort");
let numResultsPerPage = getParameterByName("n");
let newPageNum = getParameterByName("page");

let search_form = jQuery("#search")
search_form.submit(submitSearchForm);

let title = getParameterByName("title");
let year = getParameterByName("year");
let director = getParameterByName("director");
let star = getParameterByName("star");

let genre = getParameterByName("genre");
let prefixChar = getParameterByName("prefix");

let fulltext = getParameterByName("fulltext");

let getRequestURL = "";

// Makes the HTTP GET request and registers on success callback function handleResultData
// Made a search
if (title != null || year != null || director != null || star != null) {
    getRequestURL = constructGETSearchURI(title, year, director, star);
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        // Setting request url
        url: getRequestURL,
        success: (resultData) => handleResultData(resultData)
    });
}
// Clicked browse link
else if (genre != null || prefixChar != null) {
    // Makes the HTTP Get request and registers on success callback function handleResultData
    // THIS IS FOR BrowseServlet
    getRequestURL = constructGETBrowseURI(genre, prefixChar);
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        // Setting request url
        url: getRequestURL,
        success: (resultData) => handleResultData(resultData)
    });
}
// GET request for when sorting
else if (sortIndex && numResultsPerPage) {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        // Setting request url
        url: "api/results?sort=" + sortIndex + "&" +
            "n=" + numResultsPerPage,
        success: (resultData) => handleResultData(resultData)
    });
}
// Changing page
else if (newPageNum) {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        // Setting request url
        url: "api/results?page=" + newPageNum,
        success: (resultData) => handleResultData(resultData)
    });
}
// Fulltext search
else if (fulltext) {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        // Setting request url
        url: "api/results?fulltext=" + fulltext,
        success: (resultData) => handleResultData(resultData)
    });
}
// Jumping back to previous results page
else {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        // Setting request url
        url: "api/results",
        success: (resultData) => handleResultData(resultData)
    });
}

