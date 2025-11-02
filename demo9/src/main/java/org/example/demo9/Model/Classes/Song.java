package org.example.demo9.Model.Classes;

public class Song {
    private String artistName;
    private String trackName;
    private int releaseDate;
    private String genre;
    private double len;
    private String topic;
    private Song next;

    public Song(String artistName, String trackName, int releaseDate,
                String genre, double len, String topic) {
        this.artistName = artistName;
        this.trackName = trackName;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.len = len;
        this.topic = topic;
        this.next = null;
    }
    
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public String getTrackName() { return trackName; }
    public void setTrackName(String trackName) { this.trackName = trackName; }

    public int getReleaseDate() { return releaseDate; }
    public void setReleaseDate(int releaseDate) { this.releaseDate = releaseDate; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public double getLen() { return len; }
    public void setLen(double len) { this.len = len; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Song getNext() { return next; }
    public void setNext(Song next) { this.next = next; }

    @Override
    public String toString() {
        return String.format("%s - %s (%d) [%s, %.2fs, %s]",
                artistName, trackName, releaseDate, genre, len, topic);
    }
}