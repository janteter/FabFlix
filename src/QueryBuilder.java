import java.util.ArrayList;
import java.util.Map;

public class QueryBuilder {
    // This query returns the 3 genres sorted by alphabetical order
    // THIS IS FOR DISPLAYING RESULTS
    private final String generalGenreQuery =
            "SELECT g.id as genreId, g.name as genre " +
                    "FROM genres_in_movies gm " +
                    "JOIN genres g ON gm.genreId = g.id " +
                    "WHERE gm.movieId = ? " +
                    "ORDER BY g.name ASC "; // +
//            "LIMIT 3 ";

    // This query returns the 3 stars sorted by the # of movies that star starred in
    // THIS IS FOR DISPLAYING RESULTS
    private final String generalStarsQuery =
            "SELECT * " +
                    "FROM (SELECT starId, name " +
                    "FROM stars_in_movies sm, " +
                    "(SELECT s.id, s.name FROM stars_in_movies sm " +
                    "JOIN stars s ON sm.starId = s.id " +
                    "WHERE sm.movieId = ?) as movieStars " +
                    "WHERE movieStars.id = sm.starId) as results " +
                    "GROUP BY results.starId " +
                    "ORDER BY COUNT(starId) DESC, name ASC "; //+
//            "LIMIT 3 ";

    private final String movieDetailsWithIDQuery =
            "SELECT * " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r on r.movieId = m.id " +
                    "WHERE m.id = ? ";

    // This is a subquery that is only used on Searching when the "star"
    // searchbox is filled in the frontend
    private static final String searchStarParameterSubquery =
            "(SELECT name, birthYear, starId, movieId " +
                    "FROM stars s, stars_in_movies sm " +
                    "WHERE sm.starId = s.id AND s.name LIKE ?) AS moviestars "; // ? is %pattern%

    // When BROWSING by genre, gets both the movie details and the ratings of the movie
    private final String browseByGenreQuery =
            "SELECT m.id, m.title, m.year, m.director, r.rating " +
                    "FROM movies m " +
                    "JOIN genres_in_movies gm ON m.id = gm.movieId " +
                    "LEFT JOIN ratings r ON r.movieId = gm.movieId " +
                    "WHERE gm.genreId = ? ";

    // When BROWSING by prefix, gets both the movie details and the ratings of the movie
    private final String browseByPrefixQuery =
            // %s will either be "NOT REGEXP '^[a-zA-Z0-9]'" or
            // "LIKE '" + prefixChar + "%'"
            "SELECT m.id, m.title, m.year, m.director, r.rating \n" +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "WHERE m.title ? ";

    private final String checkoutGetCreditCardInfoQuery =
            "SELECT cc.id, cc.firstName, cc.lastName, cc.expiration " +
                    "FROM creditcards cc " +
                    "WHERE cc.firstName = ? AND cc.lastName = ? AND cc.id = ? AND cc.expiration = ? ";

    private final String necessaryInputForSalesTable =
            "SELECT c.id " + "FROM customers c " + "WHERE c.ccId = ? ";

    private final String updateSalesTable =
            "INSERT INTO sales (customerId, movieId, saleDate, quantity) " +
                    "VALUES ( ? , ? , ? , ? ) ";

    private final String saleIdQuery =
            "SELECT MAX(id) as saleId FROM sales s ";

    private final String singleStarSortedMoviesQuery =
            "SELECT * " +
                    "FROM stars s, stars_in_movies sim, movies m " +
                    "WHERE m.id = sim.movieid AND sim.starId = s.id AND s.id = ? " +
                    "ORDER BY year DESC, title ASC ";

    private final String verifyEmailQuery =
            "SELECT email, password FROM customers " +
                    "WHERE email = ? ";

    private final String verifyEmployeeQuery =
            "SELECT email, password FROM employees " +
                    "WHERE email = ? ";

    private final String tableMetadata =
            "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA= 'moviedb' AND TABLE_NAME= ? ;";

    private final String listOfTables =
            "SHOW TABLES ";

    private final String maxStarID =
            "SELECT max(id) as maxStarID FROM stars ;";

    private final String allGenresFromGenreTableQuery =
            "SELECT * FROM genres ";

    private final String addStarToDB =
            "INSERT INTO stars (id, name, birthYear) " +
                    "VALUES ( ? , ? , ? ) ";

    private final String addStarToStarsInMovies =
            "INSERT INTO stars_in_movies (starId, movieId) " +
                    "VALUES ( ? , ? ) ";

    private final String addMovieToDBViaInsert =
            "INSERT INTO movies (id, title, year, director, price) " +
                    "VALUES ( ? , ? , ? , ? , ? ) ";
    private final String addMovieToDB =
            "CALL add_movie( ? , ? , ? , ? , ? , ? ) ";

    private final String addGenreToDB =
            "INSERT INTO genres (name) " +
                    "VALUES ( ? ) ";

