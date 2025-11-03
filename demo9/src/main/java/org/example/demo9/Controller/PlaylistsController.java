package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.demo9.Model.Classes.Playlist;
import org.example.demo9.Model.Classes.SongNode;
import org.example.demo9.Model.Classes.User;
import org.example.demo9.Model.util.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PlaylistsController {
    @FXML private VBox playlistsContainer;
    private Playlist currentPlaylist;

    private User currentUser;
    private Database db;

    public PlaylistsController() {
        this.db = new Database();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadPlaylistsFromDatabase();
    }

    private void loadPlaylistsFromDatabase() {
        playlistsContainer.getChildren().clear();

        try {
            List<Playlist> playlists = currentUser.getPlaylistsFromDatabase();

            for (Playlist playlist : playlists) {
                addPlaylistCard(playlist);
            }

            if (playlists.isEmpty()) {
                Label emptyLabel = new Label("🎵 No playlists yet. Create your first playlist!");
                emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16; -fx-padding: 40;");
                playlistsContainer.getChildren().add(emptyLabel);
            }

        } catch (Exception e) {
            showError("Error loading playlists: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addPlaylistCard(Playlist playlist) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-border-color: #e0e0e0; -fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(600);

        Label icon = new Label("🎵");
        icon.setStyle("-fx-font-size: 28; -fx-padding: 5;");

        VBox info = new VBox(8);
        info.setPrefWidth(400);

        Label nameLabel = new Label(playlist.getName());
        nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label countLabel = new Label(playlist.getSize() + " songs • Created by " + currentUser.getUsername());
        countLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13;");

        info.getChildren().addAll(nameLabel, countLabel);

        HBox actions = new HBox(10);
        actions.setStyle("-fx-alignment: center-right;");

        Button viewBtn = createStyledButton("View Songs", "#3498db");
        viewBtn.setOnAction(e -> showSongsView(playlist));

        Button deleteBtn = createStyledButton("🗑Delete", "#e74c3c");
        deleteBtn.setOnAction(e -> deletePlaylistFromDatabase(playlist));

        actions.getChildren().addAll(viewBtn, deleteBtn);
        card.getChildren().addAll(icon, info, actions);
        playlistsContainer.getChildren().add(card);
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 8 12; -fx-background-radius: 8; -fx-border-radius: 8;");

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + color + ", -20%); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 8 12; -fx-background-radius: 8; -fx-border-radius: 8;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 8 12; -fx-background-radius: 8; -fx-border-radius: 8;"));

        return button;
    }

    private void showSongsView(Playlist playlist) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/sections/PlaylistSongs.fxml"));
            VBox playlistSongsSection = loader.load();

            PlaylistSongsController controller = loader.getController();
            controller.setPlaylistInfo(playlist.getId(), playlist.getName(), currentUser);

            StackPane contentArea = (StackPane) playlistsContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(playlistSongsSection);
            } else {
                showError("Content area not found!");
            }

        } catch (Exception e) {
            showError("Error loading playlist songs: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void showCreatePlaylistDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Playlist");
        dialog.setHeaderText("Create New Playlist");
        dialog.setContentText("Playlist Name:");
        dialog.getEditor().setStyle("-fx-font-size: 14;");

        dialog.showAndWait().ifPresent(playlistName -> {
            if (!playlistName.trim().isEmpty()) {
                createPlaylistInDatabase(playlistName.trim());
            }
        });
    }

    private void createPlaylistInDatabase(String playlistName) {
        try {
            boolean success = currentUser.createPlaylistInDatabase(playlistName);
            if (success) {
                showSuccess("Playlist '" + playlistName + "' created successfully!");
                loadPlaylistsFromDatabase();
            } else {
                showError("Failed to create playlist!");
            }
        } catch (Exception e) {
            showError("Error creating playlist: " + e.getMessage());
        }
    }

    private void deletePlaylistFromDatabase(Playlist playlist) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Playlist");
        alert.setHeaderText("Delete '" + playlist.getName() + "'?");
        alert.setContentText("This action cannot be undone. All songs in this playlist will be removed.");
        alert.getDialogPane().setStyle("-fx-background-color: white;");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String deleteSongsSql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
                String deletePlaylistSql = "DELETE FROM playlists WHERE id = ? AND user_id = ?";

                try (Connection conn = db.getConnection()) {
                    try (PreparedStatement stmt = conn.prepareStatement(deleteSongsSql)) {
                        stmt.setInt(1, playlist.getId());
                        stmt.executeUpdate();
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(deletePlaylistSql)) {
                        stmt.setInt(1, playlist.getId());
                        stmt.setInt(2, currentUser.getId());
                        stmt.executeUpdate();
                    }

                    showSuccess(" Playlist deleted successfully!");
                    loadPlaylistsFromDatabase();

                } catch (SQLException e) {
                    showError("Error deleting playlist: " + e.getMessage());
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
