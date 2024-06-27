import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Types;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataXMLParser {
    private LinkedHashMap<String, Director> directorsHashMap = new LinkedHashMap<>();
    private LinkedHashMap<String, Movie> moviesHashMap = new LinkedHashMap<>();
    private LinkedHashMap<String, Actor> actorsHashMap = new LinkedHashMap<>();
    private final String MOVIES_XML_PATH = "stanford-movies/mains243.xml";
    private final String ACTORS_XML_PATH = "stanford-movies/actors63.xml";
    private final String CASTS_XML_PATH = "stanford-movies/casts124.xml";
    private MovieDataXMLParser movieDataXMLParser;
    private ActorsDataXMLParser actorsDataXMLParser;
    private CastsDataXMLParser castsDataXMLParser;
    private QueryBuilder queryBuilder = new QueryBuilder();
    private final HashMap<String, Integer> existingGenres = new HashMap<>();
    private static HashMap<String, String> genreMap;
    private final int MAX_BATCH_SIZE = 300;
    private int numberOfStarsInserted = 0;
    private int numberOfMoviesInserted = 0;
    private int numberOfGenresInserted = 0;
    private int numberOfStarsInMoviesInserted = 0;
    private int numberOfGenresInMoviesInserted = 0;
    private int numberOfMoviesWithNoStars = 0;

    static {
        genreMap = new HashMap<>();
        genreMap.put("ACTN", "Action");
        genreMap.put("HIST", "History");
        genreMap.put("SURR", "Surreal");
        genreMap.put("S.F.", "Science Fiction");
        genreMap.put("ROAD", "Road");
        genreMap.put("MUSICAL", "Musical");
        genreMap.put("CNRBB", "Crime");
        genreMap.put("PSYCH", "Psychological");
        genreMap.put("SUSP", "Suspense");
        genreMap.put("DRAM", "Drama");
        genreMap.put("COMD", "Comedy");
        genreMap.put("DOCU", "Documentary");
        genreMap.put("EPIC", "Epic");
        genreMap.put("MYST", "Mystery");
        genreMap.put("CMR", "Crime");
        genreMap.put("HOR", "Horror");
        genreMap.put("BIOPP", "Biographical Picture");
        genreMap.put("CTXX", "Context");
        genreMap.put("MUSCL", "Musical");
        genreMap.put("BIOPX", "Biographical Picture");
        genreMap.put("SCFI", "Science Fiction");
        genreMap.put("ALLEGORY", "Allegory");
        genreMap.put("RFP;", "Request for Proposal");
        genreMap.put("SURL", "Surreal");
        genreMap.put("DRAAM", "Drama");
        genreMap.put("COND", "Conduct");
        genreMap.put("GARDE", "Garde");
        genreMap.put("ADVT", "Adventure");
        genreMap.put("CART", "Cartoon");
        genreMap.put("BIOG", "Biographical");
        genreMap.put("ART", "Art");
        genreMap.put("BIOB", "Biographical");
        genreMap.put("CNR", "Crime");
        genreMap.put("WEIRD", "Weird");
        genreMap.put("H", "Horror");
        genreMap.put("FANTH*", "Fantasy Horror");
        genreMap.put("PORB", "Pornography");
        genreMap.put("SCIF", "Science Fiction");
        genreMap.put("NATU", "Nature");
        genreMap.put("SPORTS", "Sports");
        genreMap.put("TXX", "Text");
        genreMap.put("KINKY", "Kinky");
        genreMap.put("PORN", "Pornography");
        genreMap.put("CA", "California");
        genreMap.put("COMDX", "Comedy");
        genreMap.put("COL", "Color");
        genreMap.put("CULT", "Cult");
        genreMap.put("SATI", "Satire");
        genreMap.put("STAGE", "Stage");
        genreMap.put("PSYC", "Psychological");
        genreMap.put("ACT", "Action");
        genreMap.put("ANTI-DRAM", "Anti-Drama");
        genreMap.put("BIOP", "Biographical");
        genreMap.put("ROMT", "Romantic");
        genreMap.put("TV", "Television");
        genreMap.put("MYSTP", "Mystery");
        genreMap.put("VIDEO", "Video");
        genreMap.put("DIST", "Distortion");
        genreMap.put("WEST", "Western");
        genreMap.put("WEST1", "Western");
        genreMap.put("H**", "Horror");
        genreMap.put("BNW", "Black and White");
        genreMap.put("NOIR", "Film Noir");
        genreMap.put("CTXXX", "Context");
        genreMap.put("SURREAL", "Surreal");
        genreMap.put("ROMTADVT", "Romantic Adventure");
        genreMap.put("DICU", "Documentary");
        genreMap.put("ROMTX", "Romantic");
        genreMap.put("DUCO", "Documentary");
        genreMap.put("CAMP", "Camp");
        genreMap.put("DRAM.ACTN", "Drama Action");
        genreMap.put("DRAM>", "Drama");
        genreMap.put("ROMT.", "Romantic");
        genreMap.put("DRAMA", "Drama");
        genreMap.put("VERITE", "Verite");
        genreMap.put("CTCXX", "Context");
        genreMap.put("SCAT", "Scatological");
        genreMap.put("HORR", "Horror");
        genreMap.put("DRAMD", "Drama");
        genreMap.put("DRAMN", "Drama");
        genreMap.put("DISA", "Disaster");
        genreMap.put("ADCTX", "AdContext");
        genreMap.put("FAML", "Family");
        genreMap.put("UNDR", "Underground");
        genreMap.put("CNRB", "Crime");
        genreMap.put("HOMO", "Homosexual");
        genreMap.put("ADCT", "Adcontext");
        genreMap.put("CRIM", "Crime");
        genreMap.put("AVANT", "Avant");
        genreMap.put("SXFI", "Science Fiction");
        genreMap.put("EXPM", "Experimental");
        genreMap.put("AVGA", "Avant Garde");
        genreMap.put("MUUSC", "Music");
        genreMap.put("MUSC", "Music");
        genreMap.put("FANT", "Fantasy");
        genreMap.put("H*", "Horror");
        genreMap.put("DUCU", "Documentary");
        genreMap.put("RONT", "Ront");
        genreMap.put("H0", "Horror");
        genreMap.put("BIO", "Biographical");
        genreMap.put("SCTN", "Section");
        genreMap.put("TVMINI", "TV Mini-Series");
        genreMap.put("AXTN", "Action");
        genreMap.put("VIOL", "Violence");
        genreMap.put("RAM", "Romance");
    }

    private static final int THREAD_POOL_SIZE = 2; // Adjust the number of threads as needed
    private ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    DataXMLParser() {
        movieDataXMLParser = new MovieDataXMLParser(MOVIES_XML_PATH);
        actorsDataXMLParser = new ActorsDataXMLParser(ACTORS_XML_PATH);
    }

    private void parseMovieData() throws IOException {
        movieDataXMLParser.parseDocument();
        moviesHashMap = movieDataXMLParser.getMoviesHashMap();
        directorsHashMap = movieDataXMLParser.getDirectorsHashMap();
    }

    private void parseActorData() throws IOException {
        actorsDataXMLParser.parseDocument();
        actorsHashMap = actorsDataXMLParser.getActorsHashMap();
    }

    public void parseCastData() throws IOException {
        castsDataXMLParser = new CastsDataXMLParser(CASTS_XML_PATH,
                actorsDataXMLParser.getActorsNamesHashMap(), moviesHashMap,
                directorsHashMap);
        castsDataXMLParser.parseDocument();
    }


    private void insertDataIntoDatabase() {
//        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true",
//                "mytestuser", "My6$Password")) {
//            getExistingGenres(connection);
//            insertStars(connection);
//            insertMovies(connection);
//            insertGenres(connection);
//            insertStarsInMovies(connection);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true",
                "mytestuser", "My6$Password")) {
            getExistingGenres(connection);

            // Create tasks for insertStars and insertMovies
            Runnable insertStarsTask = () -> {
                try {
                    insertStars(connection);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            };
            Runnable insertMoviesTask = () -> {
                try {
                    insertMovies(connection);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            };

            // Submit tasks to the executor service
            executorService.submit(insertStarsTask);
            executorService.submit(insertMoviesTask);

            // Wait for the tasks to complete
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            // Now execute insertGenres and insertStarsInMovies concurrently
            executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            Runnable insertGenresTask = () -> {
                try {
                    insertGenres(connection);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            };
            Runnable insertStarsInMoviesTask = () -> {
                try {
                    insertStarsInMovies(connection);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            };

            executorService.submit(insertGenresTask);
            executorService.submit(insertStarsInMoviesTask);

            // Wait for the tasks to complete
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getExistingGenres(Connection connection) throws SQLException {
        String getAllGenresQuery = queryBuilder.getAllGenresFromGenreTableQuery();
        PreparedStatement getGenresStatement = connection.prepareStatement(getAllGenresQuery);
        ResultSet genresInDB = getGenresStatement.executeQuery();

        while (genresInDB.next()) {
            existingGenres.put(genresInDB.getString("name"), genresInDB.getInt("id"));
        }
        genresInDB.close();
        getGenresStatement.close();
    }

    private void insertStars(Connection connection) throws SQLException {
        String insertStarQuery = queryBuilder.getAddStarToDB();
//        "INSERT INTO stars (id, name, birthYear) " +
//                "VALUES ( ? , ? , ? ) ";
        int batchSize = 0;
        PreparedStatement insertStarStatement = connection.prepareStatement(insertStarQuery);
        for (Map.Entry<String, Actor> actorEntry : actorsHashMap.entrySet()) {
            Actor actor = actorEntry.getValue();
            String actorId = actorEntry.getKey();

            insertStarStatement.clearParameters();
            insertStarStatement.setString(1, actorId);
            insertStarStatement.setString(2, actor.getActorName());
            if (actor.getActorDateOfBirthAsString() != null) {
                insertStarStatement.setInt(3, actor.getActorDateOfBirth());
            } else {
                insertStarStatement.setNull(3, Types.INTEGER);
            }
            insertStarStatement.addBatch();
            batchSize += 1;
            if (batchSize == MAX_BATCH_SIZE) {
                insertStarStatement.executeBatch();
                batchSize = 0;
            }
            numberOfStarsInserted += 1;
        }
        insertStarStatement.executeBatch();
        insertStarStatement.close();
    }

    private void insertMovies(Connection connection) throws SQLException {
        String insertMovieQuery = queryBuilder.getAddMovieToDBViaInsertQuery();
//        "INSERT INTO movies (id, title, year, director, price) " +
//                "VALUES ( ? , ? , ? , ? , ? ) ";
        int batchSize = 0;
        PreparedStatement insertMovieStatement = connection.prepareStatement(insertMovieQuery);
        for (Map.Entry<String, Movie> movieEntry : moviesHashMap.entrySet()) {
            Movie movie = movieEntry.getValue();
            String movieId = movieEntry.getKey();
            if (movie.getMovieActors().isEmpty()) {
                numberOfMoviesWithNoStars += 1;
            }
            insertMovieStatement.clearParameters();
            insertMovieStatement.setString(1, movieId);
            insertMovieStatement.setString(2, movie.getMovieTitle());
            insertMovieStatement.setInt(3, movie.getMovieYear()); // should be correct always since only valid movies are parsed
            insertMovieStatement.setString(4, movie.getMovieDirector().getDirectorName());
            insertMovieStatement.setInt(5, movie.getMoviePrice());
            insertMovieStatement.addBatch();
            batchSize += 1;
            numberOfMoviesInserted += 1;
            if (batchSize == MAX_BATCH_SIZE) {
                insertMovieStatement.executeBatch();
                batchSize = 0;
            }
        }
        insertMovieStatement.executeBatch();
        insertMovieStatement.close();
    }

    private void insertGenres(Connection connection) throws SQLException {
//      addGenreToDB = "INSERT INTO genres (name) " +
//                        "VALUES ( ? ) ";
//      addToGenresInMovies =
//                "INSERT INTO genres_in_movies (genreId, movieId) " +
//                        "VALUES ( ? , ? ) ";
        String addNewGenreToDBQuery = queryBuilder.getAddGenreToDB();
        String addGenreToGenresInMoviesQuery = queryBuilder.getAddGenreInMovieToDB();

        PreparedStatement insertNewGenre = connection.prepareStatement(addNewGenreToDBQuery);
        PreparedStatement insertGenreInGenresInMovies = connection.prepareStatement(addGenreToGenresInMoviesQuery);

        for (Map.Entry<String, Movie> movieEntry : moviesHashMap.entrySet()) {
            Movie movie = movieEntry.getValue();
            String movieId = movieEntry.getKey();
            for (String genre : movie.getMovieGenres()) {
                if (!existingGenres.containsKey(genreMap.get(genre))) {
                    existingGenres.put(genreMap.get(genre), Collections.max(existingGenres.values()) + 1);
                    insertNewGenre.clearParameters();
                    insertNewGenre.setString(1, genreMap.get(genre));
                    insertNewGenre.executeUpdate();
                    numberOfGenresInserted += 1;
                }
                insertGenreInGenresInMovies.clearParameters();
                insertGenreInGenresInMovies.setInt(1, existingGenres.get(genreMap.get(genre)));
                insertGenreInGenresInMovies.setString(2, movie.getMovieId());
                insertGenreInGenresInMovies.addBatch();
                numberOfGenresInMoviesInserted += 1;
            }
            insertGenreInGenresInMovies.executeBatch();
        }
        insertNewGenre.close();
        insertGenreInGenresInMovies.close();
    }

    private void insertStarsInMovies(Connection connection) throws SQLException {
        String insertStarInMoviesQuery = queryBuilder.getAddStarToStarsInMovies();
        PreparedStatement insertStarInMoviesStatement = connection.prepareStatement(insertStarInMoviesQuery);
        for (Map.Entry<String, Movie> movieEntry : moviesHashMap.entrySet()) {
            Movie movie = movieEntry.getValue();
            String movieId = movieEntry.getKey();
            for (Map.Entry<String, Actor> actorEntry : movie.getMovieActors().entrySet()) {
                String actorId = actorEntry.getKey();
                Actor actor = actorEntry.getValue();
                insertStarInMoviesStatement.clearParameters();
                insertStarInMoviesStatement.setString(1, actorId);
                insertStarInMoviesStatement.setString(2, movieId);
                insertStarInMoviesStatement.addBatch();
                numberOfStarsInMoviesInserted += 1;
            }
            insertStarInMoviesStatement.executeBatch();
        }
        insertStarInMoviesStatement.close();
    }

    private void printErrorReport() {
        System.out.println("Inserted " + numberOfStarsInserted + " stars");
        System.out.println("Inserted " + numberOfGenresInserted + " genres");
        System.out.println("Inserted " + numberOfMoviesInserted + " movies");
        System.out.println("Inserted " + numberOfGenresInMoviesInserted + " genres in movies");
        System.out.println("Inserted " + numberOfStarsInMoviesInserted + " stars in movies");
        System.out.println(movieDataXMLParser.getNumberOfInvalidMovies() + " movies inconsistent");
        System.out.println(movieDataXMLParser.getNumberRepeatedMovies() + " movies duplicate");
        System.out.println(movieDataXMLParser.getNumberMoviesWithNoGenres() + " movies have no genres");
        System.out.println(numberOfMoviesWithNoStars + " movies have no stars");
        System.out.println(castsDataXMLParser.getNumberDirectorsNotFound() + " directors not found");
        System.out.println(castsDataXMLParser.getNumberMoviesNotFound() + " movies not found");
        System.out.println(castsDataXMLParser.getNumberActorsNotFound() + " stars not found");
        System.out.println(actorsDataXMLParser.getNumberRepeatedActors() + " stars duplicate");
    }

    public static void main(String[] args) throws IOException {
        DataXMLParser parser = new DataXMLParser();
        final long startTime = System.currentTimeMillis();
        parser.parseMovieData();
        parser.parseActorData();
        parser.parseCastData();
        final long endParsingTime = System.currentTimeMillis();
        System.out.println("Total time for PARSING (in ms): " + (endParsingTime - startTime));
        final long startInsertionTime = System.currentTimeMillis();
        parser.insertDataIntoDatabase();
        final long endTime = System.currentTimeMillis();
        System.out.println("Total time for INSERTIONS (in ms): " + (endTime - startInsertionTime));
        System.out.println("Total time for EVERYTHING (in ms): " + (endTime - startTime));
        parser.printErrorReport();
    }
}