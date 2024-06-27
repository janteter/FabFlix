let place_order_form = $("#place_order");

function handleCreditCardInput(inputData)
{
    inputData = JSON.parse(inputData);
    console.log(inputData);
    let messageArea = jQuery("#payment_message");

    if (inputData["valid_checkout"])
    {
        messageArea.text("Success!");
        window.location.href = "confirmation-page.html";
    }
    else
    {
        messageArea.text(inputData["failure_message"]);
    }
}


/**
 * Submit POST call for decrement, increment, and delete of movie quantity in cart
 * @param orderData
 */
function processPlacedOrder(orderData) {
    console.log("submit cart form");
    orderData.preventDefault();

    $.ajax("checkout", {
        method: "POST",
        data: place_order_form.serialize(),
        success: handleCreditCardInput
    });
}

function showPrice(resultData)
{
    jQuery("#total_sales_price").append("$" + resultData["cart_total_price"] + ".00");
}

place_order_form.submit(processPlacedOrder);

$.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "checkout", // Setting request url, which is mapped by MoviePageServlet in Stars.java
    success: (resultData) => showPrice(resultData) // Setting callback function to handle data returned successfully by the MoviePageServlet
});