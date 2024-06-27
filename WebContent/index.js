/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleSearchResult(resultDataString) {
    let resultDataJson = resultDataString;
    console.log("Handling search response");
    console.log(resultDataJson);
    if (resultDataJson['valid']) {
        let title = resultDataJson['title'];
        let year = resultDataJson['year'];
        let director = resultDataJson['director'];
        let star = resultDataJson['star'];
        // Redirect to the results.html
        window.location.href = constructSearchRedirectURL(title, year, director, star);
    } else {
        console.log("invalid or blank search, nothing is going to happen");
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitSearchForm(formSubmitEvent) {
    console.log("submit search form on main page");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    // for searching on the main page, all you need to do is check if at least one of the
    // search queries is not null and then redirect with the new url
    jQuery.ajax(
        "api/index?typeOfSearch=advanced", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: search_form.serialize(),
            success: handleSearchResult
        }
    );
}

function constructSearchRedirectURL(title, year, director, star) {
    return "results.html?title=" + title + "&" +
        "year=" + year + "&" +
        "director=" + director + "&" +
        "star=" + star;
}

function fillInGenres(genreData) {
    let genre_links = jQuery("#browse_genre_links");

    for (let i = 0; i < genreData.length; ++i) {
        // <a href="results.html?genre=1&prefix=">1. Action</a>
        let new_genre_link = generateNewGenreLink(genreData[i]);
        genre_links.append(new_genre_link);
    }
}

function generateNewGenreLink(genreJson) {
    let result = "<a href=\"results.html?genre=" + genreJson["genre_id"] + "&prefix=\">";
    result += genreJson["genre_id"] + ". " + genreJson["genre_name"];
    result += "</a>";
    return result;
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/index",
    success: (resultData) => fillInGenres(resultData) // call doGet in the IndexServlet
});

let search_form = jQuery("#search");


// Bind the submit action of the form to a handler function
search_form.submit(submitSearchForm);