    private final String addToGenresInMovies =
            "INSERT INTO genres_in_movies (genreId, movieId) " +
                    "VALUES ( ? , ? ) ";

    private final String fulltextSearchQuery =
            "SELECT m.id, m.title, m.year, m.director, m.price, r.rating " +
            "FROM movies m LEFT JOIN ratings r ON r.movieId = m.id " +
            "WHERE MATCH(m.title) AGAINST (? IN BOOLEAN MODE) ";

    private static ArrayList<String> sortingOptions;

    static
    {
        sortingOptions = new ArrayList<>();
        sortingOptions.add("ORDER BY title ASC, rating ASC ");
        sortingOptions.add("ORDER BY title ASC, rating DESC ");
        sortingOptions.add("ORDER BY title DESC, rating ASC ");
        sortingOptions.add("ORDER BY title DESC, rating DESC ");
        sortingOptions.add("ORDER BY rating ASC, title ASC ");
        sortingOptions.add("ORDER BY rating ASC, title DESC ");
        sortingOptions.add("ORDER BY rating DESC, title ASC ");
        sortingOptions.add("ORDER BY rating DESC, title DESC ");
    }


    public String getGeneralGenreQuery() {
        return generalGenreQuery;
    }

    public String getGeneralStarsQuery() {
        return generalStarsQuery;
    }

    public String getCheckoutGetCreditCardInfoQuery() {
        return checkoutGetCreditCardInfoQuery;
    }

    public String getNecessaryInputForSalesTable() { return necessaryInputForSalesTable; }

    public String getRequestForUpdateSalesTable() { return updateSalesTable; }

    public String getBrowseByGenreQuery() { return browseByGenreQuery; }

    public String getBrowseByPrefixQuery() { return browseByPrefixQuery; }

    public String getMovieDetailsWithIDQuery() { return movieDetailsWithIDQuery; }

    public String getSingleStarSortedMoviesQuery() { return singleStarSortedMoviesQuery; }

    public String getVerifyEmailQuery() { return verifyEmailQuery; }

    public String getVerifyEmployeeQuery() { return verifyEmployeeQuery; }

    public String getListOfTables() { return listOfTables; }

    public String getMaxStarID() { return maxStarID; }

    public String getAddStarToDB() { return addStarToDB; }

    public String getTableMetadata() { return tableMetadata; }

    public String getAllGenresFromGenreTableQuery() { return allGenresFromGenreTableQuery; }

    public String getAddMovieToDBViaInsertQuery() { return addMovieToDBViaInsert; }

    public String buildResultsThreeGenresQuery() {
        return this.addLimit(generalGenreQuery, 3);
    }

    public String buildResultsThreeStarsQuery() {
        return this.addLimit(generalStarsQuery, 3);
    }

    public String getSaleIdQuery() {
        return saleIdQuery;
    }

    public String getAddMovieToDB() { return addMovieToDB; }

    public String getAddGenreToDB() { return addGenreToDB; }

    public String getAddGenreInMovieToDB() { return addToGenresInMovies; }

    public String getAddStarToStarsInMovies() { return addStarToStarsInMovies; }

    public String getFulltextSearchQuery() { return fulltextSearchQuery; }

    public String buildSearchQuery(Map<String, String> searchParams) {
        StringBuilder finalQuery = new StringBuilder("SELECT DISTINCT id, title, year, director, rating " +
                "FROM movies m LEFT JOIN ratings r ON r.movieId = m.id ");

        boolean hasWhereClause = false;
        for (Map.Entry<String, String> entry : searchParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.equals("star") && !value.isEmpty()) {
                hasWhereClause = true;

                finalQuery.append(",\n ");
                finalQuery.append(searchStarParameterSubquery);
                finalQuery.append("WHERE id = moviestars.movieId ");
            }

            else {
                if (hasWhereClause) {
                    if (!value.isEmpty()) {
                        if (key.equals("year")) {
                            finalQuery.append("\n AND year = ? ");
                        }
                        else {
                            finalQuery.append("\n AND " + key + " LIKE ? ");
                        }
                    }
                }

                else {
                    if (!value.isEmpty()) {
                        if (key.equals("year")) {
                            finalQuery.append("\n WHERE year = ? ");
                        }
                        else {
                            finalQuery.append("\n WHERE " + key + " LIKE ? ");
                        }
                        hasWhereClause = true;
                    }
                }
            }
        }
        return finalQuery.toString();
    }

    public String addOffset(String query, int numResultsPerPage, int pageNum)
    {
        int offsetAmount = numResultsPerPage * (pageNum - 1);

        assert offsetAmount >= 0;
        return query + " OFFSET " + offsetAmount;
    }

    public String addLimit(String query, int limit)
    {
        return query + " LIMIT " + limit;
    }

    public static String addSorting(String query, int sortingIndex)
    {
        return query + sortingOptions.get(sortingIndex);
    }
}