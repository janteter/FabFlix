import java.util.LinkedHashMap;

public class Director {
    //    <directorfilms><director><dirid>H</dirid><dirstart>@1922</dirstart><dirname>Hitchcock</dirname><coverage>all
    //    early British</coverage></director><films>
    private String directorName;

    private String directorId;

    private LinkedHashMap<String, Movie> moviesDirectedByDirector = new LinkedHashMap<>();

    public String getDirectorName() {
        return directorName;
    }

    public void setDirectorName(String name) {
        this.directorName = name;
    }

    public void setDirectorId(String id) {
        this.directorId = id;
    }

    public void addMovie(Movie movieToAdd) {
        moviesDirectedByDirector.put(movieToAdd.getMovieId(), movieToAdd);
    }

    public String getDirectorId() {
        return directorId;
    }

    public LinkedHashMap<String, Movie> getMoviesDirectedByDirector() {
        return moviesDirectedByDirector;
    }

    public void printMoviesFromDirector() {
        for (Movie movie : moviesDirectedByDirector.values()) {
            System.out.println(movie);
        }
    }

    @Override
    public String toString() {
        return String.format("DIRECTOR - Name: %s, Id: %s, Num movies %d", directorName, directorId, moviesDirectedByDirector.size());
    }
}