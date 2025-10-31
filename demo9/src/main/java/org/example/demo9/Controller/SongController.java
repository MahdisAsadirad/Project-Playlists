package org.example.demo9.Controller;

import org.example.demo9.Model.util.Database;

import java.sql.*;
import java.util.Scanner;

public class SongController {
    private final Database db;

    public SongController(Database db) {
        this.db = db;
    }

    // 🎶 نمایش لیست آهنگ‌ها از جدول songs
    public void showAllSongs() {
        String query = "SELECT id, artist_name, track_name, genre, len, topic FROM songs LIMIT 50";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n🎶 Available Songs:");
            System.out.println("-----------------------------------------------------------");
            while (rs.next()) {
                int id = rs.getInt("id");
                String artist = rs.getString("artist_name");
                String track = rs.getString("track_name");
                String genre = rs.getString("genre");
                double len = rs.getDouble("len");
                String topic = rs.getString("topic");

                System.out.printf("%d. %s - %s (%s, %.1f sec, %s)\n",
                        id, artist, track, genre, len, topic);
            }
            System.out.println("-----------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("❌ Error loading songs: " + e.getMessage());
        }
    }

    // ➕ افزودن آهنگ به پلی‌لیست
    public void addSongToPlaylist(int playlistId, int songId) {
        String query = "INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, playlistId);
            ps.setInt(2, songId);
            ps.executeUpdate();
            System.out.println("✅ Song added to playlist!");
        } catch (SQLException e) {
            System.out.println("❌ Error adding song to playlist: " + e.getMessage());
        }
    }

    // ❌ حذف آهنگ از پلی‌لیست
    public void removeSongFromPlaylist(Scanner scanner, int playlistId) {
        String sqlList = "SELECT s.id, s.track_name FROM songs s " +
                "JOIN playlist_songs ps ON s.id = ps.song_id WHERE ps.playlist_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlList)) {

            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n🎵 Songs in Playlist:");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + ". " + rs.getString("track_name"));
            }

            rs.close();

            System.out.print("👉 Enter Song ID to remove: ");
            int songId = Integer.parseInt(scanner.nextLine());

            String sqlDelete = "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDelete)) {
                ps.setInt(1, playlistId);
                ps.setInt(2, songId);
                int rows = ps.executeUpdate();

                if (rows > 0)
                    System.out.println("✅ Song removed from playlist!");
                else
                    System.out.println("⚠️ Song not found in this playlist.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Error removing song: " + e.getMessage());
        }
    }
}
