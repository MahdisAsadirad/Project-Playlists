package org.example.demo9;

import org.example.demo9.Model.SignUpLogin;
import org.example.demo9.Model.User;
import org.example.demo9.Model.song.*;
import org.example.demo9.Model.util.Database;


import java.util.*;

import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Database db = new Database();
            SignUpLogin signUpLogin = new SignUpLogin(db.getConnection());
            Scanner scanner = new Scanner(System.in);

            System.out.println("Welcome to Playlist!");
            User currentUser = null;

            while (currentUser == null) {
                System.out.println("1. Sign Up");
                System.out.println("2. Login");
                System.out.print("Choose an option: ");
                String option = scanner.nextLine();

                System.out.print("Username: ");
                String username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();

                if (option.equals("1")) {
                    if (signUpLogin.signUp(username, password)) {
                        System.out.println("Sign Up successful! Now login.");
                    } else {
                        System.out.println("Sign Up failed! Username might exist.");
                    }
                } else if (option.equals("2")) {
                    currentUser = signUpLogin.login(username, password);
                    if (currentUser != null) {
                        System.out.println("Login successful! Welcome " + currentUser.getUsername());
                    } else {
                        System.out.println("Login failed! Try again.");
                    }
                }
            }

            scanner.close();
            db.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
