package org.example.demo9.Model.util;

import java.sql.*;

public class SignUpLogin {
    private Connection conn;

    public SignUpLogin(Connection conn) {
        this.conn = conn;
    }

    public boolean signUp(String username, String password) throws SQLException {
        String query = "INSERT INTO users(username, password) VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, username);
        ps.setString(2, password);
        return ps.executeUpdate() > 0;
    }


    public org.example.demo9.Model.util.User login(String username, String password) throws SQLException {
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

