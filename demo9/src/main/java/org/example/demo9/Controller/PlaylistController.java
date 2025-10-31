package org.example.demo9.Controller;

import org.example.demo9.Model.util.Database;
import org.example.demo9.Model.util.User;

import java.sql.*;
import java.util.Scanner;

public class PlaylistController {
    private final Connection conn;

    public PlaylistController(Database db) {
        this.conn = db.getConnection();
    }


    public void createPlaylist(User user, Scanner scanner) {
        try {
            System.out.print("🎵 Enter new playlist name: ");
            String name = scanner.nextLine();

            String sql = "INSERT INTO playlists (user_id, name) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, user.getId());
            stmt.setString(2, name);
            stmt.executeUpdate();

            System.out.println("✅ Playlist '" + name + "' created successfully!");
            stmt.close();
        } catch (SQLException e) {
            System.out.println("❌ Error creating playlist: " + e.getMessage());
        }
    }


    public void showPlaylists(User user) {
        try {
            String sql = "SELECT id, name FROM playlists WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n🎧 Your Playlists:");
            boolean hasPlaylists = false;
            while (rs.next()) {
                System.out.println(" - [" + rs.getInt("id") + "] " + rs.getString("name"));
                hasPlaylists = true;
            }

            if (!hasPlaylists)
                System.out.println("(No playlists yet!)");

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("❌ Error fetching playlists: " + e.getMessage());
        }
    }

    public void deletePlaylist(User user, Scanner scanner) {
        try {
            showPlaylists(user);
            System.out.print("\n🗑 Enter playlist ID to delete: ");
            int id = Integer.parseInt(scanner.nextLine());

            String sql = "DELETE FROM playlists WHERE id = ? AND user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setInt(2, user.getId());
            int rows = stmt.executeUpdate();

            if (rows > 0)
                System.out.println("✅ Playlist deleted successfully!");
            else
                System.out.println("⚠️ Playlist not found or not yours.");

            stmt.close();
        } catch (SQLException e) {
            System.out.println("❌ Error deleting playlist: " + e.getMessage());
        }
    }

    public  void showAllSongs(Connection conn) throws SQLException {
        String query = "SELECT * FROM songs LIMIT 20"; // برای تست 20 تا اول رو نشون بده
        PreparedStatement ps = conn.prepareStatement(query);
        var rs = ps.executeQuery();

        System.out.println("\n🎶 Available Songs:");
        while (rs.next()) {
            System.out.println(rs.getInt("id") + ". " +
                    rs.getString("artist_name") + " - " +
                    rs.getString("track_name") + " (" + rs.getString("genre") + ")");
        }
    }

}

