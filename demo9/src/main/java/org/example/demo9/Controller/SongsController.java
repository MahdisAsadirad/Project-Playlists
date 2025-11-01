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
import java.util.ResourceBundle;

public class SongsController implements Initializable {
    @FXML private VBox songsContainer;
    @FXML private TextField searchField;

    private User currentUser;
    private Database db;

    public SongsController() {
        this.db = new Database();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadAllSongs();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // تنظیم search field
        searchField.setPromptText("Search songs...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchSongs(newValue);
        });
    }

    private void loadAllSongs() {
        songsContainer.getChildren().clear();

        String query = "SELECT id, track_name, artist_name, genre, release_date, len, topic FROM songs ORDER BY track_name";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                addSongCard(rs.getInt("id"),
                        rs.getString("track_name"),
                        rs.getString("artist_name"),
                        rs.getString("genre"),
                        rs.getInt("release_date"),
                        rs.getDouble("len"),
                        rs.getString("topic"));
            }

        } catch (SQLException e) {
            showError("Error loading songs: " + e.getMessage());
        }
    }

    private void addSongCard(int songId, String trackName, String artistName, String genre, int releaseDate, double length, String topic) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-pref-width: 600;");

        Label icon = new Label("🎵");
        icon.setStyle("-fx-font-size: 20;");

        VBox songInfo = new VBox(5);
        songInfo.setPrefWidth(400);

        Label trackLabel = new Label(trackName);
        trackLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label artistLabel = new Label("by " + artistName);
        artistLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");

        Label detailsLabel = new Label(genre + " • " + releaseDate + " • " + length + "s • " + topic);
        detailsLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");

        songInfo.getChildren().addAll(trackLabel, artistLabel, detailsLabel);

        // دکمه‌های action
        HBox actions = new HBox(10);

        Button likeButton = new Button("❤️");
        likeButton.setStyle("-fx-background-color: transparent; -fx-font-size: 16;");
        likeButton.setOnAction(e -> toggleLikeSong(songId));

        Button addToPlaylistButton = new Button("Add to Playlist");
        addToPlaylistButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 8 15;");
        addToPlaylistButton.setOnAction(e -> showAddToPlaylistDialog(songId));

        actions.getChildren().addAll(likeButton, addToPlaylistButton);
        card.getChildren().addAll(icon, songInfo, actions);
        songsContainer.getChildren().add(card);
    }

    private void searchSongs(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadAllSongs();
            return;
        }

        songsContainer.getChildren().clear();

        String sql = "SELECT id, track_name, artist_name, genre, release_date, len, topic FROM songs " +
                "WHERE track_name LIKE ? OR artist_name LIKE ? OR genre LIKE ? " +
                "ORDER BY track_name";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchTerm = "%" + query + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                addSongCard(rs.getInt("id"),
                        rs.getString("track_name"),
                        rs.getString("artist_name"),
                        rs.getString("genre"),
                        rs.getInt("release_date"),
                        rs.getDouble("len"),
                        rs.getString("topic"));
            }

        } catch (SQLException e) {
            showError("Error searching songs: " + e.getMessage());
        }
    }

    private void toggleLikeSong(int songId) {
    }

    private void showAddToPlaylistDialog(int songId) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Add to Playlist");
        dialog.setHeaderText("Select a playlist to add this song to");

        // لیست پلی‌لیست‌های کاربر
        java.util.List<String> playlists = getUserPlaylists();
        dialog.getItems().addAll(playlists);

        dialog.showAndWait().ifPresent(playlistName -> {
            addSongToPlaylist(songId, playlistName);
        });
    }

    private java.util.List<String> getUserPlaylists() {
        java.util.List<String> playlists = new java.util.ArrayList<>();
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

                // اضافه کردن آهنگ به پلی‌لیست
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