/*
 * CS 122B Project 4. Autocomplete
 *
 * This Javascript code uses this library: https://github.com/devbridge/jQuery-Autocomplete
 *
 * This example implements the basic features of the autocomplete search, features that are
 *   not implemented are mostly marked as "TODO" in the codebase as a suggestion of how to implement them.
 *
 * To read this code, start from the line "$('#autocomplete').autocomplete" and follow the callback functions.
 *
 */
/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    let pastResults = sessionStorage.getItem(query);
    if (pastResults != null) {
        console.log("Retrieving autocomplete results from cache");
        let jsonData = JSON.parse(pastResults);
        handleLookupAjaxSuccess(pastResults, query, doneCallback);
    } else {
        // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
        // with the query data
        if (query.length >= 3) {
            console.log("sending AJAX request to backend Java Servlet")
            jQuery.ajax({
                "method": "GET",
                // generate the request url from the query.
                // escape the query string to avoid errors caused by special characters
                "url": encodeURI("api/autocomplete?query=" + query),
                "success": function (data) {
                    // pass the data, query, and doneCallback function into the success handler
                    handleLookupAjaxSuccess(data, query, doneCallback)
                },
                "error": function (errorData) {
                    console.log("lookup ajax error")
                    console.log(errorData)
                }
            })
        }
    }
}

/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    // console.log("Lookup successful")

    // parse the string into JSON
    let jsonData = JSON.parse(data);
    console.log("Using suggestion list:\n" + data)
    sessionStorage.setItem(query, data);

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { "suggestions": jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    window.location.href = createSingleMoviePageRedirectURL(suggestion["data"]["movie_id"]);
}

function createSingleMoviePageRedirectURL(movieId) {
    return "single-movie.html?id=" + movieId;
}

/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$("#fulltext-search-bar").autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    minChars : 3
});

/*
 * do normal full text search if no suggestion is selected
 */
function submitFulltextSearch(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    // for searching on the main page, all you need to do is check if at least one of the
    // search queries is not null and then redirect with the new url
    jQuery.ajax(
        "api/index?typeOfSearch=fulltext", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: fulltext_form.serialize(),
            success: handleFullTextSearchResult
        }
    );
}

function handleFullTextSearchResult(resultData) {
    if (resultData["valid"]) {
        // make a get request to MoviePageServlet via redirection
        window.location.href = constructFulltextSearchRedirectURL(resultData["tokenized_query"]);
    } else {
        console.log("Fulltext Search Failed!");
    }
}

function constructFulltextSearchRedirectURL(fulltextTokens) {
    return encodeURI("results.html?fulltext=" + fulltextTokens.join(" "));
}

// bind pressing enter key to a handler function
$("#fulltext-search-bar").keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode === 13) {
        // pass the value of the input box to the handler function
        submitFulltextSearch(event)
    }
})



let fulltext_form = jQuery("#fulltext-search");
// Bind the submit action of the form to a handler function
fulltext_form.submit(submitFulltextSearch);