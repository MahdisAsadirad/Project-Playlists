package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
        viewBtn.setOnAction(e -> showBeautifulSongsView(playlist));

        Button addSongBtn = createStyledButton("➕ Add Song", "#27ae60");
        addSongBtn.setOnAction(e -> showAddSongDialog());

        Button deleteBtn = createStyledButton("🗑Delete", "#e74c3c");
        deleteBtn.setOnAction(e -> deletePlaylistFromDatabase(playlist));

        actions.getChildren().addAll(viewBtn, addSongBtn, deleteBtn);
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

    private void showBeautifulSongsView(Playlist playlist) {
        try {
            this.currentPlaylist = playlist;

            Stage songsStage = new Stage();
            songsStage.initModality(Modality.APPLICATION_MODAL);
            songsStage.initStyle(StageStyle.DECORATED);
            songsStage.setTitle("🎵 " + playlist.getName() + " - Songs");

            VBox root = new VBox(20);
            root.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 30;");

            HBox header = new HBox(15);
            header.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

            Label playlistIcon = new Label("🎵");
            playlistIcon.setStyle("-fx-font-size: 32;");

            VBox headerInfo = new VBox(5);
            Label titleLabel = new Label(playlist.getName());
            titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            Label statsLabel = new Label("📊 " + playlist.getSize() + " songs • ⏱" + calculateTotalDuration(playlist) + " • 👤 " + currentUser.getUsername());
            statsLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14;");

            headerInfo.getChildren().addAll(titleLabel, statsLabel);
            header.getChildren().addAll(playlistIcon, headerInfo);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            scrollPane.setFitToWidth(true);

            VBox songsContainer = new VBox(10);
            songsContainer.setStyle("-fx-padding: 10;");

            SongNode current = playlist.getHead();
            int index = 1;

            while (current != null) {
                HBox songCard = createSongCard(current, index);
                songsContainer.getChildren().add(songCard);
                current = current.getNext();
                index++;
            }

            scrollPane.setContent(songsContainer);

            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
            closeBtn.setOnAction(e -> songsStage.close());

            root.getChildren().addAll(header, scrollPane, closeBtn);

            Scene scene = new Scene(root, 700, 600);
            songsStage.setScene(scene);
            songsStage.show();

        } catch (Exception e) {
            showError("Error showing songs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private HBox createSongCard(SongNode song, int index) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15; -fx-border-color: #ecf0f1; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);");
        card.setPrefWidth(650);

        Label numberLabel = new Label(String.valueOf(index));
        numberLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #3498db; -fx-min-width: 30; -fx-alignment: center;");

        Label songIcon = new Label("🎵");
        songIcon.setStyle("-fx-font-size: 20;");

        VBox songInfo = new VBox(5);
        songInfo.setPrefWidth(400);

        Label trackLabel = new Label(song.getTrackName());
        trackLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label artistLabel = new Label("🎤 " + song.getArtistName());
        artistLabel.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13;");

        HBox details = new HBox(15);
        details.setStyle("-fx-alignment: center-left;");

        Label genreLabel = new Label("🎵 " + song.getGenre());
        genreLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12; -fx-background-color: #ecf0f1; -fx-padding: 2 8; -fx-background-radius: 10;");

        Label yearLabel = new Label("📅 " + song.getReleaseDate());
        yearLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12; -fx-background-color: #ecf0f1; -fx-padding: 2 8; -fx-background-radius: 10;");

        Label durationLabel = new Label("⏱" + formatDuration(song.getLen()));
        durationLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12; -fx-background-color: #ecf0f1; -fx-padding: 2 8; -fx-background-radius: 10;");

        details.getChildren().addAll(genreLabel, yearLabel, durationLabel);
        songInfo.getChildren().addAll(trackLabel, artistLabel, details);

        HBox actions = new HBox(8);
        actions.setStyle("-fx-alignment: center-right;");

        Button playBtn = createSmallButton("▶", "#27ae60");
        playBtn.setOnAction(e -> playSong(song));

        Button likeBtn = createSmallButton("❤", "blue");
        likeBtn.setOnAction(e -> likeSong(song));


        Button removeBtn = createSmallButton("🗑", "#e74c3c");
        removeBtn.setTooltip(new Tooltip("Remove from playlist"));
        removeBtn.setOnAction(e -> removeSongFromPlaylist(song, currentPlaylist));

        actions.getChildren().addAll(playBtn, likeBtn, removeBtn);
        card.getChildren().addAll(numberLabel, songIcon, songInfo, actions);

        return card;
    }
    private Button createSmallButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 10; -fx-padding: 5 8; -fx-background-radius: 6; -fx-min-width: 40;");

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + color + ", -20%); -fx-text-fill: white; -fx-font-size: 10; -fx-padding: 5 8; -fx-background-radius: 6; -fx-min-width: 40;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 10; -fx-padding: 5 8; -fx-background-radius: 6; -fx-min-width: 40;"));

        return button;
    }

    private String calculateTotalDuration(Playlist playlist) {
        double totalSeconds = 0;
        SongNode current = playlist.getHead();
        while (current != null) {
            totalSeconds += current.getLen();
            current = current.getNext();
        }
        return formatDuration(totalSeconds);
    }

    private String formatDuration(double seconds) {
        int minutes = (int) (seconds / 60);
        int remainingSeconds = (int) (seconds % 60);
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    private void playSong(SongNode song) {
        showSuccess("Now playing: " + song.getTrackName() + " - " + song.getArtistName());
    }

    private void likeSong(SongNode song) {
        showSuccess("Added to favorites: " + song.getTrackName());
    }


    private void removeSongFromPlaylist(SongNode song, Playlist playlist) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Song");
        alert.setHeaderText("Remove '" + song.getTrackName() + "'?");
        alert.setContentText("Are you sure you want to remove this song from the playlist '" + playlist.getName() + "'?");

        alert.getDialogPane().setStyle("-fx-background-color: white;");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                removeSongFromDatabase(playlist, song);
            }
        });
    }

    private void removeSongFromDatabase(Playlist playlist, SongNode song) {
        String sql = "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlist.getId());
            stmt.setInt(2, song.getSongId());
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                showSuccess("Song '" + song.getTrackName() + "' removed from playlist!");

                playlist.loadFromDatabase(db); // بارگذاری مجدد از دیتابیس
                showBeautifulSongsView(playlist); // نمایش مجدد پنجره
            }

        } catch (SQLException e) {
            showError("Error removing song: " + e.getMessage());
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
                showSuccess("🎉 Playlist '" + playlistName + "' created successfully!");
                loadPlaylistsFromDatabase();
            } else {
                showError("Failed to create playlist!");
            }
        } catch (Exception e) {
            showError("Error creating playlist: " + e.getMessage());
        }
    }

    @FXML
    private void showAddSongDialog() {
        List<Playlist> playlists = currentUser.getPlaylistsFromDatabase();

        if (playlists.isEmpty()) {
            showError("No playlists available! Create a playlist first.");
            return;
        }

        ChoiceDialog<String> playlistDialog = new ChoiceDialog<>();
        playlistDialog.setTitle("Add Song");
        playlistDialog.setHeaderText("Choose a playlist to add the song to");
        playlistDialog.setContentText("Playlist:");

        for (Playlist playlist : playlists) {
            playlistDialog.getItems().add(playlist.getName());
        }

        playlistDialog.showAndWait().ifPresent(playlistName -> {
            Playlist selectedPlaylist = currentUser.getPlaylist(playlistName);
            if (selectedPlaylist != null) {
                showAvailableSongsDialog(selectedPlaylist);
            }
        });
    }

    private void showAvailableSongsDialog(Playlist playlist) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Song to " + playlist.getName());
        dialog.setHeaderText("Select a song to add to your playlist");
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

        ListView<String> songsList = new ListView<>();
        songsList.setStyle("-fx-font-size: 14; -fx-background-color: #f8f9fa;");

        String sql = "SELECT id, track_name, artist_name, genre FROM songs ORDER BY track_name";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String songInfo = "🎵 " + rs.getString("track_name") + " - " + rs.getString("artist_name") + " [" + rs.getString("genre") + "]";
                songsList.getItems().add(songInfo);
            }

        } catch (SQLException e) {
            showError("Error loading songs: " + e.getMessage());
            return;
        }

        songsList.setPrefSize(500, 300);
        dialog.getDialogPane().setContent(songsList);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String selectedSong = songsList.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    addSongToPlaylistInDatabase(playlist, selectedSong);
                }
            }
        });
    }

    private void addSongToPlaylistInDatabase(Playlist playlist, String songInfo) {
        String trackName = songInfo.split(" - ")[0].replace("🎵 ", "");

        String findSongSql = "SELECT id FROM songs WHERE track_name = ?";
        String insertSql = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) VALUES (?, ?, ?)";

        try (Connection conn = db.getConnection()) {
            int songId;
            try (PreparedStatement findStmt = conn.prepareStatement(findSongSql)) {
                findStmt.setString(1, trackName);
                ResultSet rs = findStmt.executeQuery();
                if (rs.next()) {
                    songId = rs.getInt("id");
                } else {
                    showError("Song not found!");
                    return;
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, playlist.getId());
                insertStmt.setInt(2, songId);
                insertStmt.setInt(3, currentUser.getId());
                insertStmt.executeUpdate();

                showSuccess("Song added to " + playlist.getName() + " successfully!");
                loadPlaylistsFromDatabase();
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                showError("This song is already in the playlist!");
            } else {
                showError("Error adding song: " + e.getMessage());
            }
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

                    showSuccess("🗑️ Playlist deleted successfully!");
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
