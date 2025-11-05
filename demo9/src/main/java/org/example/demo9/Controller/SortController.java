package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.demo9.Model.Classes.Playlist;
import org.example.demo9.Model.Classes.SongNode;
import org.example.demo9.Model.Classes.User;
import org.example.demo9.Model.util.Database;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SortController implements Initializable {
    @FXML
    private ComboBox<String> playlistCombo;
    @FXML
    private ComboBox<String> sortCriteriaCombo;
    @FXML
    private Button sortButton;
    @FXML
    private VBox resultContainer;

    private User currentUser;
    private final Database db;
    private DashboardController dashboardController;

    public SortController() {
        this.db = new Database();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserPlaylists();
        setupSortCriteria();
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sortButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        sortButton.setOnAction(e -> handleSort());
    }

    private void loadUserPlaylists() {
        String sql = "SELECT name FROM playlists WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());
            var rs = stmt.executeQuery();
            var playlists = new ArrayList<String>();
            while (rs.next()) playlists.add(rs.getString("name"));
            playlistCombo.getItems().setAll(playlists);

        } catch (SQLException e) {
            showError("Error loading playlists: " + e.getMessage());
        }
    }

    private void setupSortCriteria() {
        sortCriteriaCombo.getItems().setAll("Track Name", "Artist Name", "Release Date", "Genre");
        sortCriteriaCombo.setValue("Track Name");
    }

    @FXML
    private void handleSort() {
        String playlistName = playlistCombo.getValue();
        String criteria = sortCriteriaCombo.getValue();

        if (playlistName == null || criteria == null) {
            showError("Please select a playlist and sort criteria!");
            return;
        }

        Playlist playlist = currentUser.getPlaylist(playlistName);
        if (playlist == null || playlist.getSize() == 0) {
            showError("Playlist is empty or not found!");
            return;
        }

        playlist.sortByCriteria(criteria.toLowerCase());

        List<SongNode> sortedSongs = new ArrayList<>();
        SongNode current = playlist.getHead();
        while (current != null) {
            sortedSongs.add(current);
            current = current.getNext();
        }

        saveSortedOrderToDatabase(sortedSongs, playlist.getId());

        showSuccess("Playlist sorted successfully by " + criteria + " 🎶");

        if (dashboardController != null) dashboardController.refreshPlaylists();
    }

    private void saveSortedOrderToDatabase(List<SongNode> sortedSongs, int playlistId) {
        String updateSql = "UPDATE playlist_songs SET sort_order = ? WHERE playlist_id = ? AND song_id = ? AND user_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {

            int order = 1;
            for (SongNode song : sortedSongs) {
                stmt.setInt(1, order++);
                stmt.setInt(2, playlistId);
                stmt.setInt(3, song.getSongId());
                stmt.setInt(4, currentUser.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (SQLException e) {
            showError("Error saving sorted order: " + e.getMessage());
        }
    }

    private void showSuccess(String message) {
        resultContainer.getChildren().clear();
        Label successLabel = new Label("✅ " + message);
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
