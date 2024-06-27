import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;

public class ActorsDataXMLParser extends DefaultHandler {
    private LinkedHashMap<String, Actor> actorsHashMap = new LinkedHashMap<>(); // id -> Actor
    private LinkedHashMap<String, Actor> actorsNamesHashMap = new LinkedHashMap<>(); // name -> list of Actors with the same name
    private final String actorsXMLFileLocation;
    private String currentElement;
    private String tempValue;
    Actor currentActorWhileParsing;
    int numberRepeatedActors = 0;
    int numberActorsWithNullBirthYear = 0;

    ActorsDataXMLParser(String actorsXMLFileLocation) {
        this.actorsXMLFileLocation = actorsXMLFileLocation;
    }

    public void parseDocument() throws IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            //get a new instance of parser
            SAXParser saxParser = spf.newSAXParser();

            //parse the file and also register this class for call backs
            saxParser.parse(actorsXMLFileLocation, this);
            writeActorParsingResults();

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

    public int getNumberRepeatedActors() {
        return numberRepeatedActors;
    }

    public int getNumberActorsWithNullBirthYear() {
        return numberActorsWithNullBirthYear;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = qName;
        if (currentElement.equalsIgnoreCase("actor")) {
            currentActorWhileParsing = new Actor();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempValue = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("stagename")) {
            currentActorWhileParsing.setActorName(tempValue);
        } else if (qName.equalsIgnoreCase("dob")) {
            try {
                currentActorWhileParsing.setActorDateOfBirth(tempValue);
            } catch (Exception e) {
                printError();
                numberActorsWithNullBirthYear += 1;
            }
        } else if (qName.equalsIgnoreCase("actor")) {
            if (actorsNamesHashMap.containsKey(currentActorWhileParsing.getActorName())) {
//                System.out.println("Repeated actor with name " + currentActorWhileParsing.getActorName());
                numberRepeatedActors += 1;
            } else {
                currentActorWhileParsing.generateActorId();
                actorsHashMap.put(currentActorWhileParsing.getActorId(), currentActorWhileParsing);
                actorsNamesHashMap.put(currentActorWhileParsing.getActorName(), currentActorWhileParsing);
            }
        }
    }
    public LinkedHashMap<String, Actor> getActorsHashMap() {
        return actorsHashMap;
    }

    public LinkedHashMap<String, Actor> getActorsNamesHashMap() {
        return actorsNamesHashMap;
    }

    private void printError() {
        System.out.printf("Error at element name %s, with value %s\n", currentElement, tempValue);
    }

    private void printActorParsingResults() {
        for (final Map.Entry<String, Actor> entry : actorsHashMap.entrySet()) {
            Actor actor = entry.getValue();
            System.out.println(actor);
        }
    }

    private void writeActorParsingResults() throws IOException {
        try {
            File outputFile = new File("stanford-movies/actorParsingResults.txt");

            PrintWriter writer = new PrintWriter(outputFile);
            for (final Map.Entry<String, Actor> entry : actorsHashMap.entrySet()) {
                Actor actor = entry.getValue();
                writer.println(actor);
                writer.flush();
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}