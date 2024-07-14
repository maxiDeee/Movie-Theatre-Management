// -----------------------------------------------------
// Assignment 2
// Question:
// Written by: Hongyu Chen  40070191
// The Movie class implements the Serializable interface, enabling instances of this class to be serialized,
// converted into a stream of bytes for storage or transmission to another part of a system.
// It provides constructors to initialize the attributes, getter and setter methods to access and modify them,
// and overrides the equals and toString methods from the Object class for comparison and string representation.
// -----------------------------------------------------

import java.util.Objects;
import java.io.Serializable;

public class Movie implements Serializable{
    int year;
    String title;
    int duration;
    String genres;
    String rating;
    double score;
    String director;
    String actor1;
    String actor2;
    String actor3;

    public Movie(int year, String title, int duration, String genres, String rating, double score,
                 String director, String actor1, String actor2, String actor3) {
        this.year = year;
        this.title = title;
        this.duration = duration;
        this.genres = genres;
        this.rating = rating;
        this.score = score;
        this.director = director;
        this.actor1 = actor1;
        this.actor2 = actor2;
        this.actor3 = actor3;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActor1() {
        return actor1;
    }

    public void setActor1(String actor1) {
        this.actor1 = actor1;
    }

    public String getActor2() {
        return actor2;
    }

    public void setActor2(String actor2) {
        this.actor2 = actor2;
    }

    public String getActor3() {
        return actor3;
    }

    public void setActor3(String actor3) {
        this.actor3 = actor3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return year == movie.year && duration == movie.duration && score == movie.score && Objects.equals(title, movie.title) && Objects.equals(genres, movie.genres) && Objects.equals(rating, movie.rating) && Objects.equals(director, movie.director) && Objects.equals(actor1, movie.actor1) && Objects.equals(actor2, movie.actor2) && Objects.equals(actor3, movie.actor3);
    }

    @Override
    public String toString() {
        return "Movie{" +
                "year=" + year +
                ", title='" + title + '\'' +
                ", duration=" + duration +
                ", genres='" + genres + '\'' +
                ", rating='" + rating + '\'' +
                ", score=" + score +
                ", director='" + director + '\'' +
                ", actor1='" + actor1 + '\'' +
                ", actor2='" + actor2 + '\'' +
                ", actor3='" + actor3 + '\'' +
                '}';
    }
}
