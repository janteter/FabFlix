let add_star_form = $("#place_order");


function handleAddedStar(inputData)
{
    $("#place_order")[0].reset();
    inputData = JSON.parse(inputData);
    console.log(inputData);
    let messageArea = jQuery("#payment_message");

    if (inputData["valid_checkout"])
    {
        messageArea.text("Success! New Star ID:" + inputData["new_star_id"]);
    }
    else
    {
        messageArea.text(inputData["failure_message"]);
    }
}


function processAddedStar(starData) {
    console.log("submit star form");
    starData.preventDefault();
    let baseUrl = window.location.origin + "/" + window.location.pathname.split("/")[1];
    let servletMapping = "/add-star";
    let fullUrl = baseUrl + servletMapping;
    $.ajax(fullUrl, {
        method: "POST",
        data: add_star_form.serialize(),
        success: handleAddedStar
    });
}

add_star_form.submit(processAddedStar);