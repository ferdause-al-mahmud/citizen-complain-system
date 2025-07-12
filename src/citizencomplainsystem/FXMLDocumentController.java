/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML2.java to edit this template
 */
package citizencomplainsystem;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

/**
 *
 * @author ferda
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Hyperlink signUpLink;

    // Demo admin credentials
    private static final String ADMIN_EMAIL = "a@gmail.com";
    private static final String ADMIN_PASSWORD = "adminpass";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization code if needed
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation
        if (email.isEmpty() && password.isEmpty()) {
            showAlert("Validation Error", "Please fill in both email and password fields.");
            return;
        }

        if (email.isEmpty()) {
            showAlert("Validation Error", "Please enter your email address.");
            return;
        }

        if (password.isEmpty()) {
            showAlert("Validation Error", "Please enter your password.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Validation Error", "Please enter a valid email address.");
            return;
        }

        // Authenticate with MongoDB
        MongoDBConnector.connect(); // ensure connected
        Document user = MongoDBConnector.authenticateAndGetUser(email, password);
        System.out.println(user);

        if (user != null) {
            String role = user.getString("role");

            if ("admin".equalsIgnoreCase(role)) {
                System.out.println("=== ADMIN LOGIN SUCCESS ===");
                redirectToAdminDashboard(email);
            } else {
                System.out.println("=== USER LOGIN SUCCESS ===");
                redirectToUserDashboard(email);
            }
        } else {
            showAlert("Login Failed", "Invalid email or password.");
        }
    }

    private void redirectToAdminDashboard(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
            Parent root = loader.load();

            // Pass email to admin controller
            AdminDashboardController controller = loader.getController();
            controller.setCurrentAdmin(email);

            // Get the current stage and set new scene
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setMaximized(false);  // First set to false

            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard - Citizen Complaint System");
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);  // Then set to true
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load Admin Dashboard.");
        }
    }

    private void redirectToUserDashboard(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserDashboard.fxml"));
            Parent root = loader.load();

            // Pass email to user controller
            UserDashboardController controller = loader.getController();
            controller.setCurrentUser(email);

            // Get the current stage and set new scene
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setMaximized(false);  // First set to false

            stage.setScene(new Scene(root));
            stage.setTitle("User Dashboard - Citizen Complaint System");
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);  // Then set to true
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load User Dashboard.");
        }
    }

    @FXML
    private void openRegister(ActionEvent event) {
        try {
            // Load the register.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("register.fxml"));
            Parent root = loader.load();
            // Get the current stage
            Stage stage = (Stage) signUpLink.getScene().getWindow();

            // Force window state refresh
            stage.setMaximized(false);  // First set to false
            stage.setScene(new Scene(root));
            stage.setTitle("Register - Citizen Complaint System");

            // Use Platform.runLater to ensure proper timing
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);  // Then set to true
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open registration page. Please make sure register.fxml exists.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidEmail(String email) {
        // Basic email validation regex
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}
