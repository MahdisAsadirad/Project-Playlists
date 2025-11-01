package org.example.demo9;

import org.example.demo9.Controller.PlaylistController;
import org.example.demo9.Controller.UserController;
import org.example.demo9.Controller.SongController;
import org.example.demo9.Model.util.Database;
import org.example.demo9.Model.util.User;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Database db = new Database();
            UserController signUpLogin = new UserController(db);
            PlaylistController playlistController = new PlaylistController(db);

            Scanner scanner = new Scanner(System.in);

            System.out.println("*-*-* Welcome to Playlist *-*-*");
            User currentUser = null;


            while (currentUser == null) {
                System.out.println("\n1️. Sign Up");
                System.out.println("2️. Login");
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
                        System.out.println("Sign Up failed! Username might already exist.");
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
                System.out.println("1️. Playlist Management");
                System.out.println("2️. Add / Remove Song from Playlist");
                System.out.println("3. Show song from playlist");
                System.out.println("4. Merge Two Playlists");
                System.out.println("5. Shuffle Merge Playlists");
                System.out.println("6. Show Shuffled Playlists");
                System.out.println("7. Sort Playlist");
                System.out.println("8. Filter Playlist");
                System.out.println("9. Like / Dislike Song");
                System.out.println("10. Play Playlist");
                System.out.println("11. Play Playlist (Shuffle)");
                System.out.println("0️. Logout / Exit");
                System.out.print("👉 Enter your choice: ");

                String choice = scanner.nextLine();
                SongController songController = new SongController(db);

                switch (choice) {
                    case "1" -> {
                        boolean playlistMenu = true;
                        while (playlistMenu) {
                            System.out.println("\n🎵 Playlist Menu:");
                            System.out.println("1️. Show Playlists");
                            System.out.println("2️. Create Playlist");
                            System.out.println("3️. Delete Playlist");
                            System.out.println("0️. Back to Main Menu");
                            System.out.print("👉 Enter your choice: ");

                            String subChoice = scanner.nextLine();
                            switch (subChoice) {
                                case "1" -> playlistController.showPlaylists(currentUser);
                                case "2" -> playlistController.createPlaylist(currentUser, scanner);
                                case "3" -> playlistController.deletePlaylist(currentUser, scanner);
                                case "0" -> playlistMenu = false;
                                default -> System.out.println("Invalid choice! Try again.");
                            }
                        }
                    }

                    case "2" -> {
                        playlistController.showPlaylists(currentUser);
                        System.out.print("🎧 Enter playlist ID to manage songs: ");
                        int playlistId = Integer.parseInt(scanner.nextLine());

                        System.out.println("\n1. + Add Song");
                        System.out.println("2. - Remove Song");
                        System.out.print("Choose: ");
                        String sub = scanner.nextLine();

                        if (sub.equals("1")) {
                            songController.showAllSongs();
                            System.out.print("🎵 Enter Song ID to add: ");
                            int songId = Integer.parseInt(scanner.nextLine());
                            songController.addSongToPlaylist(playlistId, songId, currentUser.getId());
                        } else if (sub.equals("2")) {
                            System.out.print("🎵 Enter Song ID to remove: ");
                            int songId = Integer.parseInt(scanner.nextLine());
                            songController.removeSongFromPlaylist(playlistId, songId);
                        } else {
                            System.out.println("Invalid option!");
                        }
                    }
                    case "3" -> {
                        playlistController.showPlaylists(currentUser);
                        System.out.print("Enter playlist ID to view songs: ");
                        int playlistId = Integer.parseInt(scanner.nextLine());
                        songController.showSongsInPlaylist(playlistId);
                    }

                    case "5" -> {
                        songController.shufflePlaylists(currentUser, scanner);
                    }

                    case "6" -> {
                        songController.showShufflePlaylists(currentUser);
                        System.out.print("Enter shuffled playlist ID to view songs (or 0 to go back): ");
                        int shuffledId = Integer.parseInt(scanner.nextLine());
                        if (shuffledId != 0) {
                            songController.showSongsInPlaylist(shuffledId);
                        }
                    }



                    case "0" -> {
                        System.out.println("👋 Goodbye, " + currentUser.getUsername() + "!");
                        running = false;
                    }

                    default -> System.out.println("Invalid choice! Please try again.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
