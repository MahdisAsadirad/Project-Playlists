package org.example.demo9.Controller;

import org.example.demo9.Model.util.Database;
import org.example.demo9.Model.util.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignUpLogin {
    private final Database db;

    public SignUpLogin(Database db) {
        this.db = db;
    }

    public boolean signUp(String username, String password) throws SQLException {
        String query = "INSERT INTO users(username, password) VALUES (?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeUpdate() > 0;
        }
    }

    public User login(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("username"));
            }
            return null;
        }
    }
}
