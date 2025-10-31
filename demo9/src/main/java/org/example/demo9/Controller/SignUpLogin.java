package org.example.demo9.Controller;

import org.example.demo9.Model.song.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class SignUpLogin {
    private Connection conn;

    public SignUpLogin(Connection conn) {
        this.conn = conn;
    }

    // ثبت نام
    public boolean signUp(String username, String password) throws SQLException {
        String query = "INSERT INTO users(username, password) VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, username);
        ps.setString(2, password);
        return ps.executeUpdate() > 0;
    }

    // ورود
    public User login(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new User(rs.getInt("id"), rs.getString("username"));
        }
        return null;
    }
}
