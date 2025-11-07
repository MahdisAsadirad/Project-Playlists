package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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

public class DiscoverSongsController implements Initializable {
    @FXML
    private VBox discoverSongsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> genreFilter;
    @FXML
    private ComboBox<String> artistFilter;


    private User currentUser;
    private final Database db;

    public DiscoverSongsController() {
        this.db = new Database();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadAllSongs();
        setupFilters();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchField.setPromptText("Search songs...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchSongs(newValue);
        });

        genreFilter.setPromptText("Filter by Genre");
        artistFilter.setPromptText("Filter by Artist");

        genreFilter.setOnAction(e -> filterSongs());
        artistFilter.setOnAction(e -> filterSongs());
    }

    private void setupFilters() {

        String genreSql = "SELECT DISTINCT genre FROM songs ORDER BY genre";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(genreSql);
             ResultSet rs = stmt.executeQuery()) {

            genreFilter.getItems().clear();
            genreFilter.getItems().add("All Genres");
            while (rs.next()) {
                genreFilter.getItems().add(rs.getString("genre"));
            }
            genreFilter.setValue("All Genres");

        } catch (SQLException e) {
            showError("Error loading genres: " + e.getMessage());
        }


        String artistSql = "SELECT DISTINCT artist_name FROM songs ORDER BY artist_name";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(artistSql);
             ResultSet rs = stmt.executeQuery()) {

            artistFilter.getItems().clear();
            artistFilter.getItems().add("All Artists");
            while (rs.next()) {
                artistFilter.getItems().add(rs.getString("artist_name"));
            }
            artistFilter.setValue("All Artists");

        } catch (SQLException e) {
            showError("Error loading artists: " + e.getMessage());
        }
    }

    private void loadAllSongs() {
        discoverSongsContainer.getChildren().clear();

        String query = "SELECT id, track_name, artist_name, genre, release_date, len, topic FROM songs ORDER BY track_name";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                addDiscoverSongCard(rs.getInt("id"),
                        rs.getString("track_name"),
                        rs.getString("artist_name"),
                        rs.getString("genre"),
                        rs.getInt("release_date"),
                        rs.getDouble("len"),
                        rs.getString("topic"));
            }

        } catch (SQLException e) {
            showError("Error loading songs: " + e.getMessage());
        }
    }

    private void addDiscoverSongCard(int songId, String trackName, String artistName,
                                     String genre, int releaseDate, double length, String topic) {
        HBox card = new HBox(15);
        card.getStyleClass().add("song-card");

        Label icon = new Label("🎵");
        icon.setStyle("-fx-font-size: 20;");


        VBox songInfo = new VBox(5);
        songInfo.setPrefWidth(400);

        Label trackLabel = new Label(trackName);
        trackLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label artistLabel = new Label("by " + artistName);
        artistLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");

        Label detailsLabel = new Label(genre + " • " + releaseDate + " • " + length + "s • " + topic);
        detailsLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");

        songInfo.getChildren().addAll(trackLabel, artistLabel, detailsLabel);


        HBox actions = new HBox(10);


        Button likeButton = new Button("♡");
        likeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 16;");


        checkAndUpdateLikeButton(likeButton, songId);

        likeButton.setOnAction(e -> toggleLikeSong(songId, likeButton));


        Button addToPlaylistButton = new Button("Add to Playlist");
        addToPlaylistButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 8 15;");
        addToPlaylistButton.setOnAction(e -> showAddToPlaylistDialog(songId));

        actions.getChildren().addAll(likeButton, addToPlaylistButton);
        card.getChildren().addAll(icon, songInfo, actions);
        discoverSongsContainer.getChildren().add(card);
    }

    private void checkAndUpdateLikeButton(Button likeButton, int songId) {
        String checkSql = "SELECT COUNT(*) FROM liked_songs WHERE user_id = ? AND song_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {

            stmt.setInt(1, currentUser.getId());
            stmt.setInt(2, songId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                likeButton.setText("❤");
                likeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 16;");
            }

        } catch (SQLException e) {
            System.out.println("Error checking like status: " + e.getMessage());
        }
    }

    private void toggleLikeSong(int songId, Button likeButton) {
        String checkSql = "SELECT COUNT(*) FROM liked_songs WHERE user_id = ? AND song_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, currentUser.getId());
            checkStmt.setInt(2, songId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {

                String deleteSql = "DELETE FROM liked_songs WHERE user_id = ? AND song_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, currentUser.getId());
                    deleteStmt.setInt(2, songId);
                    deleteStmt.executeUpdate();

                    likeButton.setText("♡");
                    likeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 16;");
                    showSuccess("Song removed from liked songs!");
                }
            } else {

                String insertSql = "INSERT INTO liked_songs (user_id, song_id) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, currentUser.getId());
                    insertStmt.setInt(2, songId);
                    insertStmt.executeUpdate();

                    likeButton.setText("❤");
                    likeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 16;");
                    showSuccess("Song added to liked songs! ❤");
                }
            }

        } catch (SQLException e) {
            showError("Error toggling like status: " + e.getMessage());
        }
    }

    private void searchSongs(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadAllSongs();
            return;
        }

        discoverSongsContainer.getChildren().clear();

        String sql = "SELECT id, track_name, artist_name, genre, release_date, len, topic FROM songs " +
                "WHERE track_name LIKE ? OR artist_name LIKE ? OR genre LIKE ? " +
                "ORDER BY track_name";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchTerm = "%" + query + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                addDiscoverSongCard(rs.getInt("id"),
                        rs.getString("track_name"),
                        rs.getString("artist_name"),
                        rs.getString("genre"),
                        rs.getInt("release_date"),
                        rs.getDouble("len"),
                        rs.getString("topic"));
            }

        } catch (SQLException e) {
            showError("Error searching songs: " + e.getMessage());
        }
    }

    private void filterSongs() {
        String genre = genreFilter.getValue();
        String artist = artistFilter.getValue();

        String sql = "SELECT id, track_name, artist_name, genre, release_date, len, topic FROM songs WHERE 1=1";

        if (genre != null && !genre.equals("All Genres")) {
            sql += " AND genre = ?";
        }

        if (artist != null && !artist.equals("All Artists")) {
            sql += " AND artist_name = ?";
        }

        discoverSongsContainer.getChildren().clear();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (genre != null && !genre.equals("All Genres")) {
                stmt.setString(paramIndex++, genre);
            }


            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                addDiscoverSongCard(rs.getInt("id"),
                        rs.getString("track_name"),
                        rs.getString("artist_name"),
                        rs.getString("genre"),
                        rs.getInt("release_date"),
                        rs.getDouble("len"),
                        rs.getString("topic"));
            }

        } catch (SQLException e) {
            showError("Error filtering songs: " + e.getMessage());
        }
    }

    private void showAddToPlaylistDialog(int songId) {

        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Add to Playlist");
        dialog.setHeaderText("Select a playlist to add this song to");

        java.util.List<String> playlists = getUserPlaylists();
        dialog.getItems().addAll(playlists);

        dialog.showAndWait().ifPresent(playlistName -> {
            addSongToPlaylist(songId, playlistName);
        });
    }

    private List<String> getUserPlaylists() {
        List<String> playlists = new ArrayList<>();
        String sql = "SELECT name FROM playlists WHERE user_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                playlists.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            showError("Error loading playlists: " + e.getMessage());
        }

        return playlists;
    }

    private void addSongToPlaylist(int songId, String playlistName) {
        String findPlaylistSql = "SELECT id FROM playlists WHERE name = ? AND user_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement findStmt = conn.prepareStatement(findPlaylistSql)) {

            findStmt.setString(1, playlistName);
            findStmt.setInt(2, currentUser.getId());
            ResultSet rs = findStmt.executeQuery();

            if (rs.next()) {
                int playlistId = rs.getInt("id");

                String insertSql = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, playlistId);
                    insertStmt.setInt(2, songId);
                    insertStmt.setInt(3, currentUser.getId());
                    insertStmt.executeUpdate();

                    showSuccess("Song added to " + playlistName + " successfully!");
                }
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                showError("This song is already in the playlist!");
            } else {
                showError("Error adding song to playlist: " + e.getMessage());
            }
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