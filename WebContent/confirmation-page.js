
function showPrice(salesData) {
    let salesTableBodyElement = jQuery("#sales_table_body");
    let saleId = salesData[0];

    jQuery("#sale_id").append("Sale ID: " + saleId);

    let i = 1;
    let totalCartPrice = 0;

    while(i < salesData.length) {
        // Concatenate the html tags with resultData jsonObject
        console.log("CART: TITLE " + salesData[i]["movie_title"]);

        let moviePrice = salesData[i]["movie_price"];
        let totalPriceOfMovieQuantity = salesData[i]["quantity_of_movie"] * moviePrice;
        let movieQuantity = salesData[i]["quantity_of_movie"];
        let movieTitle = salesData[i]["movie_title"];

        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + movieTitle + "</th>";
        rowHTML += "<td>" + movieQuantity + "</td>";
        rowHTML += "<td> $" + moviePrice + ".00</td>";
        rowHTML += "<td> $" + totalPriceOfMovieQuantity + ".00</td>";
        rowHTML += "</tr>";

        totalCartPrice += totalPriceOfMovieQuantity
        salesTableBodyElement.append(rowHTML);
        i++;
    }
    let totalCartPriceID = jQuery("#total_sales_price")
    let totalCartPriceDiv = "<p><b>Total Sale Price: $" + totalCartPrice + ".00</b></p>";
    totalCartPriceID.append(totalCartPriceDiv);
}


$.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "confirm", // Setting request url, which is mapped by MoviePageServlet in Stars.java
    success: (resultData) => showPrice(resultData) // Setting callback function to handle data returned successfully by the MoviePageServlet
});