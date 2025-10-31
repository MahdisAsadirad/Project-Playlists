package org.example.demo9.Model.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private Connection conn;

    public UserDAO(Connection conn) { this.conn = conn; }

    public boolean register(String username, String password) throws SQLException {
        String query = "INSERT INTO users(username,password) VALUES (?,?)";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, username);
        ps.setString(2, password); // بعداً بهتره Hash کنیم
        return ps.executeUpdate() > 0;
    }

    public User login(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username=? AND password=?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        if(rs.next()){
            return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
        }
        return null;
    }
}
