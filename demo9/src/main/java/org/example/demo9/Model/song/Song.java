package org.example.demo9.Model.song;

import java.util.Objects;

public class Song {
    private int id;
    private String artistName;
    private String trackName;
    private int releaseDate;
    private String genre;
    private double length;
    private String topic;
    private boolean liked;

    public Song(int id, String artistName, String trackName, int releaseDate, String genre, double length, String topic) {
        this.id=id;
        this.artistName = artistName;
        this.trackName = trackName;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.length = length;
        this.topic = topic;
        this.liked = false;
    }

    public int getId() {
        return id;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getTrackName() {
        return trackName;
    }

    public int getReleaseDate() {
        return releaseDate;
    }

    public String getGenre() {
        return genre;
    }

    public double getLength() {
        return length;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public void setReleaseDate(int releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
    @Override
    public String toString() {
        return trackName + ",    by " + artistName + " (" + releaseDate + "),     Genre: " + genre + ",   Length: " + length + "s,    Topic: " + topic;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;
        Song song = (Song) o;
        return trackName.equalsIgnoreCase(song.trackName)
                && artistName.equalsIgnoreCase(song.artistName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackName.toLowerCase(), artistName.toLowerCase());
    }

}
