package org.example.demo9.Model.Classes;

public class SongNode {
    private String artistName;
    private String trackName;
    private int releaseDate;
    private String genre;
    private double len;
    private String topic;
    private SongNode next;

    public SongNode(String artistName, String trackName, int releaseDate,
                    String genre, double len, String topic) {
        this.artistName = artistName;
        this.trackName = trackName;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.len = len;
        this.topic = topic;
        this.next = null;
    }

    // Getter and Setter methods
    public String getArtistName() { return artistName; }
    public String getTrackName() { return trackName; }
    public int getReleaseDate() { return releaseDate; }
    public String getGenre() { return genre; }
    public double getLen() { return len; }
    public String getTopic() { return topic; }
    public SongNode getNext() { return next; }
    public void setNext(SongNode next) { this.next = next; }

    @Override
    public String toString() {
        return String.format("%s - %s (%d) [%s, %.2fs, %s]",
                artistName, trackName, releaseDate, genre, len, topic);
    }
}