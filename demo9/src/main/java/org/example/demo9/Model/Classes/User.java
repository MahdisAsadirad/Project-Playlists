package org.example.demo9.Model.Classes;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private List<Playlist> playlists;
    private Playlist likedSongs;

    public User(int id, String username) {
        this.id = id;
        this.username = username;
        this.playlists = new ArrayList<>();
        this.likedSongs = new Playlist("Liked Songs");
    }

    public void createPlaylist(String name) {
        playlists.add(new Playlist(name));
    }

    public Playlist getPlaylist(String name) {
        for (Playlist playlist : playlists) {
            if (playlist.getName().equals(name)) {
                return playlist;
            }
        }
        return null;
    }

    public void addToLikedSongs(SongNode song) {
        likedSongs.addSong(song);
    }

    public void removeFromLikedSongs(String trackName) {
        likedSongs.removeSong(trackName);
    }

    // Getter methods
    public int getId() { return id; }
    public String getUsername() { return username; }
    public List<Playlist> getPlaylists() { return playlists; }
    public Playlist getLikedSongs() { return likedSongs; }
}