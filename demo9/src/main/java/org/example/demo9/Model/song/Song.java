package org.example.demo9.Model.song;

import java.util.Objects;

public class Song {
    private String artistName;
    private String trackName;
    private String releaseDate;
    private String genre;
    private double length;
    private String topic;
    private boolean liked;

    public Song(String artistName, String trackName, String releaseDate, String genre, double length, String topic) {
        this.artistName = artistName;
        this.trackName = trackName;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.length = length;
        this.topic = topic;
        this.liked = false;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getReleaseDate() {
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

    @Override
    public String toString() {
        return "🎵 " + trackName + " by " + artistName + " (" + releaseDate + "), Genre: " + genre + ", Length: " + length + "s, Topic: " + topic;
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
