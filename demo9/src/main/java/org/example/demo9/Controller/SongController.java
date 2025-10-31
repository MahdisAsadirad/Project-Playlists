package org.example.demo9.Controller;

import org.example.demo9.Model.util.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SongController {
    private final Database db;

    public SongController(Database db) {
        this.db = db;
    }

    // 🎵 نمایش تمام آهنگ‌ها
    public void showAllSongs() {
        String query = "SELECT id, artist_name, track_name, release_date, genre, len, topic FROM songs";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n🎼 ----- All Songs in Library -----");
            System.out.printf("%-5s | %-25s | %-35s | %-6s | %-10s | %-6s | %-10s%n",
                    "ID", "Artist", "Track", "Year", "Genre", "Len", "Topic");
            System.out.println("-----------------------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-5d | %-25s | %-35s | %-6d | %-10s | %-6d | %-10s%n",
                        rs.getInt("id"),
                        rs.getString("artist_name"),
                        rs.getString("track_name"),
                        rs.getInt("release_date"),
                        rs.getString("genre"),
                        rs.getInt("len"),
                        rs.getString("topic"));
            }
            System.out.println("-----------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("⚠️ Error while loading songs: " + e.getMessage());
        }
    }

    // 🎶 اضافه کردن آهنگ به پلی‌لیست
    public void addSongToPlaylist(int playlistId, int songId) {
        String query = "INSERT INTO playlist_songs (playlist_id, song_id) VALUES (" + playlistId + ", " + songId + ")";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            System.out.println("✅ Song added successfully!");
        } catch (SQLException e) {
            System.out.println("⚠️ Failed to add song: " + e.getMessage());
        }
    }

    // ❌ حذف آهنگ از پلی‌لیست
    public void removeSongFromPlaylist(java.util.Scanner scanner, int playlistId) {
        System.out.print("🎵 Enter Song ID to remove: ");
        int songId = Integer.parseInt(scanner.nextLine());
        String query = "DELETE FROM playlist_songs WHERE playlist_id = " + playlistId + " AND song_id = " + songId;
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            System.out.println("🗑️ Song removed successfully!");
        } catch (SQLException e) {
            System.out.println("⚠️ Failed to remove song: " + e.getMessage());
        }
    }
}
