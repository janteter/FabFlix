/**
 * Submit POST call for decrement, increment, and delete of movie quantity in cart
 * @param sqlMetaData
 */

let place_order_form = $("#place_order");


function handleMetaInfoResult(sqlMetaData){
    console.log("handleMoviePageResult: populating movie page table from resultData");

    let dashboardTableBodyElement = jQuery("#dashboard_table_body");

    let i = 0;

    while(i < sqlMetaData.length) {
        // Concatenate the html tags with resultData jsonObject
        console.log("TABLE: " + sqlMetaData[i]["table_name"]);

        let tableName = sqlMetaData[i]["table_name"];
        let tableFieldArr = sqlMetaData[i]["field_array"];
        let tableTypeArr = sqlMetaData[i]["type_array"];

        //ERROR CHECK THAT BOTH ARRAYS ARE THE SAME LENGTH

        let rowHTML = "";
        rowHTML += "<tr>"; // <tr> is table row
        rowHTML += "<th>" + tableName + "</th>";

        // Loop over the stars and their ids
        let fieldColumn = "<th>";
        for (let j = 0; j < tableFieldArr.length && j < tableTypeArr.length; ++j)
        {
            fieldColumn += "<a>" + tableFieldArr[j]
                + "<br></a>";
        }
        fieldColumn += "</th>";

        rowHTML += fieldColumn;

        let typeColumn = "<th>";
        for (let j = 0; j < tableFieldArr.length && j < tableTypeArr.length; ++j)
        {
            typeColumn += "<a>" + tableTypeArr[j]
                + "<br></a>";
        }
        typeColumn += "</th>";

        rowHTML += typeColumn;
        rowHTML += "</tr>";

        dashboardTableBodyElement.append(rowHTML);
        i++;
    }
}

let baseUrl = window.location.origin + "/" + window.location.pathname.split("/")[1];
let servletMapping = "/dashboard";
let fullUrl = baseUrl + servletMapping;
$.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: fullUrl, // Setting request url, which is mapped by MoviePageServlet in Stars.java
    success: (resultData) => handleMetaInfoResult(resultData) // Setting callback function to handle data returned successfully by the MoviePageServlet
});
