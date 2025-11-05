package org.example.demo9.Model.Classes;

import org.example.demo9.Model.util.Database;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class Playlist {
    private int id;
    private String name;
    private SongNode head;
    private SongNode tail;
    private int size;
    private int userId;

    public Playlist(String name) {
        this.name = name;
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public Playlist(int id, String name, int userId) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void addSongToLinkedList(SongNode song) {
        SongNode newNode = new SongNode(song);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            tail = newNode;
        }
        size++;
    }

    public boolean removeSongFromPlaylist(String trackName) {
        if (head == null) return false;

        if (head.getTrackName().equals(trackName)) {
            head = head.getNext();
            if (head == null) {
                tail = null;
            }
            size--;
            return true;
        }

        SongNode current = head;
        while (current.getNext() != null) {
            if (current.getNext().getTrackName().equals(trackName)) {
                current.setNext(current.getNext().getNext());

                if (current.getNext() == null) {
                    tail = current;
                }

                size--;
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    public Playlist merge(Playlist other, String newName, Database db) throws SQLException {
        Playlist merged = new Playlist(newName);
        merged.setUserId(this.userId);

        Set<String> addedSongIds = new HashSet<>();

        //کپی کردن اهنگ های پلی لیست اول
        SongNode current = this.head;
        while (current != null) {
            if (!addedSongIds.contains(current.getTrackName())) {
                merged.addSongToLinkedList(new SongNode(current));
                addedSongIds.add(current.getTrackName());
            }
            current = current.getNext();
        }

        //کپی اهنگ های پلی لیست دو
        current = other.head;
        while (current != null) {
            if (!addedSongIds.contains(current.getTrackName())) {
                merged.addSongToLinkedList(new SongNode(current));
                addedSongIds.add(current.getTrackName());
            }
            current = current.getNext();
        }

        // حذف پلی‌لیست‌های اصلی از دیتابیس
        deletePlaylistFromDatabase(db, this.id);
        deletePlaylistFromDatabase(db, other.id);

        this.clear();
        other.clear();

        return merged;
    }

    private void deletePlaylistFromDatabase(Database db, int playlistId) throws SQLException {
        try (Connection conn = db.getConnection()) {
            // اول آهنگ‌های پلی‌لیست رو حذف کن
            String deleteSongsSql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSongsSql)) {
                stmt.setInt(1, playlistId);
                stmt.executeUpdate();
            }

            // سپس خود پلی‌لیست رو حذف کن
            String deletePlaylistSql = "DELETE FROM playlists WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deletePlaylistSql)) {
                stmt.setInt(1, playlistId);
                stmt.executeUpdate();
            }
        }
    }

    public void loadFromDatabase(Database db) throws SQLException {
        String sql = "SELECT s.id, s.artist_name, s.track_name, s.release_date, s.genre, s.len, s.topic " +
                "FROM songs s " +
                "JOIN playlist_songs ps ON s.id = ps.song_id " +
                "WHERE ps.playlist_id = ? " +
                "ORDER BY ps.user_id";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SongNode song = new SongNode(
                        rs.getInt("id"),
                        rs.getString("artist_name"),
                        rs.getString("track_name"),
                        rs.getInt("release_date"),
                        rs.getString("genre"),
                        rs.getDouble("len"),
                        rs.getString("topic")
                );
                this.addSongToLinkedList(song);
            }
        }
    }


    public int savePlaylistToDatabase(Database db) throws SQLException {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            // ایجاد پلی‌لیست جدید در دیتابیس
            String insertPlaylistSql = "INSERT INTO playlists (user_id, name) VALUES (?, ?)";
            int newPlaylistId;

            try (PreparedStatement stmt = conn.prepareStatement(insertPlaylistSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, this.userId);
                stmt.setString(2, this.name);
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    newPlaylistId = rs.getInt(1);
                    this.id = newPlaylistId;
                } else {
                    throw new SQLException("Failed to create new playlist");
                }
            }

            savePlaylistSongsToDatabase(db, newPlaylistId);

            conn.commit();
            return newPlaylistId;
        }
    }

    private void savePlaylistSongsToDatabase(Database db, int playlistId) throws SQLException {
        String sql = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) VALUES (?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            SongNode current = this.head;
            while (current != null) {
                stmt.setInt(1, playlistId);
                stmt.setInt(2, current.getSongId());
                stmt.setInt(3, this.userId);
                stmt.addBatch();
                current = current.getNext();
            }
            stmt.executeBatch();
        }
    }

    public void sortByCriteria(String subject, boolean ascending) {
        if (head == null || head.getNext() == null) return;

        head = mergeSort(head, subject, ascending);

        tail = head;
        while (tail != null && tail.getNext() != null) {
            tail = tail.getNext();
        }
    }

    private SongNode mergeSort(SongNode head, String subject, boolean ascending) {
        if (head == null || head.getNext() == null) return head;

        // پیدا کردن وسط لیست
        SongNode middle = getMiddle(head);
        SongNode nextOfMiddle = middle.getNext();
        middle.setNext(null);

        SongNode left = mergeSort(head, subject, ascending);
        SongNode right = mergeSort(nextOfMiddle, subject, ascending);

        // ادغام دو نیمه مرتب شده
        return sort(left, right, subject, ascending);
    }

    private SongNode getMiddle(SongNode head) {
        if (head == null) return head;

        SongNode slow = head;
        SongNode fast = head;

        while (fast.getNext() != null && fast.getNext().getNext() != null) {
            slow = slow.getNext();
            fast = fast.getNext().getNext();
        }

        return slow;
    }

    private SongNode sort(SongNode left, SongNode right, String subject, boolean ascending) {
        if (left == null) return right;
        if (right == null) return left;

        boolean compare;
        switch (subject.toLowerCase()) {
            case "artist":
                compare = ascending ?
                        left.getArtistName().compareToIgnoreCase(right.getArtistName()) <= 0 :
                        left.getArtistName().compareToIgnoreCase(right.getArtistName()) > 0;
                break;
            case "release date":
                compare = ascending ?
                        left.getReleaseDate() <= right.getReleaseDate() :
                        left.getReleaseDate() > right.getReleaseDate();
                break;
            case "genre":
                compare = ascending ?
                        left.getGenre().compareToIgnoreCase(right.getGenre()) <= 0 :
                        left.getGenre().compareToIgnoreCase(right.getGenre()) > 0;
                break;
            default: // track name
                compare = ascending ?
                        left.getTrackName().compareToIgnoreCase(right.getTrackName()) <= 0 :
                        left.getTrackName().compareToIgnoreCase(right.getTrackName()) > 0;
        }

        if (compare) {
            left.setNext(sort(left.getNext(), right, subject, ascending));
            return left;
        } else {
            right.setNext(sort(left, right.getNext(), subject, ascending));
            return right;
        }
    }

    // پاک کردن تمام آهنگ‌های پلی‌لیست
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public SongNode getHead() { return head; }
    public int getSize() { return size; }
    public int getUserId() { return userId; }
    public SongNode getTail() { return tail; }

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setHead(SongNode head) { this.head = head; }
    public void setTail(SongNode tail) { this.tail = tail; }
    public void setSize(int size) { this.size = size; }
}