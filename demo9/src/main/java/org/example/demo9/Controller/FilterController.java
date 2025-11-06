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

public class FilterController implements Initializable {
    @FXML private ComboBox<String> playlistCombo;
    @FXML private ComboBox<String> criteriaCombo;
    @FXML private TextField filterValueField;
    @FXML private TextField newPlaylistNameField;
    @FXML private Button filterButton;
    @FXML private VBox resultContainer;

    private User currentUser;
    private Database db;

    public FilterController() {
        this.db = new Database();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserPlaylists();
        setupCriteriaCombo();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterButton.setOnAction(e -> handleFilter());
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

    private void setupCriteriaCombo() {
        criteriaCombo.getItems().setAll(
                "Genre",
                "Artist",
                "Release Year",
                "Topic"
        );

        criteriaCombo.setValue("Genre");
    }

    @FXML
    private void handleFilter() {
        String playlistName = playlistCombo.getValue();
        String criteria = criteriaCombo.getValue();
        String filterValue = filterValueField.getText().trim();
        String newName = newPlaylistNameField.getText().trim();

        if (playlistName == null || criteria == null || filterValue.isEmpty() || newName.isEmpty()) {
            showError("Please fill all fields!");
            return;
        }

        try {
            createFilteredPlaylist(playlistName, criteria, filterValue, newName);
        } catch (SQLException e) {
            showError("Error creating filtered playlist: " + e.getMessage());
        }
    }

    private void createFilteredPlaylist(String playlistName, String criteria, String filterValue, String newName) throws SQLException {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int sourcePlaylistId = getPlaylistId(conn, playlistName);
                int newPlaylistId = createNewPlaylist(conn, newName);

                int filteredCount = copyFilteredSongs(conn, sourcePlaylistId, newPlaylistId, criteria, filterValue);

                conn.commit();

                if (filteredCount > 0) {
                    showSuccess("Filtered playlist created successfully! \n" +
                            "Found " + filteredCount + " songs matching '" + filterValue + "'");
                } else {
                    showError("No songs found matching the filter criteria!");
                }
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

    private int copyFilteredSongs(Connection conn, int sourcePlaylistId, int targetPlaylistId,
                                  String criteria, String filterValue) throws SQLException {
        String columnName = getColumnName(criteria);
        String sql = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) " +
                "SELECT ?, ps.song_id, ps.user_id " +
                "FROM playlist_songs ps " +
                "JOIN songs s ON ps.song_id = s.id " +
                "WHERE ps.playlist_id = ? AND LOWER(s." + columnName + ") LIKE LOWER(?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, targetPlaylistId);
            stmt.setInt(2, sourcePlaylistId);
            stmt.setString(3, "%" + filterValue + "%");

            return stmt.executeUpdate();
        }
    }

    private String getColumnName(String criteria) {
        switch (criteria.toLowerCase()) {
            case "genre": return "genre";
            case "artist": return "artist_name";
            case "release year": return "release_date";
            case "topic": return "topic";
            default: return "genre";
        }
    }

    private void clearForm() {
        playlistCombo.setValue(null);
        criteriaCombo.setValue("Genre");
        filterValueField.clear();
        newPlaylistNameField.clear();
    }

    private void showSuccess(String message) {
        resultContainer.getChildren().clear();
        Label successLabel = new Label("" + message);
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