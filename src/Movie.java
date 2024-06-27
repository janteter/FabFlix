import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Movie {
    // <film><fid>H1</fid>
    // <t>Always Tell Your Wife</t>
    // <year>1922</year>
    // <dirs><dir><dirk>R</dirk><dirn>Se.Hicks</dirn></dir><dir><dirk>R</dirk><dirn>Hitchcock</dirn></dir></dirs>
    // <prods><prod><prodk>R</prodk><pname>Lasky</pname></prod></prods>
    // <studios><studio>Famous</studio></studios><prcs><prc>sbw</prc></prcs>
    // <cats><cat>Dram</cat></cats>
    // <awards></awards> <loc></loc><notes/></film>
    private String movieId;
    private String movieTitle;
    private int movieYear = -1; // Default to -1 to show invalid
    private String movieYearAsString;
    private Director movieDirector;
    private ArrayList<String> movieGenres = new ArrayList<>();
    private HashMap<String, Actor> movieActors = new HashMap<>();
    private int price; // = something randomized (could do this after parsing XML and then insert or could do it after via SQL in cmd)

    Movie() {
        this.price = new Random().nextInt(91) + 10; // price b/w 10 to 100
    }

    public void setMovieId(String id) {
        this.movieId = id;
    }

    public void setMovieDirector(Director director) {
        this.movieDirector = director;
    }

    public void setMovieTitle(String title) {
        this.movieTitle = title;
    }

    public void setMovieYear(String year) {
        try {
            this.movieYear = Integer.parseInt(year);
            this.movieYearAsString = year;
        } catch (Exception e) {
            this.movieYearAsString = null;
            throw e;
        }
    }

    public ArrayList<String> getMovieGenres() { return movieGenres; }
    public String getMovieId() { return movieId; }
    public String getMovieTitle() {
        return movieTitle;
    }
    public String getMovieYearAsString() {
        return movieYearAsString;
    }
    public int getMovieYear() {
        return movieYear;
    }
    public Director getMovieDirector() { return movieDirector; }
    public int getMoviePrice() { return price; }
    public HashMap<String, Actor> getMovieActors() { return movieActors; }

    public void addActor(Actor actorToAdd) {
        this.movieActors.put(actorToAdd.getActorId(), actorToAdd);
    }
    public void addGenreToMovie(String genreName) {
        this.movieGenres.add(genreName);
    }

    @Override
    public String toString() {
        String genres = String.join(", ", movieGenres);
        return String.format("MOVIE - Movie ID: %s, Title: %s, Year: %s, Genres: %s", movieId, movieTitle, movieYearAsString, genres);
    }
}