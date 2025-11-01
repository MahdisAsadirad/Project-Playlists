package org.example.demo9.Controller;
import org.example.demo9.Model.util.Database;
import org.example.demo9.Model.Classes.User;

import java.sql.*;
import java.util.Scanner;

public class PlaylistController {
    private final Database db;

    public PlaylistController(Database db) {
        this.db = db;
    }



    public void createPlaylist(User user, Scanner scanner) {
        System.out.print("Enter new playlist name: ");
        String name = scanner.nextLine();


        String checkSql = "SELECT COUNT(*) FROM playlists WHERE user_id = ? AND name = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, user.getId());
            checkStmt.setString(2, name);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Error: A playlist with this name already exists!");
                return;
            }

            String insertSql = "INSERT INTO playlists (user_id, name) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, user.getId());
                stmt.setString(2, name);
                stmt.executeUpdate();
                System.out.println("✅ Playlist '" + name + "' created successfully!");
            }

        } catch (SQLException e) {
            System.out.println("Error creating playlist: " + e.getMessage());
        }
    }


    public void showPlaylists(User user) {
        String sql = "SELECT id, name FROM playlists WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nYour Playlists:");
            boolean hasPlaylists = false;
            while (rs.next()) {
                System.out.println(" - [" + rs.getInt("id") + "] " + rs.getString("name"));
                hasPlaylists = true;
            }

            if (!hasPlaylists)
                System.out.println("(No playlists yet!)");

        } catch (SQLException e) {
            System.out.println("Error fetching playlists: " + e.getMessage());
        }
    }

    public void deletePlaylist(User user, Scanner scanner) {
        showPlaylists(user);
        System.out.print("\nEnter playlist ID to delete: ");
        int id = Integer.parseInt(scanner.nextLine());

        String sql = "DELETE FROM playlists WHERE id = ? AND user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setInt(2, user.getId());
            int rows = stmt.executeUpdate();

            if (rows > 0)
                System.out.println("Playlist deleted successfully!");
            else
                System.out.println("Playlist not found or not yours.");

        } catch (SQLException e) {
            System.out.println("Error deleting playlist: " + e.getMessage());
        }
    }
}
