/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

// ***THIS IS FOR THE MOVIE LIST PAGE*** 

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMoviePageResult(resultData) {
    console.log("handleMoviePageResult: populating movie page table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let moviesTableBodyElement = jQuery("#movies_table_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        console.log("TITLE " + resultData[i]["movie_title"]);
        console.log("YEAR " + resultData[i]["movie_year"]);

        // Both these arrays should be length 3
        let movieStars = resultData[i]["movie_stars"];
        let movieStarsIds = resultData[i]["movie_stars_ids"];
        console.log(movieStars);
        console.log(movieStarsIds);


        let rowHTML = "";
        rowHTML += "<tr>"; // <tr> is table row

        // Generate the movie link here

        // SHOULD BE MOVIE'S ID
        rowHTML += "<th><a href=" + generateMovieLink(resultData[i]["movie_id"]) + ">" +
            resultData[i]["movie_title"] + "</a></th>";

        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_genres"].join(", ") + "</td>";

        // Loop over the stars and their ids
        let movieStarsLinks = "<th>";
        for (let j = 0; j < movieStars.length; ++j)
        {
            movieStarsLinks += "<a href=" + generateSingleStarLink(movieStarsIds[j])
                + ">"
                + movieStars[j]
                + "<br></a>";
        }
        movieStarsLinks += "</th>";

        rowHTML += movieStarsLinks;
        rowHTML += "<td>" + resultData[i]["movie_rating"] + "</td>";

        // "<th>" + // <th> is table head and it represents a header cell in a table. Essentially just bolds the text
        // Can use <td> (for table data cell) which would be the same thing except the text isn't bolded.
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        // rowHTML will be slapped onto the <tbody> tag in the HTML file
        moviesTableBodyElement.append(rowHTML);
    }
}

/**
 * Function to generate a movie link based on the movie ID
 * @param {string} movie_title - The title  of the movie
 * @returns {string} - The generated movie link
 */
function generateMovieLink(movie_id) {
    return "./single-movie.html?id=" + movie_id;
}

function generateSingleStarLink(star_id) {
    return "./single-star.html?id=" + star_id;
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
 * Once this .js is loaded, following scripts will be executed by the browser
 */
let search_form = jQuery("#search")
search_form.submit(submitSearchForm);
// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "top20", // Setting request url, which is mapped by MoviePageServlet in Stars.java
    success: (resultData) => handleMoviePageResult(resultData) // Setting callback function to handle data returned successfully by the MoviePageServlet
});