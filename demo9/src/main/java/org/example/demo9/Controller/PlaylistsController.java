package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.demo9.Model.Classes.Playlist;
import org.example.demo9.Model.Classes.SongNode;
import org.example.demo9.Model.Classes.User;

public class PlaylistsController {
    @FXML private VBox playlistsContainer;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadPlaylists();
    }

    private void loadPlaylists() {
        playlistsContainer.getChildren().clear();

        for (Playlist playlist : currentUser.getPlaylists()) {
            addPlaylistCard(playlist);
        }

        if (currentUser.getPlaylists().isEmpty()) {
            Label emptyLabel = new Label("No playlists yet. Create your first playlist!");
            emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16; -fx-padding: 40;");
            playlistsContainer.getChildren().add(emptyLabel);
        }
    }

    private void addPlaylistCard(Playlist playlist) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; -fx-border-color: #e0e0e0; -fx-border-radius: 10;");
        card.setPrefWidth(600);

        Label icon = new Label("🎵");
        icon.setStyle("-fx-font-size: 24;");

        VBox info = new VBox(5);
        Label nameLabel = new Label(playlist.getName());
        nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label countLabel = new Label(playlist.getSize() + " songs");
        countLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");

        info.getChildren().addAll(nameLabel, countLabel);

        HBox actions = new HBox(10);

        Button viewBtn = new Button("View Songs");
        viewBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 8 15;");
        viewBtn.setOnAction(e -> viewPlaylistSongs(playlist));

        Button sortBtn = new Button("Sort");
        sortBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 8 15;");
        sortBtn.setOnAction(e -> {
            playlist.sortByTrackName();
            showSuccess("Playlist sorted successfully!");
        });

        Button reverseBtn = new Button("Reverse");
        reverseBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 8 15;");
        reverseBtn.setOnAction(e -> {
            playlist.reverse();
            showSuccess("Playlist reversed successfully!");
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 8 15;");
        deleteBtn.setOnAction(e -> deletePlaylist(playlist));

        actions.getChildren().addAll(viewBtn, sortBtn, reverseBtn, deleteBtn);
        card.getChildren().addAll(icon, info, actions);
        playlistsContainer.getChildren().add(card);
    }

    private void viewPlaylistSongs(Playlist playlist) {
        StringBuilder songsList = new StringBuilder();
        SongNode current = playlist.getHead();
        int index = 1;

        while (current != null) {
            songsList.append(index).append(". ").append(current.toString()).append("\n");
            current = current.getNext();
            index++;
        }

        TextArea textArea = new TextArea(songsList.toString());
        textArea.setEditable(false);
        textArea.setPrefSize(600, 400);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Songs in " + playlist.getName());
        alert.setHeaderText("Total songs: " + playlist.getSize());
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void showCreatePlaylistDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Playlist");
        dialog.setHeaderText("Create New Playlist");
        dialog.setContentText("Playlist Name:");

        dialog.showAndWait().ifPresent(playlistName -> {
            if (!playlistName.trim().isEmpty()) {
                currentUser.createPlaylist(playlistName.trim());
                showSuccess("Playlist '" + playlistName + "' created successfully!");
                loadPlaylists();
            }
        });
    }

    @FXML
    private void showAddSongDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Song");
        dialog.setHeaderText("Add New Song to Playlist");

        ChoiceDialog<String> playlistDialog = new ChoiceDialog<>();
        playlistDialog.setTitle("Select Playlist");
        playlistDialog.setHeaderText("Choose a playlist to add the song to");

        for (Playlist playlist : currentUser.getPlaylists()) {
            playlistDialog.getItems().add(playlist.getName());
        }

        playlistDialog.showAndWait().ifPresent(playlistName -> {
            Playlist selectedPlaylist = currentUser.getPlaylist(playlistName);
            if (selectedPlaylist != null) {
                // دریافت اطلاعات آهنگ از کاربر
                Dialog<SongNode> songDialog = createSongInputDialog();
                songDialog.showAndWait().ifPresent(song -> {
                    selectedPlaylist.addSong(song);
                    showSuccess("Song added to " + playlistName + " successfully!");
                });
            }
        });
    }

    private Dialog<SongNode> createSongInputDialog() {
        Dialog<SongNode> dialog = new Dialog<>();
        dialog.setTitle("Add Song");
        dialog.setHeaderText("Enter Song Details");

        // ایجاد فرم ورودی
        TextField artistField = new TextField();
        TextField trackField = new TextField();
        TextField yearField = new TextField();
        TextField genreField = new TextField();
        TextField lengthField = new TextField();
        TextField topicField = new TextField();

        VBox content = new VBox(10,
                new Label("Artist:"), artistField,
                new Label("Track Name:"), trackField,
                new Label("Release Year:"), yearField,
                new Label("Genre:"), genreField,
                new Label("Length (seconds):"), lengthField,
                new Label("Topic:"), topicField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    return new SongNode(
                            artistField.getText(),
                            trackField.getText(),
                            Integer.parseInt(yearField.getText()),
                            genreField.getText(),
                            Double.parseDouble(lengthField.getText()),
                            topicField.getText()
                    );
                } catch (NumberFormatException e) {
                    showError("Please enter valid numbers for year and length!");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private void deletePlaylist(Playlist playlist) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Playlist");
        alert.setHeaderText("Are you sure you want to delete this playlist?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                currentUser.getPlaylists().remove(playlist);
                showSuccess("Playlist deleted successfully!");
                loadPlaylists();
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