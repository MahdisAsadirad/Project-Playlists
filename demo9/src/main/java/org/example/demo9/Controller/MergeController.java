package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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

public class MergeController implements Initializable {
    @FXML private ComboBox<String> firstPlaylistCombo;
    @FXML private ComboBox<String> secondPlaylistCombo;
    @FXML private TextField newPlaylistNameField;
    @FXML private Button mergeButton;
    @FXML private VBox resultContainer;

    private User currentUser;
    private Database db;

    public MergeController() {
        this.db = new Database();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserPlaylists();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mergeButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        mergeButton.setOnAction(e -> handleMerge());
    }

    private void loadUserPlaylists() {
        String sql = "SELECT id, name FROM playlists WHERE user_id = ? ORDER BY name";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();

            List<String> playlists = new ArrayList<>();
            while (rs.next()) {
                playlists.add(rs.getString("name"));
            }

            firstPlaylistCombo.getItems().setAll(playlists);
            secondPlaylistCombo.getItems().setAll(playlists);

        } catch (SQLException e) {
            showError("Error loading playlists: " + e.getMessage());
        }
    }

    @FXML
    private void handleMerge() {
        String firstPlaylist = firstPlaylistCombo.getValue();
        String secondPlaylist = secondPlaylistCombo.getValue();
        String newName = newPlaylistNameField.getText().trim();

        if (firstPlaylist == null || secondPlaylist == null || newName.isEmpty()) {
            showError("Please select both playlists and enter a name for the new playlist!");
            return;
        }

        if (firstPlaylist.equals(secondPlaylist)) {
            showError("Please select two different playlists!");
            return;
        }

        try {
            mergePlaylists(firstPlaylist, secondPlaylist, newName);
        } catch (SQLException e) {
            showError("Error merging playlists: " + e.getMessage());
        }
    }

    private void mergePlaylists(String firstPlaylistName, String secondPlaylistName, String newName) throws SQLException {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // پیدا کردن ID پلی‌لیست‌ها
                int firstId = getPlaylistId(conn, firstPlaylistName);
                int secondId = getPlaylistId(conn, secondPlaylistName);


                int newPlaylistId = createNewPlaylist(conn, newName);


                copyPlaylistSongs(conn, firstId, newPlaylistId);


                copyPlaylistSongsUnique(conn, secondId, newPlaylistId);

                conn.commit();

                showSuccess("Playlists merged successfully! New playlist: " + newName);
                clearForm();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private int getPlaylistId(Connection conn, String playlistName) throws SQLException {
        String sql = "SELECT id FROM playlists WHERE name = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playlistName);
            stmt.setInt(2, currentUser.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Playlist not found: " + playlistName);
            }
        }
    }

    private int createNewPlaylist(Connection conn, String name) throws SQLException {
        String sql = "INSERT INTO playlists (user_id, name) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, currentUser.getId());
            stmt.setString(2, name);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Failed to create new playlist");
            }
        }
    }

    private void copyPlaylistSongs(Connection conn, int sourcePlaylistId, int targetPlaylistId) throws SQLException {
        String sql = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) " +
                "SELECT ?, song_id, user_id FROM playlist_songs WHERE playlist_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, targetPlaylistId);
            stmt.setInt(2, sourcePlaylistId);
            stmt.executeUpdate();
        }
    }

    private void copyPlaylistSongsUnique(Connection conn, int sourcePlaylistId, int targetPlaylistId) throws SQLException {
        String sql = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) " +
                "SELECT ?, ps.song_id, ps.user_id FROM playlist_songs ps " +
                "WHERE ps.playlist_id = ? AND NOT EXISTS (" +
                "    SELECT 1 FROM playlist_songs ps2 " +
                "    WHERE ps2.playlist_id = ? AND ps2.song_id = ps.song_id" +
                ")";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, targetPlaylistId);
            stmt.setInt(2, sourcePlaylistId);
            stmt.setInt(3, targetPlaylistId);
            stmt.executeUpdate();
        }
    }

    private void clearForm() {
        firstPlaylistCombo.setValue(null);
        secondPlaylistCombo.setValue(null);
        newPlaylistNameField.clear();
    }

    private void showSuccess(String message) {
        resultContainer.getChildren().clear();
        Label successLabel = new Label("" + message);
        successLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 14; -fx-font-weight: bold;");
        resultContainer.getChildren().add(successLabel);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}