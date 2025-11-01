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
import java.util.*;

public class ShuffleController implements Initializable {
    @FXML private ListView<String> playlistsListView;
    @FXML private TextField newPlaylistNameField;
    @FXML private Button shuffleButton;
    @FXML private VBox resultContainer;

    private User currentUser;
    private Database db;

    public ShuffleController() {
        this.db = new Database();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserPlaylists();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        shuffleButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        shuffleButton.setOnAction(e -> handleShuffleMerge());


        playlistsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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

            playlistsListView.getItems().setAll(playlists);

        } catch (SQLException e) {
            showError("Error loading playlists: " + e.getMessage());
        }
    }

    @FXML
    private void handleShuffleMerge() {
        List<String> selectedPlaylists = playlistsListView.getSelectionModel().getSelectedItems();
        String newName = newPlaylistNameField.getText().trim();

        if (selectedPlaylists.size() < 2) {
            showError("Please select at least 2 playlists!");
            return;
        }

        if (newName.isEmpty()) {
            showError("Please enter a name for the shuffled playlist!");
            return;
        }

        try {
            createShuffledPlaylist(selectedPlaylists, newName);
        } catch (SQLException e) {
            showError("Error creating shuffled playlist: " + e.getMessage());
        }
    }

    private void createShuffledPlaylist(List<String> playlistNames, String newName) throws SQLException {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            try {

                Set<Integer> allSongIds = new HashSet<>();
                Map<Integer, String> songDetails = new HashMap<>();

                for (String playlistName : playlistNames) {
                    int playlistId = getPlaylistId(conn, playlistName);
                    loadSongsFromPlaylist(conn, playlistId, allSongIds, songDetails);
                }


                List<Integer> shuffledSongIds = new ArrayList<>(allSongIds);
                Collections.shuffle(shuffledSongIds);


                int newPlaylistId = createNewPlaylist(conn, newName);


                addSongsToPlaylist(conn, newPlaylistId, shuffledSongIds);

                conn.commit();

                showSuccess("Shuffled playlist created successfully! \n" +
                        "Merged " + playlistNames.size() + " playlists with " +
                        shuffledSongIds.size() + " unique songs!");
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

    private void loadSongsFromPlaylist(Connection conn, int playlistId,
                                       Set<Integer> songIds, Map<Integer, String> songDetails) throws SQLException {
        String sql = "SELECT s.id, s.track_name, s.artist_name " +
                "FROM playlist_songs ps " +
                "JOIN songs s ON ps.song_id = s.id " +
                "WHERE ps.playlist_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int songId = rs.getInt("id");
                songIds.add(songId);
                songDetails.put(songId, rs.getString("track_name") + " - " + rs.getString("artist_name"));
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

    private void addSongsToPlaylist(Connection conn, int playlistId, List<Integer> songIds) throws SQLException {
        String sql = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int songId : songIds) {
                stmt.setInt(1, playlistId);
                stmt.setInt(2, songId);
                stmt.setInt(3, currentUser.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void clearForm() {
        playlistsListView.getSelectionModel().clearSelection();
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