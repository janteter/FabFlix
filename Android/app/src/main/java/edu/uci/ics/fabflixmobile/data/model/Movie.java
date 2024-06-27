package edu.uci.ics.fabflixmobile.data.model;

import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private String name;
    private String id;
    private String year;
    private String director;
    private ArrayList<String> genres;
    private ArrayList<String> stars;

    public Movie(String name, String year) {
        this.name = name;
        this.year = year;
    }

    public Movie(String name, String year, String director, ArrayList<String> genres, ArrayList<String> stars, String id) {
        this.name = name;
        this.year = year;
        this.director = director;
        this.genres = genres;
        this.stars = stars;
        this.id = id;
    }

    public Movie() {

    }

    public void setName(String name) { this.name = name; }
    public void setYear(String year) { this.name = year; }
    public void setDirector(String director) { this.name = director; }
    public void setGenres(ArrayList<String> genres) { this.genres = genres; }
    public void setStars(ArrayList<String> stars) { this.stars = stars; }
    public void setId(String id) { this.id = id; }


    public String getName() {
        return name;
    }

    public String getYear() {
        return year;
    }

    public String getDirector() { return director; }

    public ArrayList<String> getGenres() { return genres; }

    public ArrayList<String> getStars() { return stars; }
    public String getId() { return id; }
}