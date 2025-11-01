package org.example.demo9.Model.song;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Playlist {
    private int id;
    private final int userId;
    private final String name;
    private SongNode head;
    private SongNode tail;
    private int size;


    public Playlist(int id, String name, int userId) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.head = null;
        this.tail = null;
        this.size = 0;
    }


    public Playlist(String name) {
        this(0, name, 0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id=userId;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public void addSong(Song song) {
        SongNode newNode = new SongNode(song);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    public boolean removeSong(String trackName) {
        SongNode current = head;
        while (current != null) {
            if (current.data.getTrackName().equalsIgnoreCase(trackName)) {
                if (current.prev != null) current.prev.next = current.next;
                else head = current.next;

                if (current.next != null) current.next.prev = current.prev;
                else tail = current.prev;

                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public Song findSong(String trackName) {
        SongNode current = head;
        while (current != null) {
            if (current.data.getTrackName().equalsIgnoreCase(trackName))
                return current.data;
            current = current.next;
        }
        return null;
    }

    public List<Song> toList() {
        List<Song> songs = new ArrayList<>();
        SongNode current = head;
        while (current != null) {
            songs.add(current.data);
            current = current.next;
        }
        return songs;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    public void merge(Playlist other) {
        SongNode current = other.head;
        while (current != null) {
            if (findSong(current.data.getTrackName()) == null)
                addSong(current.data);
            current = current.next;
        }
    }

    public static Playlist shuffleMerge(List<Playlist> lists, String newName) {
        Playlist result = new Playlist(newName);
        List<Song> pool = new ArrayList<>();
        Set<Song> seen = new HashSet<>();
        for (Playlist p : lists) {
            for (Song s : p.toList()) {
                if (seen.add(s)) pool.add(s);
            }
        }
        Collections.shuffle(pool);
        for (Song s : pool) result.addSong(s);
        return result;
    }

    public void sortBy(String criteria) {
        List<Song> list = toList();
        Comparator<Song> cmp;
        switch (criteria.toLowerCase()) {
            case "track name":
            case "track_name":
                cmp = Comparator.comparing(Song::getTrackName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "artist name":
            case "artist_name":
                cmp = Comparator.comparing(Song::getArtistName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "release date":
            case "release_date":
                cmp = Comparator.comparing(Song::getReleaseDate, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                return;
        }
        list.sort(cmp);
        clear();
        for (Song s : list) addSong(s);
    }

    public Playlist filterBy(String criteria, String value) {
        Playlist out = new Playlist(this.name + "-filtered");
        for (Song s : toList()) {
            switch (criteria.toLowerCase()) {
                case "genre":
                    if (s.getGenre().equalsIgnoreCase(value)) out.addSong(s);
                    break;
                case "artist":
                    if (s.getArtistName().equalsIgnoreCase(value)) out.addSong(s);
                    break;
                case "year":
                    if (s.getReleaseDate().equalsIgnoreCase(value)) out.addSong(s);
                    break;
                case "topic":
                    if (s.getTopic().equalsIgnoreCase(value)) out.addSong(s);
                    break;
            }
        }
        return out;
    }

    public void likeSong(String trackName) {
        Song s = findSong(trackName);
        if (s != null) s.setLiked(true);
    }

    public void unlikeSong(String trackName) {
        Song s = findSong(trackName);
        if (s != null) s.setLiked(false);
    }

    public String play() {
        StringBuilder sb = new StringBuilder();
        SongNode current = head;
        while (current != null) {
            sb.append(current.data).append("\n");
            current = current.next;
        }
        return sb.toString();
    }

    public String playBackward() {
        StringBuilder sb = new StringBuilder();
        SongNode current = tail;
        while (current != null) {
            sb.append(current.data).append("\n");
            current = current.prev;
        }
        return sb.toString();
    }

    public String shufflePlay() {
        List<Song> list = toList();
        Collections.shuffle(list);
        StringBuilder sb = new StringBuilder();
        for (Song s : list) sb.append(s).append("\n");
        return sb.toString();
    }

    public void loadSongsFromDatabase(Connection conn) throws SQLException {
        String query = """
                SELECT s.id, s.artist_name, s.track_name, s.release_date, s.genre, s.len, s.topic
                FROM songs s
                JOIN playlist_songs ps ON s.id = ps.song_id
                WHERE ps.playlist_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Song song = new Song(
                        rs.getInt("id"),
                        rs.getString("artist_name"),
                        rs.getString("track_name"),
                        rs.getString("release_date"),
                        rs.getString("genre"),
                        rs.getDouble("len"),
                        rs.getString("topic")
                );
                addSong(song);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("🎶 Playlist: ").append(name)
                .append(" (").append(size).append(" songs)\n\n");

        SongNode current = head;
        int i = 1;
        while (current != null) {
            sb.append(i++).append(". ").append(current.data.toString()).append("\n");
            current = current.next;
        }
        sb.append("------------------------------------------------------------------------------------\n");
        return sb.toString();
    }

    // به جای این:
    public Playlist mergeAndCreateNew(Playlist other, String newName) {
        Playlist mergedPlaylist = new Playlist(newName);

        // اضافه کردن آهنگ‌های پلی‌لیست اول (this)
        SongNode current = this.head;
        while (current != null) {
            mergedPlaylist.addSong(current.data);
            current = current.next;
        }

        // اضافه کردن آهنگ‌های پلی‌لیست دوم (other)
        current = other.head;
        while (current != null) {
            mergedPlaylist.addSong(current.data);
            current = current.next;
        }

        return mergedPlaylist;
    }
}

