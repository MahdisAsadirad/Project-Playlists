package org.example.demo9.Model.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private Connection conn;
    private static final String URL = "jdbc:mysql://localhost:3306/Playlist";
    private static final String USER = "root";
    private static final String PASS = "";

    public Database() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        conn = DriverManager.getConnection(URL, USER, PASS);
    }

    public Connection getConnection() {
        return conn;
    }

    public void close() throws SQLException {
        if (conn != null) conn.close();
    }
}

