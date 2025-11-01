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

public class SortController implements Initializable {
    @FXML private ComboBox<String> playlistCombo;
    @FXML private ComboBox<String> sortCriteriaCombo;
    @FXML private ToggleGroup sortOrderGroup;
    @FXML private RadioButton ascendingRadio;
    @FXML private RadioButton descendingRadio;
    @FXML private Button sortButton;
    @FXML private VBox resultContainer;

    private User currentUser;
    private Database db;

    public SortController() {
        this.db = new Database();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserPlaylists();
        setupSortCriteria();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sortButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        sortButton.setOnAction(e -> handleSort());

        ascendingRadio.setSelected(true);
    }

    private void loadUserPlaylists() {
        String sql = "SELECT name FROM playlists WHERE user_id = ? ORDER BY name";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();

            List<String> playlists = new ArrayList<>();
            while (rs.next()) {
                playlists.add(rs.getString("name"));
            }

            playlistCombo.getItems().setAll(playlists);

        } catch (SQLException e) {
            showError("Error loading playlists: " + e.getMessage());
        }
    }

    private void setupSortCriteria() {
        sortCriteriaCombo.getItems().setAll(
                "Track Name",
                "Artist Name",
                "Release Date",
                "Genre"
        );

        sortCriteriaCombo.setValue("Track Name");
    }

    @FXML
    private void handleSort() {
        String playlistName = playlistCombo.getValue();
        String criteria = sortCriteriaCombo.getValue();
        String sortOrder = ascendingRadio.isSelected() ? "ASC" : "DESC";

        if (playlistName == null || criteria == null) {
            showError("Please select a playlist and sort criteria!");
            return;
        }

        try {
            sortPlaylist(playlistName, criteria, sortOrder);
        } catch (SQLException e) {
            showError("Error sorting playlist: " + e.getMessage());
        }
    }

    private void sortPlaylist(String playlistName, String criteria, String sortOrder) throws SQLException {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int playlistId = getPlaylistId(conn, playlistName);

                deletePlaylistSongs(conn, playlistId);

                reinsertSortedSongs(conn, playlistId, criteria, sortOrder);

                conn.commit();

                showSuccess("Playlist sorted successfully by " + criteria + " (" +
                        (sortOrder.equals("ASC") ? "Ascending" : "Descending") + ")");
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

    private void deletePlaylistSongs(Connection conn, int playlistId) throws SQLException {
        String sql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            stmt.executeUpdate();
        }
    }

    private void reinsertSortedSongs(Connection conn, int playlistId, String criteria, String sortOrder) throws SQLException {
        String columnName = getColumnName(criteria);
        String sql = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) " +
                "SELECT ?, s.id, ? " +
                "FROM songs s " +
                "JOIN playlist_songs ps ON s.id = ps.song_id " +
                "WHERE ps.playlist_id = ? " +
                "ORDER BY s." + columnName + " " + sortOrder;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, currentUser.getId());
            stmt.setInt(3, playlistId);
            stmt.executeUpdate();
        }
    }

    private String getColumnName(String criteria) {
        switch (criteria.toLowerCase()) {
            case "track name": return "track_name";
            case "artist name": return "artist_name";
            case "release date": return "release_date";
            case "genre": return "genre";
            default: return "track_name";
        }
    }

    private void clearForm() {
        playlistCombo.setValue(null);
        sortCriteriaCombo.setValue("Track Name");
        ascendingRadio.setSelected(true);
    }

    private void showSuccess(String message) {
        resultContainer.getChildren().clear();
        Label successLabel = new Label("✅ " + message);
        successLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 14; -fx-font-weight: bold;");
        successLabel.setWrapText(true);
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