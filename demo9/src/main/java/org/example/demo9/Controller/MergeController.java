package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.demo9.Model.Classes.Playlist;
import org.example.demo9.Model.Classes.User;
import org.example.demo9.Model.util.Database;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MergeController implements Initializable {
    @FXML
    private ComboBox<String> firstPlaylistCombo;
    @FXML
    private ComboBox<String> secondPlaylistCombo;
    @FXML
    private TextField newPlaylistNameField;
    @FXML
    private Button mergeButton;
    @FXML
    private VBox resultContainer;

    private User currentUser;
    private final Database db;
    Playlist mergedPlaylist;

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
        String sql = "SELECT id, name FROM playlists WHERE user_id = ?";

        try (var conn = db.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());
            var rs = stmt.executeQuery();

            firstPlaylistCombo.getItems().clear();
            secondPlaylistCombo.getItems().clear();

            while (rs.next()) {
                String playlistName = rs.getString("name");
                firstPlaylistCombo.getItems().add(playlistName);
                secondPlaylistCombo.getItems().add(playlistName);
            }

        } catch (SQLException e) {
            showError("Error loading playlists: " + e.getMessage());
        }
    }

    @FXML
    private void handleMerge() {
        String firstPlaylistName = firstPlaylistCombo.getValue();
        String secondPlaylistName = secondPlaylistCombo.getValue();
        String newName = newPlaylistNameField.getText().trim();

        if (firstPlaylistName == null || secondPlaylistName == null || newName.isEmpty()) {
            showError("Please select both playlists and enter a name for the new playlist!");
            return;
        }

        if (firstPlaylistName.equals(secondPlaylistName)) {
            showError("Please select two different playlists!");
            return;
        }

        try {
            Playlist playlist1 = loadPlaylistFromDB(firstPlaylistName);
            Playlist playlist2 = loadPlaylistFromDB(secondPlaylistName);

            mergedPlaylist = playlist1.merge(playlist2, newName, db);

            showSuccess("Playlists merged successfully! \n" + "New playlist: " + newName +
                    " with " + mergedPlaylist.getSize() + " songs") ;

            loadUserPlaylists();
            clearForm();

        } catch (SQLException e) {
            showError("Error merging playlists: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Playlist loadPlaylistFromDB(String playlistName) throws SQLException {
        String sql = "SELECT id, name, user_id FROM playlists WHERE name = ? AND user_id = ?";
        try (var conn = db.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playlistName);
            stmt.setInt(2, currentUser.getId());
            var rs = stmt.executeQuery();

            if (rs.next()) {
                Playlist playlist = new Playlist(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("user_id")
                );
                playlist.loadFromDatabase(db);
                return playlist;
            }
            throw new SQLException("Playlist not found: " + playlistName);
        }
    }

    private void clearForm() {
        firstPlaylistCombo.setValue(null);
        secondPlaylistCombo.setValue(null);
        newPlaylistNameField.clear();
    }

    private void showSuccess(String message) {
        resultContainer.getChildren().clear();
        Label successLabel = new Label(message);
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
