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
    @FXML private Button playlistsBtn, discoverSongsBtn, mergeBtn, shuffleBtn, filterBtn, likedBtn, sortBtn;

    private User currentUser;
    private final Map<String, Parent> loadedSections = new HashMap<>();
    private Button activeButton;

    private final String defaultStyle = "sidebar-button";
    private final String activeStyle = "sidebar-button:selected";

    public void setCurrentUser(User user) {
        this.currentUser = user;
        userLabel.setText("👤 " + user.getUsername());
        initializeSidebar();
        showPlaylistsSection();
    }

    private void initializeSidebar() {
        Button[] buttons = {playlistsBtn, discoverSongsBtn, mergeBtn, shuffleBtn, filterBtn, likedBtn, sortBtn};

        for (Button btn : buttons) {
            btn.getStyleClass().clear();
            btn.getStyleClass().add(defaultStyle);

            btn.setOnMouseEntered(e -> {
                if (btn != activeButton) {
                    btn.setStyle(activeStyle);
                }
            });

            btn.setOnMouseExited(e -> {
                if (btn != activeButton) {
                    btn.setStyle("-fx-background-color: transparent;");
                }
            });
        }

        setActiveButton(playlistsBtn);
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().clear();
            activeButton.getStyleClass().add(defaultStyle);
            activeButton.setStyle("-fx-background-color: transparent;");
        }

        activeButton = button;
        activeButton.getStyleClass().clear();
        activeButton.getStyleClass().add(defaultStyle);
        activeButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
    }

    @FXML
    private void showPlaylistsSection() {
        setActiveButton(playlistsBtn);
        loadSection("PlaylistsSection");
    }

    @FXML
    private void showDiscoverSongsSection() {
        setActiveButton(discoverSongsBtn);
        loadSection("DiscoverSongsSection");
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/sections/" + sectionName + ".fxml"));
                section = loader.load();

                Object controller = loader.getController();
                if (controller instanceof PlaylistsController) {
                    ((PlaylistsController) controller).setCurrentUser(currentUser);
                } else if (controller instanceof SongsController) {
                    ((SongsController) controller).setCurrentUser(currentUser);
                } else if (controller instanceof DiscoverSongsController) {
                    ((DiscoverSongsController) controller).setCurrentUser(currentUser);
                } else if (controller instanceof MergeController) {
                    ((MergeController) controller).setCurrentUser(currentUser);
                } else if (controller instanceof ShuffleController) {
                    ((ShuffleController) controller).setCurrentUser(currentUser);
                } else if (controller instanceof FilterController) {
                    ((FilterController) controller).setCurrentUser(currentUser);
                } else if (controller instanceof SortController) {
                    ((SortController) controller).setCurrentUser(currentUser);
                } else if (controller instanceof LikedSongsController) {
                    ((LikedSongsController) controller).setCurrentUser(currentUser);
                }

                loadedSections.put(sectionName, section);
            }
            contentArea.getChildren().setAll(section);
        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error loading " + sectionName);
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16; -fx-padding: 20;");
            contentArea.getChildren().setAll(errorLabel);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.setScene(new Scene(loginRoot, 1200, 800));
            stage.setTitle("🎵 Music Playlist Manager");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
