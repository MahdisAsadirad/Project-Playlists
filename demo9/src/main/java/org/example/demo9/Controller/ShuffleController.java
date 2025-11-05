package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.demo9.Model.Classes.Playlist;
import org.example.demo9.Model.Classes.SongNode;
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
    @FXML private Button reshuffleButton;
    @FXML private VBox resultContainer;
    @FXML private VBox shuffledSongsContainer;

    private User currentUser;
    private final Database db;
    private Playlist currentShuffledPlaylist;
    private int currentShuffledPlaylistId;
    private final Random random;

    public ShuffleController() {
        this.db = new Database();
        this.random = new Random();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserPlaylists();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        shuffleButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        reshuffleButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");

        shuffleButton.setOnAction(e -> handleShuffleMerge());
        reshuffleButton.setOnAction(e -> reshufflePlaylist());

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
            e.printStackTrace();
        }
    }

    private void createShuffledPlaylist(List<String> playlistNames, String newName) throws SQLException {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                List<SongNode> allSongs = new ArrayList<>();

                for (String playlistName : playlistNames) {
                    int playlistId = getPlaylistId(conn, playlistName);
                    List<SongNode> songs = loadSongsFromPlaylist(conn, playlistId);
                    allSongs.addAll(songs);
                }

                List<SongNode> shuffledSongs = shuffleWithRandom(allSongs);

                int newPlaylistId = createNewPlaylist(conn, newName);

                addShuffledSongsToPlaylist(conn, newPlaylistId, shuffledSongs);

                currentShuffledPlaylist = new Playlist(newPlaylistId, newName, currentUser.getId());
                currentShuffledPlaylistId = newPlaylistId;

                for (SongNode song : shuffledSongs) {
                    currentShuffledPlaylist.addSongToLinkedList(new SongNode(song));
                }

                conn.commit();

                showSuccess("Shuffled playlist created successfully! \n" +
                        "Merged " + playlistNames.size() + " playlists with " +
                        shuffledSongs.size() + " songs in RANDOM order!");

                clearForm();
                displayShuffledSongs();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private List<SongNode> shuffleWithRandom(List<SongNode> songs) {
        List<SongNode> shuffled = new ArrayList<>(songs);

        for (int i = shuffled.size() - 1; i > 0; i--) {
            int randomIndex = random.nextInt(i + 1);
            SongNode temp = shuffled.get(i);
            shuffled.set(i, shuffled.get(randomIndex));
            shuffled.set(randomIndex, temp);
        }

        return shuffled;
    }

    private void addShuffledSongsToPlaylist(Connection conn, int playlistId, List<SongNode> shuffledSongs) throws SQLException {
        String sql = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (SongNode song : shuffledSongs) {
                stmt.setInt(1, playlistId);
                stmt.setInt(2, song.getSongId());
                stmt.setInt(3, currentUser.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @FXML
    private void reshufflePlaylist() {
        if (currentShuffledPlaylist == null) {
            showError("No shuffled playlist available! Create one first.");
            return;
        }

        try {
            reshuffleExistingPlaylist();
        } catch (SQLException e) {
            showError("Error reshuffling playlist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reshuffleExistingPlaylist() throws SQLException {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String deleteSql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, currentShuffledPlaylistId);
                    deleteStmt.executeUpdate();
                }

                List<SongNode> currentSongs = new ArrayList<>();
                SongNode current = currentShuffledPlaylist.getHead();
                while (current != null) {
                    currentSongs.add(new SongNode(current));
                    current = current.getNext();
                }

                List<SongNode> reshuffledSongs = shuffleWithRandom(currentSongs);

                addShuffledSongsToPlaylist(conn, currentShuffledPlaylistId, reshuffledSongs);

                currentShuffledPlaylist.clear();
                for (SongNode song : reshuffledSongs) {
                    currentShuffledPlaylist.addSongToLinkedList(new SongNode(song));
                }

                conn.commit();

                showSuccess("🔄 Playlist reshuffled successfully!");
                displayShuffledSongs();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private List<SongNode> loadSongsFromPlaylist(Connection conn, int playlistId) throws SQLException {
        List<SongNode> songs = new ArrayList<>();
        String sql = "SELECT s.id, s.track_name, s.artist_name, s.release_date, s.genre, s.len, s.topic " +
                "FROM playlist_songs ps " +
                "JOIN songs s ON ps.song_id = s.id " +
                "WHERE ps.playlist_id = ? " +
                "ORDER BY ps.user_id";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SongNode song = new SongNode(
                        rs.getInt("id"),
                        rs.getString("artist_name"),
                        rs.getString("track_name"),
                        rs.getInt("release_date"),
                        rs.getString("genre"),
                        rs.getDouble("len"),
                        rs.getString("topic")
                );
                songs.add(song);
            }
        }
        return songs;
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

    private void displayShuffledSongs() {

        if (currentShuffledPlaylist == null) {
            showError("No shuffled playlist available! Create one first.");
            return;
        }

        shuffledSongsContainer.getChildren().clear();

        Label titleLabel = new Label("Shuffled Songs in: " + currentShuffledPlaylist.getName());
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 0 0 15 0;");
        shuffledSongsContainer.getChildren().add(titleLabel);

        SongNode current = currentShuffledPlaylist.getHead();
        int position = 1;

        if (current == null) {
            Label emptyLabel = new Label("No songs in shuffled playlist!");
            emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16; -fx-padding: 20;");
            shuffledSongsContainer.getChildren().add(emptyLabel);
            return;
        }

        while (current != null) {
            HBox songCard = createSongCard(current, position);
            shuffledSongsContainer.getChildren().add(songCard);
            current = current.getNext();
            position++;
        }

        Label infoLabel = new Label("Displaying " + (position - 1) + " shuffled songs");
        infoLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");
        shuffledSongsContainer.getChildren().add(infoLabel);
    }

    private HBox createSongCard(SongNode song, int position) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-pref-width: 750; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label positionLabel = new Label(position + ".");
        positionLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #9b59b6; -fx-min-width: 30;");

        Label icon = new Label("🎵");
        icon.setStyle("-fx-font-size: 16;");

        VBox songInfo = new VBox(5);
        songInfo.setPrefWidth(600);

        Label trackLabel = new Label(song.getTrackName());
        trackLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #333;");
        trackLabel.setWrapText(true);

        Label artistLabel = new Label("by " + song.getArtistName());
        artistLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        artistLabel.setWrapText(true);

        Label detailsLabel = new Label(String.format("%s • %d • %.1fs • %s",
                song.getGenre(), song.getReleaseDate(), song.getLen(), song.getTopic()));
        detailsLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");
        detailsLabel.setWrapText(true);

        songInfo.getChildren().addAll(trackLabel, artistLabel, detailsLabel);

        Button playButton = new Button("▶ Play");
        playButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-font-size: 11; -fx-padding: 5 10; -fx-background-radius: 5;");
        playButton.setOnAction(e -> playSong(song.getTrackName(), song.getArtistName()));

        card.getChildren().addAll(positionLabel, icon, songInfo, playButton);
        return card;
    }

    private void playSong(String trackName, String artistName) {
        showSuccess("Now playing: " + trackName + " - " + artistName);
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