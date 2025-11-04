package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.demo9.Model.Classes.User;
import org.example.demo9.Model.util.Database;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SongsController implements Initializable {
    @FXML private VBox songsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    private User currentUser;
    private Database db;
    private List<SongNodeWrapper> allSongs;

    public SongsController() {
        this.db = new Database();
        this.allSongs = new ArrayList<>();
    }

    // کلاس wrapper برای مدیریت وضعیت لایک
    private class SongNodeWrapper {
        private int songId;
        private String trackName;
        private String artistName;
        private String genre;
        private int releaseDate;
        private double length;
        private String topic;
        private boolean isLiked;

        public SongNodeWrapper(int songId, String trackName, String artistName,
                               String genre, int releaseDate, double length, String topic) {
            this.songId = songId;
            this.trackName = trackName;
            this.artistName = artistName;
            this.genre = genre;
            this.releaseDate = releaseDate;
            this.length = length;
            this.topic = topic;
            this.isLiked = false;
        }

        public int getSongId() { return songId; }
        public String getTrackName() { return trackName; }
        public String getArtistName() { return artistName; }
        public String getGenre() { return genre; }
        public int getReleaseDate() { return releaseDate; }
        public double getLength() { return length; }
        public String getTopic() { return topic; }
        public boolean isLiked() { return isLiked; }
        public void setLiked(boolean liked) { this.isLiked = liked; }

        public void setSongId(int songId) {
            this.songId = songId;
        }

        public void setTrackName(String trackName) {
            this.trackName = trackName;
        }

        public void setArtistName(String artistName) {
            this.artistName = artistName;
        }

        public void setGenre(String genre) {
            this.genre = genre;
        }

        public void setReleaseDate(int releaseDate) {
            this.releaseDate = releaseDate;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadAllSongs();
        setupSortCombo();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchField.setPromptText("Search songs...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchSongs(newValue);
        });
    }

    private void setupSortCombo() {
        sortCombo.getItems().setAll(
                "Track Name (A-Z)",
                "Track Name (Z-A)",
                "Artist Name (A-Z)",
                "Artist Name (Z-A)",
                "Release Date (Newest)",
                "Release Date (Oldest)",
                "Genre (A-Z)",
                "Genre (Z-A)"
        );
        sortCombo.setValue("Track Name (A-Z)");

        // اضافه کردن listener برای مرتب‌سازی
        sortCombo.setOnAction(e -> sortAndDisplaySongs());
    }

    private void loadAllSongs() {
        allSongs.clear();
        songsContainer.getChildren().clear();

        String query = "SELECT id, track_name, artist_name, genre, release_date, len, topic FROM songs";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                SongNodeWrapper song = new SongNodeWrapper(
                        rs.getInt("id"),
                        rs.getString("track_name"),
                        rs.getString("artist_name"),
                        rs.getString("genre"),
                        rs.getInt("release_date"),
                        rs.getDouble("len"),
                        rs.getString("topic")
                );
                allSongs.add(song);

                // بررسی وضعیت لایک
                checkLikeStatus(song);
            }

            displaySongs(allSongs);

        } catch (SQLException e) {
            showError("Error loading songs: " + e.getMessage());
        }
    }

    private void checkLikeStatus(SongNodeWrapper song) {
        String checkSql = "SELECT COUNT(*) FROM liked_songs WHERE user_id = ? AND song_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {

            stmt.setInt(1, currentUser.getId());
            stmt.setInt(2, song.getSongId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                song.setLiked(true);
            }

        } catch (SQLException e) {
            System.out.println("Error checking like status: " + e.getMessage());
        }
    }

    private void displaySongs(List<SongNodeWrapper> songs) {
        songsContainer.getChildren().clear();

        if (songs.isEmpty()) {
            Label emptyLabel = new Label("No songs found!");
            emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16; -fx-padding: 40;");
            songsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (SongNodeWrapper song : songs) {
            addSongCard(song);
        }
    }

    private void addSongCard(SongNodeWrapper song) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-pref-width: 600; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // آیکون آهنگ
        Label icon = new Label("🎵");
        icon.setStyle("-fx-font-size: 20;");

        // اطلاعات آهنگ
        VBox songInfo = new VBox(5);
        songInfo.setPrefWidth(400);

        Label trackLabel = new Label(song.getTrackName());
        trackLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label artistLabel = new Label("by " + song.getArtistName());
        artistLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");

        Label detailsLabel = new Label(String.format("%s • %d • %.1fs • %s",
                song.getGenre(), song.getReleaseDate(), song.getLength(), song.getTopic()));
        detailsLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");

        songInfo.getChildren().addAll(trackLabel, artistLabel, detailsLabel);

        // دکمه‌های action
        HBox actions = new HBox(10);

        Button likeButton = new Button(song.isLiked() ? "❤" : "♡");
        likeButton.setStyle(song.isLiked() ?
                "-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 16;" :
                "-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 16;");

        likeButton.setOnAction(e -> toggleLikeSong(song, likeButton));

        Button addToPlaylistButton = new Button("Add to Playlist");
        addToPlaylistButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-font-size: 12; -fx-padding: 8 15; -fx-background-radius: 6;");
        addToPlaylistButton.setOnAction(e -> showAddToPlaylistDialog(song.getSongId()));

        actions.getChildren().addAll(likeButton, addToPlaylistButton);
        card.getChildren().addAll(icon, songInfo, actions);
        songsContainer.getChildren().add(card);
    }

    private void toggleLikeSong(SongNodeWrapper song, Button likeButton) {
        String checkSql = "SELECT COUNT(*) FROM liked_songs WHERE user_id = ? AND song_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, currentUser.getId());
            checkStmt.setInt(2, song.getSongId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // آنلایک کردن
                String deleteSql = "DELETE FROM liked_songs WHERE user_id = ? AND song_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, currentUser.getId());
                    deleteStmt.setInt(2, song.getSongId());
                    deleteStmt.executeUpdate();

                    likeButton.setText("♡");
                    likeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 16;");
                    song.setLiked(false);
                    showSuccess("Song removed from liked songs!");
                }
            } else {
                // لایک کردن
                String insertSql = "INSERT INTO liked_songs (user_id, song_id) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, currentUser.getId());
                    insertStmt.setInt(2, song.getSongId());
                    insertStmt.executeUpdate();

                    likeButton.setText("❤");
                    likeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 16;");
                    song.setLiked(true);
                    showSuccess("Song added to liked songs! ❤");
                }
            }

        } catch (SQLException e) {
            showError("Error toggling like status: " + e.getMessage());
        }
    }

    private void searchSongs(String query) {
        if (query == null || query.trim().isEmpty()) {
            sortAndDisplaySongs();
            return;
        }

        List<SongNodeWrapper> filteredSongs = new ArrayList<>();
        String searchTerm = query.toLowerCase().trim();

        for (SongNodeWrapper song : allSongs) {
            if (song.getTrackName().toLowerCase().contains(searchTerm) ||
                    song.getArtistName().toLowerCase().contains(searchTerm) ||
                    song.getGenre().toLowerCase().contains(searchTerm)) {
                filteredSongs.add(song);
            }
        }

        sortAndDisplaySongs(filteredSongs);
    }

    private void sortAndDisplaySongs() {
        sortAndDisplaySongs(allSongs);
    }

    private void sortAndDisplaySongs(List<SongNodeWrapper> songs) {
        if (songs.isEmpty()) {
            displaySongs(songs);
            return;
        }

        String selectedSort = sortCombo.getValue();
        if (selectedSort == null) {
            selectedSort = "Track Name (A-Z)";
        }

        // استخراج معیار و جهت از گزینه انتخابی
        String criteria;
        boolean ascending;

        switch (selectedSort) {
            case "Track Name (Z-A)":
                criteria = "Track Name";
                ascending = false;
                break;
            case "Artist Name (A-Z)":
                criteria = "Artist Name";
                ascending = true;
                break;
            case "Artist Name (Z-A)":
                criteria = "Artist Name";
                ascending = false;
                break;
            case "Release Date (Newest)":
                criteria = "Release Date";
                ascending = false;
                break;
            case "Release Date (Oldest)":
                criteria = "Release Date";
                ascending = true;
                break;
            case "Genre (A-Z)":
                criteria = "Genre";
                ascending = true;
                break;
            case "Genre (Z-A)":
                criteria = "Genre";
                ascending = false;
                break;
            default: // "Track Name (A-Z)"
                criteria = "Track Name";
                ascending = true;
        }

        // مرتب‌سازی لیست
        songs.sort((s1, s2) -> {
            int result = 0;
            switch (criteria) {
                case "Track Name":
                    result = s1.getTrackName().compareToIgnoreCase(s2.getTrackName());
                    break;
                case "Artist Name":
                    result = s1.getArtistName().compareToIgnoreCase(s2.getArtistName());
                    break;
                case "Release Date":
                    result = Integer.compare(s1.getReleaseDate(), s2.getReleaseDate());
                    break;
                case "Genre":
                    result = s1.getGenre().compareToIgnoreCase(s2.getGenre());
                    break;
            }
            return ascending ? result : -result;
        });

        displaySongs(songs);
    }

    private void showAddToPlaylistDialog(int songId) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Add to Playlist");
        dialog.setHeaderText("Select a playlist to add this song to");

        List<String> playlists = getUserPlaylists();
        if (playlists.isEmpty()) {
            showError("No playlists available! Create a playlist first.");
            return;
        }

        dialog.getItems().addAll(playlists);

        dialog.showAndWait().ifPresent(playlistName -> {
            addSongToPlaylist(songId, playlistName);
        });
    }

    private List<String> getUserPlaylists() {
        List<String> playlists = new ArrayList<>();
        String sql = "SELECT name FROM playlists WHERE user_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                playlists.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            showError("Error loading playlists: " + e.getMessage());
        }

        return playlists;
    }

    private void addSongToPlaylist(int songId, String playlistName) {
        String findPlaylistSql = "SELECT id FROM playlists WHERE name = ? AND user_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement findStmt = conn.prepareStatement(findPlaylistSql)) {

            findStmt.setString(1, playlistName);
            findStmt.setInt(2, currentUser.getId());
            ResultSet rs = findStmt.executeQuery();

            if (rs.next()) {
                int playlistId = rs.getInt("id");

                String insertSql = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, playlistId);
                    insertStmt.setInt(2, songId);
                    insertStmt.setInt(3, currentUser.getId());
                    insertStmt.executeUpdate();

                    showSuccess("Song added to " + playlistName + " successfully!");
                }
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                showError("This song is already in the playlist!");
            } else {
                showError("Error adding song to playlist: " + e.getMessage());
            }
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
