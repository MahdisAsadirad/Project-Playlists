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
        this.id = id;
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

    public void addSong(Song song, int userId, Connection conn) throws SQLException {
        // ابتدا به لیست پیوندی اضافه کن
        addSong(song); // از متد ساده‌تر استفاده می‌کنیم

        // سپس به دیتابیس اضافه کن
        String query = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, this.id);
            stmt.setInt(2, song.getId());
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        }
    }

    public boolean removeSong(String trackName, Connection conn) throws SQLException {
        SongNode current = head;
        while (current != null) {
            if (current.data.getTrackName().equalsIgnoreCase(trackName)) {
                if (current.prev != null) current.prev.next = current.next;
                else head = current.next;

                if (current.next != null) current.next.prev = current.prev;
                else tail = current.prev;

                size--;


                String query = "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, this.id);
                    stmt.setInt(2, current.data.getId());
                    stmt.executeUpdate();
                }
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
                this.addSong(song); // اضافه کردن به لیست پیوندی
            }
        }
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
