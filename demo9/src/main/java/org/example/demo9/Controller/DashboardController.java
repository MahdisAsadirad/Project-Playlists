package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.demo9.Model.Classes.User;

import java.util.HashMap;
import java.util.Map;

public class DashboardController {
    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private Label userLabel;
    @FXML private Button playlistsBtn, songsBtn, mergeBtn, shuffleBtn, filterBtn, likedBtn, sortBtn;

    private User currentUser;
    private Map<String, Parent> loadedSections = new HashMap<>();
    private Button activeButton;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        userLabel.setText("" + user.getUsername());
        initializeSidebar();
        showPlaylistsSection();
    }

    private void initializeSidebar() {

        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #333; -fx-font-size: 14; -fx-pref-width: 100%; -fx-alignment: center-left; -fx-padding: 12 15;";
        String activeStyle = "-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14; -fx-pref-width: 100%; -fx-alignment: center-left; -fx-padding: 12 15;";

        playlistsBtn.setStyle(activeStyle);
        activeButton = playlistsBtn;


        Button[] buttons = {playlistsBtn, songsBtn, mergeBtn, shuffleBtn, filterBtn, likedBtn, sortBtn};
        for (Button btn : buttons) {
            btn.setOnMouseEntered(e -> {
                if (btn != activeButton) {
                    btn.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #333; -fx-font-size: 14; -fx-pref-width: 100%; -fx-alignment: center-left; -fx-padding: 12 15;");
                }
            });

            btn.setOnMouseExited(e -> {
                if (btn != activeButton) {
                    btn.setStyle(defaultStyle);
                }
            });
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-font-size: 14; -fx-pref-width: 100%; -fx-alignment: center-left; -fx-padding: 12 15;");
        }
        activeButton = button;
        activeButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14; -fx-pref-width: 100%; -fx-alignment: center-left; -fx-padding: 12 15;");
    }

    @FXML
    private void showPlaylistsSection() {
        setActiveButton(playlistsBtn);
        loadSection("PlaylistsSection");
    }

    @FXML
    private void showSongsSection() {
        setActiveButton(songsBtn);
        loadSection("SongsSection");
    }

    @FXML
    private void showMergeSection() {
        setActiveButton(mergeBtn);
        loadSection("MergeSection");
    }

    @FXML
    private void showShuffleSection() {
        setActiveButton(shuffleBtn);
        loadSection("ShuffleSection");
    }

    @FXML
    private void showFilterSection() {
        setActiveButton(filterBtn);
        loadSection("FilterSection");
    }

    @FXML
    private void showLikedSection() {
        setActiveButton(likedBtn);
        loadSection("LikedSection");
    }

    @FXML
    private void showSortSection() {
        setActiveButton(sortBtn);
        loadSection("SortSection");
    }

    private void loadSection(String sectionName) {
        try {
            Parent section = loadedSections.get(sectionName);
            if (section == null) {
                section = FXMLLoader.load(getClass().getResource("/views/sections/" + sectionName + ".fxml"));
                loadedSections.put(sectionName, section);
            }
            contentArea.getChildren().setAll(section);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/views/sections/Login.fxml"));
            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.setScene(new Scene(loginRoot, 900, 600));
            stage.setTitle("🎵 Music Playlist Manager");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }
}