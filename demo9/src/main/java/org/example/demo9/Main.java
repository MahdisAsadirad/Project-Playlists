package org.example.demo9;

import org.example.demo9.Controller.SignUpLogin;
import org.example.demo9.Model.song.User;
import org.example.demo9.Model.util.Database;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Database db = new Database();
            SignUpLogin signUpLogin = new SignUpLogin(db.getConnection());
            Scanner scanner = new Scanner(System.in);

            System.out.println("Welcome to Playlist 🎵");
            User currentUser = null;


            while (currentUser == null) {
                System.out.println("1.Sign Up");
                System.out.println("2️.Login");
                System.out.print("Choose an option: ");
                String option = scanner.nextLine();

                System.out.print("Username: ");
                String username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();

                if (option.equals("1")) {
                    if (signUpLogin.signUp(username, password)) {
                        System.out.println("✅ Sign Up successful! Now login.");
                    } else {
                        System.out.println("⚠️ Sign Up failed! Username might already exist.");
                    }
                } else if (option.equals("2")) {
                    currentUser = signUpLogin.login(username, password);
                    if (currentUser != null) {
                        System.out.println("🎉 Login successful! Welcome, " + currentUser.getUsername() + "!");
                    } else {
                        System.out.println("❌ Login failed! Try again.");
                    }
                } else {
                    System.out.println("Invalid option!");
                }
            }


            boolean running = true;
            while (running) {
                System.out.println("\n🎧 What would you like to do?");
                System.out.println("1️. Create Playlist");
                System.out.println("2️. Add / Remove Song from Playlist");
                System.out.println("3️. Merge Two Playlists");
                System.out.println("4️. Shuffle Merge");
                System.out.println("5️. Sort Playlist");
                System.out.println("6️. Filter Playlist");
                System.out.println("7️. Like / Dislike Song");
                System.out.println("8️. Play Playlist");
                System.out.println("9️. Play Playlist (Shuffle)");
                System.out.println("0️. Logout / Exit");
                System.out.print("👉 Enter your choice: ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1" -> System.out.println("Creating a new playlist...");
                    case "2" -> System.out.println("Adding or removing song...");
                    case "3" -> System.out.println("Merging playlists...");
                    case "4" -> System.out.println("Performing shuffle merge...");
                    case "5" -> System.out.println("Sorting playlist...");
                    case "6" -> System.out.println("🎚Filtering playlist...");
                    case "7" -> System.out.println("❤Toggling like/dislike...");
                    case "8" -> System.out.println("▶Playing playlist...");
                    case "9" -> System.out.println("Playing playlist (shuffle)...");
                    case "0" -> {
                        System.out.println("👋 Goodbye, " + currentUser.getUsername() + "!");
                        running = false;
                    }
                    default -> System.out.println("⚠️ Invalid choice! Please try again.");
                }
            }

            scanner.close();
            db.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
