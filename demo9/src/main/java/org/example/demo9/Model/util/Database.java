package org.example.demo9.Model.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
<<<<<<< HEAD
    private Connection conn;
    private static final String URL = "jdbc:mysql://localhost:3306/Playlist";
    private static final String USER = "root";
    private static final String PASS = "";
=======
    private static final String URL = "jdbc:mysql://localhost:3306/Playlist";
    private static final String USER = "root";
    private static final String PASS = "";
    private Connection conn;
>>>>>>> Mahdis

    public Database() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
<<<<<<< HEAD
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        conn = DriverManager.getConnection(URL, USER, PASS);
=======
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to Database successfully!\n");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL Driver not found!");
        }
>>>>>>> Mahdis
    }

    public Connection getConnection() {
        return conn;
    }

    public void close() throws SQLException {
        if (conn != null) conn.close();
    }
}
<<<<<<< HEAD

=======
>>>>>>> Mahdis
