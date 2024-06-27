import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class MovieDataXMLParser extends DefaultHandler {
    private LinkedHashMap<String, Director> directorsHashMap = new LinkedHashMap<>();
    private LinkedHashMap<String, Movie> moviesHashMap = new LinkedHashMap<>();
    private HashSet<String> movieIdentifiers = new HashSet<>();
    private final String moviesXMLFileLocation;
    private String currentElement;
    private String tempValue;
    Director currentDirectorWhileParsing;
    Movie currentMovieWhileParsing;
    boolean directorNameSet = false;
    private int numberMoviesWithNoGenres = 0;
    private int numberRepeatedMovies = 0;
    private int numberOfInvalidMovies = 0;

    MovieDataXMLParser(String moviesXMLFileLocation) {
        this.moviesXMLFileLocation = moviesXMLFileLocation;
    }

    public int getNumberMoviesWithNoGenres() {
        return numberMoviesWithNoGenres;
    }

    public int getNumberRepeatedMovies() {
        return numberRepeatedMovies;
    }

    public int getNumberOfInvalidMovies() {
        return numberOfInvalidMovies;
    }

    public void parseDocument() throws IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            //get a new instance of parser
            SAXParser saxParser = spf.newSAXParser();

            //parse the file and also register this class for call backs
            saxParser.parse(moviesXMLFileLocation, this);
            writeMovieParsingResults();

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = qName;
        if (currentElement.equalsIgnoreCase("film")) {
            currentMovieWhileParsing = new Movie();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempValue = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("director")) {
            directorsHashMap.put(currentDirectorWhileParsing.getDirectorId(), currentDirectorWhileParsing);
        } else if (qName.equalsIgnoreCase("film")) {
            currentMovieWhileParsing.setMovieDirector(currentDirectorWhileParsing);
            if (checkIfValidMovie(currentMovieWhileParsing)) {
                String movieIdentifier = createMovieIdentifier(currentMovieWhileParsing);
                if (!movieIdentifiers.add(movieIdentifier)) { // if the movie already exists
//                    System.out.println("Repeated Film! Details: " + currentMovieWhileParsing);
                    numberRepeatedMovies += 1;
                } else { // else insert the movie in
                    currentDirectorWhileParsing.addMovie(currentMovieWhileParsing);
                    moviesHashMap.put(currentMovieWhileParsing.getMovieId(), currentMovieWhileParsing);
                    if (currentMovieWhileParsing.getMovieGenres().isEmpty()) {
                        numberMoviesWithNoGenres += 1;
                    }
                }
            } else {
//                System.out.println("Invalid film! Details: " + currentMovieWhileParsing);
                numberOfInvalidMovies += 1;
            }
        } else if (qName.equalsIgnoreCase("dirname") || qName.equalsIgnoreCase("dirn")) {
            if (!directorNameSet) {
                currentDirectorWhileParsing.setDirectorName(tempValue);
                directorNameSet = true;
            }
        } else if (qName.equalsIgnoreCase("dirid")) {
            if (!directorsHashMap.containsKey(tempValue)) {
                currentDirectorWhileParsing = new Director();
                currentDirectorWhileParsing.setDirectorId(tempValue);
                directorNameSet = false;
            } else {
                currentDirectorWhileParsing = directorsHashMap.get(tempValue); // change the director back if coming back to a previous one
                directorNameSet = true;
            }
        } else if (qName.equalsIgnoreCase("fid") ||
                qName.equalsIgnoreCase("filmed")) { // "filmed" seems to be another tag for "fid" which is filmid
            if (tempValue.isBlank()) {
                currentMovieWhileParsing.setMovieId(tempValue);
            } else {
                String movieId = padIdWithZeros(tempValue);
                currentMovieWhileParsing.setMovieId(movieId);
            }
        } else if (qName.equalsIgnoreCase("t")) {
            currentMovieWhileParsing.setMovieTitle(tempValue);
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                currentMovieWhileParsing.setMovieYear(tempValue);
            } catch (Exception e) {
                printError();
            }
        } else if (qName.equalsIgnoreCase("cat")) {
            String capitalized = tempValue.strip().toUpperCase();
            String[] potentialGenres = capitalized.split(" ");
            for (String genre : potentialGenres) {
                if (!genre.isBlank()) {
                    currentMovieWhileParsing.addGenreToMovie(genre);
                }
            }
        }
    }

    public LinkedHashMap<String, Movie> getMoviesHashMap() {
        return moviesHashMap;
    }

    public LinkedHashMap<String, Director> getDirectorsHashMap() {
        return directorsHashMap;
    }

    public HashSet<String> getMovieIdentifiers() {
        return movieIdentifiers;
    }

    private boolean checkIfValidMovie(Movie movie) {
        return (movie.getMovieYearAsString() != null &&
                movie.getMovieId() != null && !movie.getMovieId().isBlank() &&
                movie.getMovieTitle() != null &&
                movie.getMovieDirector() != null);
    }

    private void printError() {
        System.out.printf("Error at element name %s, with value %s\n", currentElement, tempValue);
    }

    private void printMovieParsingResults() {
        int totalMovies = 0;
        for (final Map.Entry<String, Director> entry : directorsHashMap.entrySet()) {
            Director director = entry.getValue();
            System.out.println(director);
            totalMovies += director.getMoviesDirectedByDirector().size();
        }
        System.out.println("Total movies found are: " + totalMovies);
    }

    private String createMovieIdentifier(Movie movie) {
        return movie.getMovieTitle() + movie.getMovieYearAsString() + movie.getMovieDirector().getDirectorName();
    }

    private String padIdWithZeros(String idToPad) {
        int targetLength = 10;
        // Calculate the number of zeros to pad
        int zerosToPad = targetLength - idToPad.length();
        // Create a StringBuilder to efficiently build the padded string
        StringBuilder paddedString = new StringBuilder();

        // Append zeros to the StringBuilder
        for (int i = 0; i < zerosToPad; i++) {
            paddedString.append('0');
        }
        // Append the original input to the StringBuilder
        paddedString.append(idToPad);
        // Convert StringBuilder to String and return
        return paddedString.toString();
    }

    private void writeMovieParsingResults() throws IOException {
        try {
            File outputFile = new File("stanford-movies/movieParsingResults.txt");

            PrintWriter writer = new PrintWriter(outputFile);
            for (final Map.Entry<String, Director> entry : directorsHashMap.entrySet()) {
                Director director = entry.getValue();
                writer.println(director);
                for (final Movie movie : director.getMoviesDirectedByDirector().values()) {
                    writer.println(movie);
                }
                writer.flush();
            }
            writer.println("Total number of movies is: " + moviesHashMap.size());
            writer.println("Total movies with no genre is: " + numberMoviesWithNoGenres);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}