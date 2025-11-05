package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.demo9.Model.Classes.User;
import org.example.demo9.Model.util.Database;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PlaylistSongsController implements Initializable {
    @FXML private Label playlistTitleLabel;
    @FXML private VBox songsContainer;
    @FXML private Button backButton;
    @FXML private Button addSongButton;

    private int playlistId;
    private String playlistName;
    private User currentUser;
    private final Database db;

    public PlaylistSongsController() {
        this.db = new Database();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        backButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        addSongButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
    }

    public void setPlaylistInfo(int playlistId, String playlistName, User user) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.currentUser = user;
        updateUI();
        loadSongs();
    }

    private void updateUI() {
        playlistTitleLabel.setText("🎵 " + playlistName + " - Songs");
    }

    private void loadSongs() {
        songsContainer.getChildren().clear();

        String query = "SELECT s.id, s.track_name, s.artist_name, s.genre, s.release_date, u.username " +
                "FROM playlist_songs ps " +
                "JOIN songs s ON ps.song_id = s.id " +
                "JOIN users u ON ps.user_id = u.id " +
                "WHERE ps.playlist_id = ? ORDER BY ps.sort_order ASC";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            boolean hasSongs = false;
            while (rs.next()) {
                addSongCard(rs.getInt("id"),
                        rs.getString("track_name"),
                        rs.getString("artist_name"),
                        rs.getString("genre"),
                        rs.getInt("release_date"),
                        rs.getString("username"));
                hasSongs = true;
            }

            if (!hasSongs) {
                Label emptyLabel = new Label("No songs in this playlist yet!");
                emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16; -fx-padding: 40;");
                songsContainer.getChildren().add(emptyLabel);
            }

        } catch (SQLException e) {
            showError("Error loading songs: " + e.getMessage());
        }
    }

    private void addSongCard(int songId, String trackName, String artistName, String genre, int releaseDate, String addedBy) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-pref-width: 600;");

        Label icon = new Label("🎵");
        icon.setStyle("-fx-font-size: 20;");

        VBox songInfo = new VBox(5);
        songInfo.setPrefWidth(350);

        Label trackLabel = new Label(trackName);
        trackLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label artistLabel = new Label("by " + artistName);
        artistLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");

        Label detailsLabel = new Label(genre + "   • " + releaseDate + "   • Added by: " + addedBy);
        detailsLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");

        songInfo.getChildren().addAll(trackLabel, artistLabel, detailsLabel);

        // Like button
        Button likeBtn = new Button("♡");
        likeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 16;");

        // Check and set initial like status
        checkAndUpdateLikeButton(likeBtn, songId);

        // Set action for like button
        likeBtn.setOnAction(e -> toggleLikeSong(songId, likeBtn));

        Button removeButton = new Button("Remove");
        removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 8 15;");
        removeButton.setOnAction(e -> removeSongFromPlaylist(songId));

        Button playBtn = new Button("▶");
        playBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 8 15;");
        playBtn.setOnAction(e -> playSong(trackName, artistName));

        HBox buttonsBox = new HBox(10, playBtn, likeBtn, removeButton);

        card.getChildren().addAll(icon, songInfo, buttonsBox);
        songsContainer.getChildren().add(card);
    }

    private void checkAndUpdateLikeButton(Button likeButton, int songId) {
        String checkSql = "SELECT COUNT(*) FROM liked_songs WHERE user_id = ? AND song_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {

            stmt.setInt(1, currentUser.getId());
            stmt.setInt(2, songId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                likeButton.setText("❤");
                likeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 16;");
            } else {
                likeButton.setText("♡");
                likeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 16;");
            }

        } catch (SQLException e) {
            System.out.println("Error checking like status: " + e.getMessage());
        }
    }

    private void toggleLikeSong(int songId, Button likeButton) {
        String checkSql = "SELECT COUNT(*) FROM liked_songs WHERE user_id = ? AND song_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, currentUser.getId());
            checkStmt.setInt(2, songId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // Unlike the song
                String deleteSql = "DELETE FROM liked_songs WHERE user_id = ? AND song_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, currentUser.getId());
                    deleteStmt.setInt(2, songId);
                    deleteStmt.executeUpdate();

                    likeButton.setText("♡");
                    likeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 16;");
                    showSuccess("Song removed from liked songs!");
                }
            } else {
                // Like the song
                String insertSql = "INSERT INTO liked_songs (user_id, song_id) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, currentUser.getId());
                    insertStmt.setInt(2, songId);
                    insertStmt.executeUpdate();

                    likeButton.setText("❤");
                    likeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 16;");
                    showSuccess("Song added to liked songs! ❤");
                }
            }

        } catch (SQLException e) {
            showError("Error toggling like status: " + e.getMessage());
        }
    }

    private void playSong(String trackName, String artistName) {
        showSuccess("Now playing: " + trackName + " - " + artistName);
    }

    @FXML
    private void handleAddSong() {
        try {
            showAddSongDialog();
        } catch (SQLException e) {
            showError("Error showing add song dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/sections/PlaylistsSection.fxml"));
            VBox playlistsSection = loader.load();

            PlaylistsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            StackPane contentArea = (StackPane) songsContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(playlistsSection);
            }

        } catch (Exception e) {
            showError("Error going back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAddSongDialog() throws SQLException {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Song to Playlist");
        dialog.setHeaderText("Select a song to add to " + playlistName);

        VBox songsList = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(songsList);
        scrollPane.setPrefSize(400, 300);

        loadAvailableSongs(songsList);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait();
    }

    private void loadAvailableSongs(VBox songsList) throws SQLException {
        String query ="SELECT id, track_name, artist_name, genre FROM songs ORDER BY id ";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                HBox songItem = new HBox(10);
                songItem.setStyle("-fx-padding: 10; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");

                Label songInfo = new Label(rs.getString("track_name") + " - " + rs.getString("artist_name"));
                songInfo.setStyle("-fx-font-size: 14;");

                Button addButton = new Button("Add");
                addButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-size: 12;");

                final int currentSongId = rs.getInt("id");
                addButton.setOnAction(e -> addSongToPlaylist(currentSongId));

                songItem.getChildren().addAll(songInfo, addButton);
                songsList.getChildren().add(songItem);
            }

        } catch (SQLException e) {
            showError("Error loading available songs: " + e.getMessage());
            throw e;
        }
    }

    private void addSongToPlaylist(int songId) {
        String query = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) VALUES (?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            stmt.setInt(3, currentUser.getId());
            stmt.executeUpdate();

            showSuccess("Song added successfully!");
            loadSongs();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                showError("This song is already in the playlist!");
            } else {
                showError("Error adding song: " + e.getMessage());
            }
        }
    }

    private void removeSongFromPlaylist(int songId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Song");
        alert.setHeaderText("Are you sure you want to remove this song from the playlist?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String query = "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";

                try (Connection conn = db.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {

                    stmt.setInt(1, playlistId);
                    stmt.setInt(2, songId);
                    int rows = stmt.executeUpdate();

                    if (rows > 0) {
                        showSuccess("Song removed successfully!");
                        loadSongs();
                    }

                } catch (SQLException e) {
                    showError("Error removing song: " + e.getMessage());
                }
            }
        });
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