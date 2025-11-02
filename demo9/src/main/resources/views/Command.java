package org.example.demo9.View;

import org.example.demo9.Controller.UserController;
import org.example.demo9.Model.Classes.User;
import org.example.demo9.Model.util.Database;

import java.sql.SQLException;
import java.util.Scanner;

public class Command {
    private final Database db;
    private final UserController userController;
    private final PlaylistController playlistController;
    private final SongController songController;
    private final Scanner scanner;
    private User currentUser;

    public Command() throws SQLException {
        this.db = new Database();
        this.userController = new UserController(db);
        this.playlistController = new PlaylistController(db);
        this.songController = new SongController(db);
        this.scanner = new Scanner(System.in);
    }

    public void start() throws SQLException {
        System.out.println("*-*-* Welcome to Playlist *-*-*");

        handleAuthentication();

        if (currentUser != null) {
            handleMainMenu();
        }

        scanner.close();
    }

    private void handleAuthentication() throws SQLException {
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
                handleSignUp(username, password);
            } else if (option.equals("2")) {
                handleLogin(username, password);
            } else {
                System.out.println("Invalid option!");
            }
        }
    }

    private void handleSignUp(String username, String password) throws SQLException {
        if (userController.signUp(username, password)) {
            System.out.println("Sign Up successful! Now login.");
        } else {
            System.out.println("Sign Up failed! Username might already exist.");
        }
    }

    private void handleLogin(String username, String password) throws SQLException {
        currentUser = userController.login(username, password);
        if (currentUser != null) {
            System.out.println("🎉 Login successful! Welcome, " + currentUser.getUsername() + "!");
        } else {
            System.out.println("❌ Login failed! Try again.");
        }
    }

    private void handleMainMenu() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> handlePlaylistManagement();
                case "2" -> handleSongManagement();
                case "3" -> handleShowSongsInPlaylist();
                case "4" -> handleMergePlaylists();
                case "5" -> handleShuffleMerge();
                case "6" -> handleShowShuffledPlaylists();
                case "7" -> handleSortPlaylist();
                case "8" -> handleFilterPlaylist();
                case "9" -> handleLikeDislike();
                case "0" -> {
                    System.out.println("👋 Goodbye, " + currentUser.getUsername() + "!");
                    running = false;
                }
                default -> System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private void printMainMenu() {
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
        System.out.println("0️. Logout / Exit");
        System.out.print("👉 Enter your choice: ");
    }

    private void handlePlaylistManagement() {
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

    private void handleSongManagement() {
        playlistController.showPlaylists(currentUser);
        System.out.print("Enter playlist ID to manage songs: ");
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

    private void handleShowSongsInPlaylist() {
        playlistController.showPlaylists(currentUser);
        System.out.print("Enter playlist ID to view songs: ");
        int playlistId = Integer.parseInt(scanner.nextLine());
        songController.showSongsInPlaylist(playlistId);
    }

    private void handleMergePlaylists() {
        songController.mergePlaylists(currentUser, scanner);
    }

    private void handleShuffleMerge() {
        songController.shufflePlaylists(currentUser, scanner);
    }

    private void handleShowShuffledPlaylists() {
        songController.showShufflePlaylists(currentUser);
        System.out.print("Enter shuffled playlist ID to view songs (or 0 to go back): ");
        int shuffledId = Integer.parseInt(scanner.nextLine());
        if (shuffledId != 0) {
            songController.showShuffleSongs(shuffledId);
            songController.showSongsInPlaylist(shuffledId);
        }
    }

    private void handleSortPlaylist() {
        songController.sortPlaylist(currentUser, scanner);
    }

    private void handleFilterPlaylist() {
        System.out.println("\n🎛Filter Options:");
        System.out.println("1. Create Filtered Playlist");
        System.out.println("2. Show My Filtered Playlists");
        System.out.print("Choose: ");
        String filterChoice = scanner.nextLine();

        if (filterChoice.equals("1")) {
            songController.filterPlaylist(currentUser, scanner);
        } else if (filterChoice.equals("2")) {
            songController.showFilteredPlaylists(currentUser);
            System.out.print("Enter filtered playlist ID to view songs (or 0 to go back): ");
            int filteredId = Integer.parseInt(scanner.nextLine());
            if (filteredId != 0) {
                songController.showFilteredSongs(filteredId);
            }
        } else {
            System.out.println("Invalid option!");
        }
    }

    private void handleLikeDislike() {
        System.out.println("\n❤Like/Dislike Menu:");
        System.out.println("1. Show All Songs & Toggle Like");
        System.out.println("2. Show My Liked Songs");
        System.out.print("Choose: ");
        String likeChoice = scanner.nextLine();

        if (likeChoice.equals("1")) {
            songController.toggleLikeStatus(currentUser, scanner);
        } else if (likeChoice.equals("2")) {
            songController.showLikedSongs(currentUser);
        } else {
            System.out.println("Invalid option!");
        }
    }
}