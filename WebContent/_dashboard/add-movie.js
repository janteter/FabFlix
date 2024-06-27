let add_movie_form = $("#place_order");

function handleAddedMovie(inputData)
{
    $("#place_order")[0].reset();
    inputData = JSON.parse(inputData);
    console.log(inputData);
    let messageArea = jQuery("#payment_message");

    if (inputData["error"])
    {
        messageArea.text("Movie Already Exists!");
    }
    else
    {
        messageArea.text("Movie ID: " + inputData["id"]
        + " Genre ID: " + inputData["genreID"]
        + " Star ID: " + inputData["starID"]);
    }
}


function processAddedMovie(movieData) {
    console.log("submit movie form");
    movieData.preventDefault();
    let baseUrl = window.location.origin + "/" + window.location.pathname.split("/")[1];
    let servletMapping = "/add-movie";
    let fullUrl = baseUrl + servletMapping;
    $.ajax(fullUrl, {
        method: "POST",
        data: add_movie_form.serialize(),
        success: handleAddedMovie
    });
}

add_movie_form.submit(processAddedMovie);