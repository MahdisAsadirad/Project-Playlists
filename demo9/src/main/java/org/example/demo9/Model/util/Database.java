package org.example.demo9.Model.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/Playlist";
    private static final String USER = "root";
    private static final String PASS = "";

    public Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(true);
            return conn;
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("❌ Database disconnected " + e.getMessage(), e);
        }
    }
}
