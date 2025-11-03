package org.example.demo9.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.demo9.Model.Classes.User;
import org.example.demo9.Model.util.Database;

import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button signupButton;
    @FXML
    private Label errorLabel;

    private final UserController userController;

    public LoginController() {
        Database db = new Database();
        this.userController = new UserController(db);
    }

    @FXML
    public void initialize() {
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #5a6fd8; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 300; -fx-pref-height: 45; -fx-background-radius: 8;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 300; -fx-pref-height: 45; -fx-background-radius: 8;"));

        signupButton.setOnMouseEntered(e -> signupButton.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #667eea; -fx-font-weight: bold; -fx-border-color: #667eea; -fx-pref-width: 300; -fx-pref-height: 45; -fx-background-radius: 8; -fx-border-radius: 8;"));
        signupButton.setOnMouseExited(e -> signupButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #667eea; -fx-font-weight: bold; -fx-border-color: #667eea; -fx-pref-width: 300; -fx-pref-height: 45; -fx-background-radius: 8; -fx-border-radius: 8;"));
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password!");
            return;
        }

        try {
            User user = userController.login(username, password);
            if (user != null) {
                loadMainDashboard(user);
            } else {
                showError("Invalid username or password!");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password!");
            return;
        }

        try {
            if (userController.signUp(username, password)) {
                showSuccess();
                clearFields();
            } else {
                showError("Sign up failed! Username might already exist.");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void loadMainDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setCurrentUser(user);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("🎵 Dashboard - " + user.getUsername());

        } catch (Exception e) {
            showError("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Sign up successful! Please login.");
        alert.showAndWait();
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        errorLabel.setVisible(false);
    }
}