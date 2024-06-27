/**
 * Submit POST call for decrement, increment, and delete of movie quantity in cart
 * @param buttonData
 */
function handleCartInc(buttonData) {
    console.log("submit cart increase");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    let movieID = buttonData.getAttribute('movie_id');

    $.ajax("cart", {
        method: "POST",
        data: {currentMovieId : movieID, typeOfChange : "increment"}, // data will hold some field specifying if its is dec or increment
        success: function() {
            location.reload();
        }
    });
}

function handleCartDec(buttonData) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */

    let movieID = buttonData.getAttribute('movie_id');

    $.ajax("cart", {
        method: "POST",
        data: {currentMovieId : movieID, typeOfChange : "decrement"}, // data will hold some field specifying if its is dec or increment
        success: function() {
            location.reload();
        }
    });
}

function handleCartDelete(buttonData) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
        //button.preventDefault();
    let movieID = buttonData.getAttribute('movie_id');
    let movieTitle = buttonData.getAttribute('movie_title')

    $.ajax("cart", {
        method: "POST",
        data: {currentMovieId : movieID, typeOfChange : "delete"}, // data will hold some field specifying if its is dec or increment
        success: function() {
            alert(movieTitle + " removed from cart ");
            location.reload();
        }
    });
}

function handleCartInfoResult(resultData){
    console.log("handleMoviePageResult: populating movie page table from resultData");

    let cartTableBodyElement = jQuery("#cart_table_body");

    let i = 0;
    let totalCartPrice = 0;

    while(i < resultData.length) {
        // Concatenate the html tags with resultData jsonObject
        console.log("CART: TITLE " + resultData[i]["movie_title"]);

        let moviePrice = resultData[i]["movie_price"];
        let totalPriceOfMovieQuantity = resultData[i]["quantity_of_movie"] * moviePrice;
        let movieId = resultData[i]["movie_id"];
        let movieQuantity = resultData[i]["quantity_of_movie"];
        let movieTitle = resultData[i]["movie_title"];

        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + movieTitle + "</th>";
        rowHTML += "<td><button type= \"button\" style=\"float: left\" VALUE=\"-1\"  onclick=\"handleCartDec(this)\" movie_id=\"" + movieId + "\">-1</button>"
            + movieQuantity + "<button type= \"button\" style=\"float: right\" VALUE=\"+1\" onclick=\"handleCartInc(this)\" movie_id=\"" + movieId + "\">+1</button></td>";
        rowHTML += "<td><button type=\"button\" VALUE=\"Delete\" onclick=\"handleCartDelete(this)\" movie_id=\"" + movieId
            + "\" movie_title=\"" + movieTitle + "\">Delete</td>";
        rowHTML += "<td> $" + moviePrice + ".00</td>";
        rowHTML += "<td> $" + totalPriceOfMovieQuantity + ".00</td>";
        rowHTML += "</tr>";

        totalCartPrice += totalPriceOfMovieQuantity
        cartTableBodyElement.append(rowHTML);
        i++;
    }
    let totalCartPriceID = jQuery("#total_cart_price")
    let totalCartPriceDiv = "<p><b>Total Cart Price: $" + totalCartPrice + ".00</b></p>";
    totalCartPriceID.append(totalCartPriceDiv);
}

$.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "cart", // Setting request url, which is mapped by MoviePageServlet in Stars.java
    success: (resultData) => handleCartInfoResult(resultData) // Setting callback function to handle data returned successfully by the MoviePageServlet
});
