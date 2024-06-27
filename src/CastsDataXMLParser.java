import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class CastsDataXMLParser extends DefaultHandler {
    private LinkedHashMap<String, Actor> actorsHashMap;
    private LinkedHashMap<String, Movie> moviesHashMap;
    private LinkedHashMap<String, Director> directorsHashMap;
    private final String castsXMLFileLocation;
    private String currentElement;
    private String tempValue;
    boolean foundMovie = false;
    boolean foundDirector = false;
    Actor currentActorWhileParsing;
    Director currentDirectorWhileParsing;
    Movie currentMovieWhileParsing;
    private int numberDirectorsNotFound = 0;
    private int numberActorsNotFound = 0;
    private int numberMoviesNotFound = 0;

    CastsDataXMLParser(String castsXMLFileLocation, LinkedHashMap<String, Actor> actors,
                       LinkedHashMap<String, Movie> movies, LinkedHashMap<String, Director> directors) {
        actorsHashMap = actors;
        moviesHashMap = movies;
        directorsHashMap = directors;
        this.castsXMLFileLocation = castsXMLFileLocation;
    }

    public int getNumberActorsNotFound() {
        return numberActorsNotFound;
    }

    public int getNumberMoviesNotFound() {
        return numberMoviesNotFound;
    }

    public int getNumberDirectorsNotFound() {
        return numberDirectorsNotFound;
    }

    public void parseDocument() throws IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            //get a new instance of parser
            SAXParser saxParser = spf.newSAXParser();

            //parse the file and also register this class for call backs
            saxParser.parse(castsXMLFileLocation, this);
            writeCastParsingResults();

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
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempValue = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("dirid")) {
            String directorId = tempValue;
            if (directorsHashMap.containsKey(directorId)) {
                currentDirectorWhileParsing = directorsHashMap.get(directorId);
                foundDirector = true;
            } else {
//                System.out.println("Director with id " + directorId + " not found!");
                foundDirector = false;
                numberDirectorsNotFound += 1;
            }
        } else if (qName.equalsIgnoreCase("f")) {
            if (foundDirector) {
                String movieId = padIdWithZeros(tempValue);
                if (moviesHashMap.containsKey(movieId)) {
                    currentMovieWhileParsing = moviesHashMap.get(movieId);
                    foundMovie = true;
                } else {
//                    System.out.println("Movie with id " + tempValue + " not found!");
                    foundMovie = false;
                    numberMoviesNotFound += 1;
                }
            }
        } else if (qName.equalsIgnoreCase("a")) {
            if (foundMovie) {
                String actorName = tempValue;
                if (actorsHashMap.containsKey(actorName)) {
                    currentMovieWhileParsing.addActor(actorsHashMap.get(actorName));
                } else {
//                    System.out.println("Actor with name " + tempValue + " not found!");
                    numberActorsNotFound += 1;
                }
            }
        }
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

    public void writeCastParsingResults() {
        try {
            File outputFile = new File("stanford-movies/castsParsingResults.txt");

            PrintWriter writer = new PrintWriter(outputFile);
            for (Map.Entry<String, Movie> movieEntry : moviesHashMap.entrySet()) {
                Movie movie = movieEntry.getValue();
                if (!movie.getMovieActors().isEmpty()) {
                    writer.println(movie);
                    for (Map.Entry<String, Actor> actorEntry : movie.getMovieActors().entrySet()) {
                        Actor actor = actorEntry.getValue();
                        writer.println(actor);
                    }
                }
                writer.flush();
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}