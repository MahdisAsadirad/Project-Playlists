package org.example.demo9.Controller;

import org.example.demo9.Model.util.Database;

import java.sql.*;
import java.util.Scanner;

public class SongController {
    private final Database db;

    public SongController(Database db) {
        this.db = db;
    }

    public void showAllSongs() {
        String query = "SELECT id, artist_name, track_name, release_date, genre, len, topic FROM songs";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n -*-*-*-*- All Songs in Library -*-*-*-*-");
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
            System.out.println("Error while loading songs: " + e.getMessage());
        }
    }

    public void addSongToPlaylist(int playlistId, int songId, int userId) {
        String query = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            stmt.setInt(3, userId);

            stmt.executeUpdate();
            System.out.println("Song added successfully by user ID: " + userId);

        } catch (SQLException e) {
            System.out.println("Failed to add song: " + e.getMessage());
        }
    }


    public void removeSongFromPlaylist(int playlistId, int songId) {
        String query = "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);

            int rows = stmt.executeUpdate();
            if (rows > 0)
                System.out.println("Song removed successfully!");
            else
                System.out.println("Song not found in this playlist.");
        } catch (SQLException e) {
            System.out.println("Failed to remove song: " + e.getMessage());
        }
    }

    public void showSongsInPlaylist(int playlistId) {
        String query = "SELECT s.id, s.track_name, s.artist_name, s.genre, u.username " +
                "FROM playlist_songs ps " +
                "JOIN songs s ON ps.song_id = s.id " +
                "JOIN users u ON ps.user_id = u.id " +
                "WHERE ps.playlist_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n🎶 Songs in Playlist:");
            while (rs.next()) {
                System.out.printf("%d. %s - %s (%s) | Added by: %s%n",
                        rs.getInt("id"),
                        rs.getString("artist_name"),
                        rs.getString("track_name"),
                        rs.getString("genre"),
                        rs.getString("username"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Error loading songs: " + e.getMessage());
        }
    }
}
