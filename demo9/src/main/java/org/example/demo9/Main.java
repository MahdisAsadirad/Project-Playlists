package org.example.demo9;

import org.example.demo9.View.Command;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            Command command = new Command();
            command.start();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Database connection failed!");
        }
    }
}