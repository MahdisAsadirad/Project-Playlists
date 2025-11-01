package org.example.demo9.Model.Classes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Playlist {
    private int id;
    private final int userId;
    private final String name;
    public SongNode head;
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

    // Getter and Setter methods
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public int getSize() { return size; }
    public SongNode getHead() { return head; }

    // ✅ متد اصلی برای اضافه کردن آهنگ
    public void addSong(Song song) {
        SongNode newNode = new SongNode(song);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            newNode.setPrev(tail);
            tail = newNode;
        }
        size++;
    }

    // ✅ تبدیل لیست پیوندی به ArrayList
    public List<Song> toList() {
        List<Song> songs = new ArrayList<>();
        SongNode current = head;
        while (current != null) {
            songs.add(current.getData());
            current = current.getNext();
        }
        return songs;
    }

    // ✅ بررسی وجود آهنگ در پلی‌لیست
    public boolean containsSong(Song song) {
        SongNode current = head;
        while (current != null) {
            if (current.getData().equals(song)) {
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    // ✅ ادغام دو پلی‌لیست بدون آهنگ تکراری (ایجاد پلی‌لیست جدید)
    public Playlist mergeAndCreateNew(Playlist other, String newName) {
        Playlist mergedPlaylist = new Playlist(newName);
        Set<Song> uniqueSongs = new HashSet<>();

        // اضافه کردن آهنگ‌های پلی‌لیست اول
        SongNode current = this.head;
        while (current != null) {
            if (uniqueSongs.add(current.getData())) {
                mergedPlaylist.addSong(current.getData());
            }
            current = current.getNext();
        }

        // اضافه کردن آهنگ‌های پلی‌لیست دوم
        current = other.head;
        while (current != null) {
            if (uniqueSongs.add(current.getData())) {
                mergedPlaylist.addSong(current.getData());
            }
            current = current.getNext();
        }

        return mergedPlaylist;
    }

    // ✅ ادغام مستقیم دو پلی‌لیست (اتصال انتهای اول به ابتدای دوم)
    public void mergeWith(Playlist other) {
        if (other == null || other.head == null) {
            return; // پلی‌لیست دوم خالی است
        }

        if (this.head == null) {
            // اگر پلی‌لیست اول خالی است
            this.head = other.head;
            this.tail = other.tail;
            this.size = other.size;
        } else {
            // اتصال فیزیکی نودها
            this.tail.setNext(other.head);
            other.head.setPrev(this.tail);
            this.tail = other.tail;
            this.size += other.size;
        }

        // پاک کردن پلی‌لیست دوم
        other.head = null;
        other.tail = null;
        other.size = 0;
    }

    // ✅ سورت کردن پلی‌لیست با جابجایی واقعی نودها
    public void sortLinkedlistBy(String criteria) {
        if (head == null || head.getNext() == null) {
            return; // لیست خالی یا فقط یک عنصر
        }

        // استفاده از Merge Sort
        head = mergeSort(head, criteria);

        // به روز رسانی tail
        tail = head;
        while (tail != null && tail.getNext() != null) {
            tail = tail.getNext();
        }
    }

    // ✅ الگوریتم Merge Sort برای لیست پیوندی
    private SongNode mergeSort(SongNode start, String criteria) {
        if (start == null || start.getNext() == null) {
            return start;
        }

        // پیدا کردن وسط لیست
        SongNode middle = getMiddle(start);
        SongNode nextOfMiddle = middle.getNext();
        middle.setNext(null);

        // سورت بازگشتی دو نیمه
        SongNode left = mergeSort(start, criteria);
        SongNode right = mergeSort(nextOfMiddle, criteria);

        // ادغام دو نیمه سورت شده
        return merge(left, right, criteria);
    }

    // ✅ پیدا کردن وسط لیست پیوندی
    private SongNode getMiddle(SongNode start) {
        if (start == null) return null;

        SongNode slow = start;
        SongNode fast = start.getNext();

        while (fast != null) {
            fast = fast.getNext();
            if (fast != null) {
                slow = slow.getNext();
                fast = fast.getNext();
            }
        }
        return slow;
    }

    // ✅ ادغام دو لیست سورت شده با جابجایی واقعی نودها
    private SongNode merge(SongNode left, SongNode right, String criteria) {
        SongNode dummy = new SongNode(null);
        SongNode current = dummy;

        while (left != null && right != null) {
            if (compareSongs(left.getData(), right.getData(), criteria) <= 0) {
                current.setNext(left);
                left.setPrev(current);
                left = left.getNext();
            } else {
                current.setNext(right);
                right.setPrev(current);
                right = right.getNext();
            }
            current = current.getNext();
        }

        // اضافه کردن باقیمانده
        if (left != null) {
            current.setNext(left);
            left.setPrev(current);
        } else {
            current.setNext(right);
            if (right != null) right.setPrev(current);
        }

        SongNode result = dummy.getNext();
        if (result != null) {
            result.setPrev(null);
        }
        return result;
    }

    // ✅ مقایسه آهنگ‌ها بر اساس معیار داده شده
    private int compareSongs(Song song1, Song song2, String criteria) {
        switch (criteria.toLowerCase()) {
            case "track name":
            case "track_name":
                return song1.getTrackName().compareToIgnoreCase(song2.getTrackName());
            case "artist name":
            case "artist_name":
                return song1.getArtistName().compareToIgnoreCase(song2.getArtistName());
            case "release date":
            case "release_date":
                return Integer.compare(song1.getReleaseDate(), song2.getReleaseDate());
            default:
                return song1.getTrackName().compareToIgnoreCase(song2.getTrackName());
        }
    }

    // ✅ بارگذاری آهنگ‌ها از دیتابیس - اصلاح شده برای جاوا 11
    public void loadSongsFromDatabase(Connection conn) throws SQLException {
        // جایگزینی Text Block با String معمولی
        String query = "SELECT s.id, s.artist_name, s.track_name, s.release_date, s.genre, s.len, s.topic " +
                "FROM songs s " +
                "JOIN playlist_songs ps ON s.id = ps.song_id " +
                "WHERE ps.playlist_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Song song = new Song(
                        rs.getInt("id"),
                        rs.getString("artist_name"),
                        rs.getString("track_name"),
                        rs.getInt("release_date"),
                        rs.getString("genre"),
                        rs.getDouble("len"),
                        rs.getString("topic")
                );
                addSong(song);
            }
        }
    }

    // ✅ نمایش پلی‌لیست
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("🎶 Playlist: ").append(name)
                .append(" (").append(size).append(" songs)\n\n");

        SongNode current = head;
        int i = 1;
        while (current != null) {
            sb.append(i++).append(". ").append(current.getData().toString()).append("\n");
            current = current.getNext();
        }
        sb.append("------------------------------------------------------------------------------------\n");
        return sb.toString();
    }
}