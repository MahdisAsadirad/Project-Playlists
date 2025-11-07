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
    @FXML
    private VBox sidebar;
    @FXML
    private StackPane contentArea;
    @FXML
    private Label userLabel;
    @FXML
    private Button playlistsBtn, discoverSongsBtn, mergeBtn, shuffleBtn, filterBtn, likedBtn, sortBtn;

    private User currentUser;
    private final Map<String, Parent> loadedSections = new HashMap<>();
    private Button activeButton;


    private void initializeSidebar() {
        Button[] buttons = {playlistsBtn, discoverSongsBtn, mergeBtn, shuffleBtn, filterBtn, likedBtn, sortBtn};

        for (Button btn : buttons) {
            String defaultStyle = "sidebar-button";
            String activeStyle = "sidebar-button:selected";
            btn.getStyleClass().removeAll(defaultStyle, activeStyle, "selected");
            if (!btn.getStyleClass().contains(defaultStyle)) {
                btn.getStyleClass().add(defaultStyle);
            }

            btn.setOnMouseExited(e -> {
                if (btn != activeButton) {
                    btn.setStyle("-fx-background-color: transparent;");
                }
            });
        }

        setActiveButton(playlistsBtn);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        userLabel.setText("👤 " + user.getUsername());
        initializeSidebar();
        showPlaylistsSection();
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
            activeButton.setStyle("-fx-background-color: transparent;");
        }

        activeButton = button;
        if (!activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active"); 
        }
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
                    ((PlaylistsController) controller).setDashboardController(this);

                } else if (controller instanceof DiscoverSongsController) {
                    ((DiscoverSongsController) controller).setCurrentUser(currentUser);

                } else if (controller instanceof MergeController) {
                    ((MergeController) controller).setCurrentUser(currentUser);
                    ((MergeController) controller).setDashboardController(this);

                } else if (controller instanceof ShuffleController) {
                    ((ShuffleController) controller).setCurrentUser(currentUser);
                    ((ShuffleController) controller).setDashboardController(this);

                } else if (controller instanceof FilterController) {
                    ((FilterController) controller).setCurrentUser(currentUser);
                    ((FilterController) controller).setDashboardController(this);

                } else if (controller instanceof SortController) {
                    ((SortController) controller).setCurrentUser(currentUser);
                    ((SortController) controller).setDashboardController(this);

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

    public void refreshPlaylists() {
        if (activeButton == playlistsBtn) {
            showPlaylistsSection();
        }
    }
}
