package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.demo9.Model.Classes.User;
import org.example.demo9.Model.util.Database;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LikedSongsController implements Initializable {
    @FXML private VBox likedSongsContainer;
    @FXML private Label statsLabel;

    private User currentUser;
    private final Database db;

    public LikedSongsController() {
        this.db = new Database();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadLikedSongs();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // اضافه کردن کلاس‌های استایل به عناصر اصلی
        likedSongsContainer.getStyleClass().add("vbox");
        statsLabel.getStyleClass().add("subtitle");
    }

    private void loadLikedSongs() {
        likedSongsContainer.getChildren().clear();

        String query = "SELECT s.id, s.track_name, s.artist_name, s.genre, s.release_date, s.len, s.topic " +
                "FROM liked_songs ls " +
                "JOIN songs s ON ls.song_id = s.id " +
                "WHERE ls.user_id = ? " +
                "ORDER BY ls.created_at DESC";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();

            int songCount = 0;
            while (rs.next()) {
                addLikedSongCard(rs.getInt("id"),
                        rs.getString("track_name"),
                        rs.getString("artist_name"),
                        rs.getString("genre"),
                        rs.getInt("release_date"),
                        rs.getDouble("len"),
                        rs.getString("topic"));
                songCount++;
            }

            updateStats(songCount);

            if (songCount == 0) {
                Label emptyLabel = new Label("You haven't liked any songs yet!");
                emptyLabel.getStyleClass().add("hint");
                likedSongsContainer.getChildren().add(emptyLabel);
            }

        } catch (SQLException e) {
            showError("Error loading liked songs: " + e.getMessage());
        }
    }

    private void addLikedSongCard(int songId, String trackName, String artistName,
                                  String genre, int releaseDate, double length, String topic) {
        HBox card = new HBox(15);
        card.getStyleClass().add("song-card");

        // آیکون قلب
        Label heartIcon = new Label("❤");
        heartIcon.getStyleClass().add("heart-icon");

        // اطلاعات آهنگ
        VBox songInfo = new VBox(5);
        songInfo.setPrefWidth(400);

        Label trackLabel = new Label(trackName);
        trackLabel.getStyleClass().add("song-title");

        Label artistLabel = new Label("by " + artistName);
        artistLabel.getStyleClass().add("song-artist");

        Label detailsLabel = new Label(genre + " • " + releaseDate + " • " + length + "s • " + topic);
        detailsLabel.getStyleClass().add("song-details");

        songInfo.getChildren().addAll(trackLabel, artistLabel, detailsLabel);

        // دکمه‌ها
        HBox actions = new HBox(10);

        Button unlikeButton = new Button("Unlike");
        unlikeButton.getStyleClass().addAll("button", "danger");
        unlikeButton.setOnAction(e -> unlikeSong(songId));

        Button addToPlaylistButton = new Button("Add to Playlist");
        addToPlaylistButton.getStyleClass().addAll("button", "success");
        addToPlaylistButton.setOnAction(e -> showAddToPlaylistDialog(songId));

        actions.getChildren().addAll(unlikeButton, addToPlaylistButton);
        card.getChildren().addAll(heartIcon, songInfo, actions);
        likedSongsContainer.getChildren().add(card);
    }

    private void unlikeSong(int songId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unlike Song");
        alert.setHeaderText("Are you sure you want to remove this song from your liked songs?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM liked_songs WHERE user_id = ? AND song_id = ?";
                try (Connection conn = db.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setInt(1, currentUser.getId());
                    stmt.setInt(2, songId);
                    int rows = stmt.executeUpdate();

                    if (rows > 0) {
                        showSuccess("Song removed from liked songs!");
                        loadLikedSongs();
                    }

                } catch (SQLException e) {
                    showError("Error removing song: " + e.getMessage());
                }
            }
        });
    }

    private void showAddToPlaylistDialog(int songId) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Add to Playlist");
        dialog.setHeaderText("Select a playlist to add this song to");

        List<String> playlists = getUserPlaylists();
        dialog.getItems().addAll(playlists);

        dialog.showAndWait().ifPresent(playlistName -> addSongToPlaylist(songId, playlistName));
    }

    private List<String> getUserPlaylists() {
        List<String> playlists = new ArrayList<>();
        String sql = "SELECT name FROM playlists WHERE user_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) playlists.add(rs.getString("name"));

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
            if (e.getErrorCode() == 1062)
                showError("This song is already in the playlist!");
            else
                showError("Error adding song to playlist: " + e.getMessage());
        }
    }

    private void updateStats(int songCount) {
        statsLabel.setText("❤ You have " + songCount + " liked songs");
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


