package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.demo9.Model.Classes.Playlist;
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
    @FXML private Button sortButton;
    @FXML private VBox resultContainer;

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

        if (playlistName == null || criteria == null) {
            showError("Please select a playlist and sort criteria!");
            return;
        }

        try {
            sortPlaylistUsingLinkedList(playlistName, criteria);
        } catch (Exception e) {
            showError("Error sorting playlist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sortPlaylistUsingLinkedList(String playlistName, String criteria) {
        // دریافت پلی‌لیست از کاربر
        Playlist playlist = currentUser.getPlaylist(playlistName);

        if (playlist == null) {
            showError("Playlist not found!");
            return;
        }

        // تعیین جهت مرتب‌سازی
        boolean ascending = getSortOrderFromUI();

        // مرتب‌سازی روی لیست پیوندی
        playlist.sortByCriteria(criteria.toLowerCase(), ascending);

        // ذخیره در دیتابیس
        try {
            // ابتدا آهنگ‌های قدیمی را از دیتابیس حذف می‌کنیم
            deletePlaylistSongsFromDatabase(playlist.getId());

            // سپس آهنگ‌های مرتب شده را ذخیره می‌کنیم
            playlist.savePlaylistSongsToDatabase(db, playlist.getId());

            showSuccess("Playlist sorted successfully by " + criteria);

            // رفرش کردن داشبورد
            if (dashboardController != null) {
                dashboardController.refreshPlaylists();
            }

            clearForm();

        } catch (SQLException e) {
            showError("Error saving sorted playlist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deletePlaylistSongsFromDatabase(int playlistId) throws SQLException {
        String sql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            stmt.executeUpdate();
        }
    }

    private boolean getSortOrderFromUI() {
        RadioButton selected = (RadioButton) sortOrderGroup.getSelectedToggle();
        return selected != null && selected.getText().equalsIgnoreCase("Ascending");
    }

    private void clearForm() {
        playlistCombo.setValue(null);
        sortCriteriaCombo.setValue("Track Name");

        // بازنشانی radio buttons
        if (sortOrderGroup.getSelectedToggle() != null) {
            sortOrderGroup.getSelectedToggle().setSelected(false);
        }
        // انتخاب پیش‌فرض
        RadioButton ascendingRadio = (RadioButton) sortOrderGroup.getToggles().get(0);
        if (ascendingRadio != null) {
            ascendingRadio.setSelected(true);
        }
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