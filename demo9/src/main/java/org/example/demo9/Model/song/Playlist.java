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

    public List<Song> toList() {
        List<Song> songs = new ArrayList<>();
        SongNode current = head;
        while (current != null) {
            songs.add(current.data);
            current = current.next;
        }
        return songs;
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

    public void sortLinkedlistBy(String criteria) {
        if (head == null || head.next == null) {
            return; // لیست خالی یا فقط یک عنصر
        }

        // استفاده از Merge Sort برای لیست پیوندی
        head = mergeSort(head, criteria);

        // به روز رسانی tail بعد از سورت
        tail = head;
        while (tail != null && tail.next != null) {
            tail = tail.next;
        }
    }

    private SongNode mergeSort(SongNode start, String criteria) {
        if (start == null || start.next == null) {
            return start;
        }

        // پیدا کردن وسط لیست
        SongNode middle = getMiddle(start);
        SongNode nextOfMiddle = middle.next;
        middle.next = null;

        // سورت بازگشتی دو نیمه
        SongNode left = mergeSort(start, criteria);
        SongNode right = mergeSort(nextOfMiddle, criteria);

        // ادغام دو نیمه سورت شده
        return merge(left, right, criteria);
    }

    private SongNode getMiddle(SongNode start) {
        if (start == null) return null;

        SongNode slow = start;
        SongNode fast = start.next;

        while (fast != null) {
            fast = fast.next;
            if (fast != null) {
                slow = slow.next;
                fast = fast.next;
            }
        }
        return slow;
    }

    private SongNode merge(SongNode left, SongNode right, String criteria) {
        if (left == null) return right;
        if (right == null) return left;

        SongNode result;
        Comparator<Song> comparator = getComparator(criteria);

        if (comparator.compare(left.data, right.data) <= 0) {
            result = left;
            result.next = merge(left.next, right, criteria);
            if (result.next != null) {
                result.next.prev = result;
            }
        } else {
            result = right;
            result.next = merge(left, right.next, criteria);
            if (result.next != null) {
                result.next.prev = result;
            }
        }
        result.prev = null;
        return result;
    }

    private Comparator<Song> getComparator(String criteria) {
        switch (criteria.toLowerCase()) {
            case "track name":
            case "track_name":
                return Comparator.comparing(Song::getTrackName, String.CASE_INSENSITIVE_ORDER);
            case "artist name":
            case "artist_name":
                return Comparator.comparing(Song::getArtistName, String.CASE_INSENSITIVE_ORDER);
            case "release date":
            case "release_date":
                return Comparator.comparing(Song::getReleaseDate, String.CASE_INSENSITIVE_ORDER);
            default:
                return Comparator.comparing(Song::getTrackName, String.CASE_INSENSITIVE_ORDER);
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
}

