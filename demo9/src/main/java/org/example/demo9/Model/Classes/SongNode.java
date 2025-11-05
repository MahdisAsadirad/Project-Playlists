package org.example.demo9.Model.Classes;

public class SongNode {
    private final int songId;
    private final String artistName;
    private final String trackName;
    private final int releaseDate;
    private final String genre;
    private final double length;
    private final String topic;
    private SongNode next;

    public SongNode(int songId, String artistName, String trackName, int releaseDate, String genre, double length, String topic) {
        this.songId = songId;
        this.artistName = artistName;
        this.trackName = trackName;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.length = length;
        this.topic = topic;
        this.next = null;
    }

    public SongNode(SongNode other) {
        this.songId = other.songId;
        this.artistName = other.artistName;
        this.trackName = other.trackName;
        this.releaseDate = other.releaseDate;
        this.genre = other.genre;
        this.length = other.length;
        this.topic = other.topic;
        this.next = null;
    }

    public int getSongId() { return songId; }
    public String getArtistName() { return artistName; }
    public String getTrackName() { return trackName; }
    public int getReleaseDate() { return releaseDate; }
    public String getGenre() { return genre; }
    public double getLen() { return length; }
    public SongNode getNext() { return next; }
    public String getTopic() { return topic; }
    public void setNext(SongNode next) { this.next = next; }

    @Override
    public String toString() {
        return String.format("%s - %s (%d)", artistName, trackName, releaseDate);
    }
}