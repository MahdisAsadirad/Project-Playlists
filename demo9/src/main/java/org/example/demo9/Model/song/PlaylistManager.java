package org.example.demo9.Model.song;

import java.util.*;

public class PlaylistManager {
    private Map<String, Playlist> playlists = new HashMap<>();

    public Playlist createPlaylist(String name) {
        if (playlists.containsKey(name)) return playlists.get(name);
        Playlist p = new Playlist(name);
        playlists.put(name, p);
        return p;
    }

    public boolean deletePlaylist(String name) {
        return playlists.remove(name) != null;
    }

    public Playlist getPlaylist(String name) {
        return playlists.get(name);
    }

    public Playlist mergePlaylists(String name1, String name2, String newName) {
        Playlist p1 = playlists.get(name1);
        Playlist p2 = playlists.get(name2);
        if (p1 == null || p2 == null) return null;
        Playlist res = new Playlist(newName);
        Set<Song> seen = new HashSet<>();
        for (Song s : p1.toList()) {
            res.addSong(s);
            seen.add(s);
        }
        for (Song s : p2.toList()) if (!seen.contains(s)) res.addSong(s);
        playlists.put(newName, res);
        return res;
    }

    public Playlist shuffleMergePlaylists(List<String> names, String newName) {
        List<Playlist> lists = new ArrayList<>();
        for (String n : names) {
            if (playlists.containsKey(n)) lists.add(playlists.get(n));
        }
        Playlist res = Playlist.shuffleMerge(lists, newName);
        playlists.put(newName, res);
        return res;
    }

    public Playlist getLikedSongsPlaylist() {
        Playlist liked = new Playlist("Liked Songs");
        Set<Song> seen = new HashSet<>();
        for (Playlist p : playlists.values()) {
            for (Song s : p.toList()) {
                if (s.isLiked() && !seen.contains(s)) {
                    liked.addSong(s);
                    seen.add(s);
                }
            }
        }
        playlists.put("Liked Songs: ", liked);
        return liked;
    }

    public Collection<Playlist> allPlaylists() {
        return playlists.values();
    }
}
