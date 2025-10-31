package org.example.demo9.Model.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/Playlist";
    private static final String USER = "root";
    private static final String PASS = "";
    private Connection conn;

    public Database() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to Database successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL Driver not found!");
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public void close() throws SQLException {
        if (conn != null) conn.close();
    }
}
