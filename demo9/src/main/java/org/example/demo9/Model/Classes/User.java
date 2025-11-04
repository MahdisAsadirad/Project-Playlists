package org.example.demo9.Model.Classes;

import org.example.demo9.Model.util.Database;
import java.sql.*;

public class User {
    private final int id;
    private final String username;
    private final Database db;

    public User(int id, String username) {
        this.id = id;
        this.username = username;
        this.db = new Database();
    }

    public boolean createPlaylistInDatabase(String name) {
        String sql = "INSERT INTO playlists (user_id, name) VALUES (?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.id);
            stmt.setString(2, name);
            int result = stmt.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Playlist getPlaylist(String name) {
        String sql = "SELECT id, name FROM playlists WHERE name = ? AND user_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, this.id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Playlist playlist = new Playlist(rs.getInt("id"), rs.getString("name"), this.id);
                playlist.loadFromDatabase(db);
                return playlist;
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public java.util.List<Playlist> getPlaylistsFromDatabase() {
        java.util.List<Playlist> playlists = new java.util.ArrayList<>();
        String sql = "SELECT id, name FROM playlists WHERE user_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Playlist playlist = new Playlist(rs.getInt("id"), rs.getString("name"), this.id);
                playlist.loadFromDatabase(db);
                playlists.add(playlist);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playlists;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
}