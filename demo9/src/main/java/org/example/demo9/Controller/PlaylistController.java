package org.example.demo9.Controller;

import org.example.demo9.Model.song.Playlist;
import org.example.demo9.Model.song.PlaylistManager;
import org.example.demo9.Model.song.Song;
import org.example.demo9.Model.util.CSVLoader;

import java.io.IOException;
import java.util.List;

public class PlaylistController {
    private PlaylistManager manager;

    public PlaylistController(PlaylistManager manager) {
        this.manager = manager;
    }

    public void loadSongsFromCSV(String filePath, String targetPlaylist) throws IOException {
        List<Song> songs = CSVLoader.loadSongs(filePath);
        Playlist p = manager.createPlaylist(targetPlaylist);
        for (Song s : songs) p.addSong(s);
    }

    public void handleCreatePlaylist(String name) {
        manager.createPlaylist(name);
    }

    public void handleAddSong(String playlistName, Song song) {
        Playlist p = manager.getPlaylist(playlistName);
        if (p != null) p.addSong(song);
    }

    public void handleMerge(String p1, String p2, String newName) {
        manager.mergePlaylists(p1, p2, newName);
    }

    public void handleShuffleMerge(List<String> names, String newName) {
        manager.shuffleMergePlaylists(names, newName);
    }

    public Playlist handleFilter(String playlistName, String criteria, String value) {
        Playlist p = manager.getPlaylist(playlistName);
        if (p == null) return null;
        return p.filterBy(criteria, value);
    }

    public void handleSort(String playlistName, String criteria) {
        Playlist p = manager.getPlaylist(playlistName);
        if (p != null) p.sortBy(criteria);
    }

    public void handlePlay(String playlistName, boolean shuffle) {
        Playlist p = manager.getPlaylist(playlistName);
        if (p == null) {
            System.out.println("Playlist not found");
            return;
        }
        if (shuffle) p.shufflePlay();
        else p.play();
    }

    public void handleLike(String playlistName, String trackName) {
        Playlist p = manager.getPlaylist(playlistName);
        if (p != null) p.likeSong(trackName);
    }

    public void handleUnlike(String playlistName, String trackName) {
        Playlist p = manager.getPlaylist(playlistName);
        if (p != null) p.unlikeSong(trackName);
    }
}

