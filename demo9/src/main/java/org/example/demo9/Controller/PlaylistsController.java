package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.example.demo9.Model.Classes.User;
import org.example.demo9.Model.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlaylistsController {
    @FXML private VBox playlistsContainer;
    @FXML private ScrollPane scrollPane;

    private Database db;
    private User currentUser;

    public PlaylistsController() {
        this.db = new Database();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadPlaylists();
    }

    @FXML
    public void initialize() {

        DashboardController dashboardController = (DashboardController) playlistsContainer.getScene().getWindow().getUserData();
        if (dashboardController != null) {
            setCurrentUser(dashboardController.getCurrentUser());
        }
    }

    private void loadPlaylists() {
        playlistsContainer.getChildren().clear();

        String sql = "SELECT id, name FROM playlists WHERE user_id = ? ORDER BY name";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                addPlaylistCard(rs.getInt("id"), rs.getString("name"));
            }

            if (playlistsContainer.getChildren().isEmpty()) {
                Label emptyLabel = new Label("No playlists yet. Create your first playlist!");
                emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16; -fx-padding: 40;");
                playlistsContainer.getChildren().add(emptyLabel);
            }

        } catch (SQLException e) {
            showError("Error loading playlists: " + e.getMessage());
        }
    }

    private void addPlaylistCard(int playlistId, String playlistName) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; -fx-border-color: #e0e0e0; -fx-border-radius: 10;");
        card.setPrefWidth(600);


        Label icon = new Label("🎵");
        icon.setStyle("-fx-font-size: 24;");


        VBox info = new VBox(5);
        Label nameLabel = new Label(playlistName);
        nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #333;");


        int songCount = getSongCount(playlistId);
        Label countLabel = new Label(songCount + " songs");
        countLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");

        info.getChildren().addAll(nameLabel, countLabel);


        HBox actions = new HBox(10);

        Button viewBtn = new Button("View Songs");
        viewBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 8 15;");
        viewBtn.setOnAction(e -> viewPlaylistSongs(playlistId, playlistName));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 8 15;");
        deleteBtn.setOnAction(e -> deletePlaylist(playlistId));

        actions.getChildren().addAll(viewBtn, deleteBtn);

        card.getChildren().addAll(icon, info, actions);
        playlistsContainer.getChildren().add(card);
    }

    private int getSongCount(int playlistId) {
        String sql = "SELECT COUNT(*) as count FROM playlist_songs WHERE playlist_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @FXML
    private void showCreatePlaylistDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Playlist");
        dialog.setHeaderText("Create New Playlist");
        dialog.setContentText("Playlist Name:");

        dialog.showAndWait().ifPresent(playlistName -> {
            if (!playlistName.trim().isEmpty()) {
                createPlaylist(playlistName.trim());
            }
        });
    }

    private void createPlaylist(String playlistName) {
        String sql = "INSERT INTO playlists (user_id, name) VALUES (?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());
            stmt.setString(2, playlistName);
            stmt.executeUpdate();

            showSuccess("Playlist '" + playlistName + "' created successfully!");
            loadPlaylists();

        } catch (SQLException e) {
            showError("Error creating playlist: " + e.getMessage());
        }
    }

    private void deletePlaylist(int playlistId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Playlist");
        alert.setHeaderText("Are you sure you want to delete this playlist?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                String sql = "DELETE FROM playlists WHERE id = ? AND user_id = ?";
                try (Connection conn = db.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setInt(1, playlistId);
                    stmt.setInt(2, currentUser.getId());
                    int rows = stmt.executeUpdate();

                    if (rows > 0) {
                        showSuccess("Playlist deleted successfully!");
                        loadPlaylists();
                    }

                } catch (SQLException e) {
                    showError("Error deleting playlist: " + e.getMessage());
                }
            }
        });
    }

    private void viewPlaylistSongs(int playlistId, String playlistName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/sections/PlaylistSongs.fxml"));
            Parent songsSection = loader.load();

            PlaylistSongsController controller = loader.getController();
            controller.setPlaylistInfo(playlistId, playlistName, currentUser);

            StackPane contentArea = (StackPane) playlistsContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(songsSection);
            }

        } catch (Exception e) {
            showError("Error loading songs: " + e.getMessage());
            e.printStackTrace();
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