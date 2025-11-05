package org.example.demo9.Model.Classes;

public class SongNode {
    private int songId;
    private String artistName;
    private String trackName;
    private int releaseDate;
    private String genre;
    private double length;
    private String topic;
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

    public void setLen(double length) {
        this.length = length;
    }

    public void setSongId(int songId) {
        this.songId = songId;
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

    public void setTopic(String topic) {
        this.topic = topic;
    }
}