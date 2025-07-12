/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML2.java to edit this template
 */
package citizencomplainsystem;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * FXML Controller class for User Dashboard
 *
 * @author ferda
 */
public class UserDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label totalComplaintsLabel;

    @FXML
    private Label pendingComplaintsLabel;

    @FXML
    private Label resolvedComplaintsLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button newComplaintButton;

    @FXML
    private Button viewComplaintsButton;

    @FXML
    private Button trackComplaintButton;

    @FXML
    private Button viewAllRecentButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button settingsButton;

    @FXML
    private VBox recentComplaintsContainer;

    @FXML
    private VBox noComplaintsContainer;

    private String currentUserEmail;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize dashboard data
        loadDashboardData();
        updateWelcomeMessage();
    }

    /**
     * Sets the current user email for personalized dashboard
     */
    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        updateWelcomeMessage();
    }

    /**
     * Updates the welcome message with user email
     */
    private void updateWelcomeMessage() {
        if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
            welcomeLabel.setText("Welcome, " + currentUserEmail + "!");
        } else {
            welcomeLabel.setText("Welcome, User!");
        }
    }

    /**
     * Loads dashboard data (complaints statistics)
     */
    private void loadDashboardData() {
        // TODO: Replace with actual database queries
        // For now, using sample data
        totalComplaintsLabel.setText("5");
        pendingComplaintsLabel.setText("2");
        resolvedComplaintsLabel.setText("3");

        // Show/hide no complaints message based on data
        boolean hasComplaints = !totalComplaintsLabel.getText().equals("0");
        noComplaintsContainer.setVisible(!hasComplaints);

        // TODO: Load recent complaints from database
        loadRecentComplaints();
    }

    /**
     * Loads recent complaints list
     */
    private void loadRecentComplaints() {
        // TODO: Replace with actual database query
        // For now, the sample complaint is shown in FXML
        System.out.println("Loading recent complaints for user: " + currentUserEmail);
    }

    /**
     * Handles logout action
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Load the login page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
                Parent root = loader.load();

                // Get the current stage
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setMaximized(false);  // First set to false

                // Set the login scene
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Login - Citizen Complaint System");
                javafx.application.Platform.runLater(() -> {
                    stage.setMaximized(true);  // Then set to true
                });

                System.out.println("User logged out successfully");

            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Error", "Could not return to login page.");
            }
        }
    }

    /**
     * Opens new complaint form
     */
    @FXML
    private void openNewComplaint(ActionEvent event) {
        try {
            // TODO: Create NewComplaint.fxml and controller
            System.out.println("Opening New Complaint form for user: " + currentUserEmail);
            showInfoAlert("New Complaint", "New Complaint form will be implemented soon!");

            // Placeholder for actual implementation:
            /*
            FXMLLoader loader = new FXMLLoader(getClass().getResource("NewComplaint.fxml"));
            Parent root = loader.load();
            
            NewComplaintController controller = loader.getController();
            controller.setCurrentUser(currentUserEmail);
            
            Stage stage = new Stage();
            stage.setTitle("New Complaint - Citizen Complaint System");
            stage.setScene(new Scene(root));
            stage.show();
             */
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open New Complaint form.");
        }
    }

    /**
     * Opens view all complaints page
     */
    @FXML
    private void viewAllComplaints(ActionEvent event) {
        try {
            // TODO: Create ViewComplaints.fxml and controller
            System.out.println("Opening View All Complaints for user: " + currentUserEmail);
            showInfoAlert("View Complaints", "View All Complaints page will be implemented soon!");

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open View Complaints page.");
        }
    }

    /**
     * Opens track complaint page
     */
    @FXML
    private void trackComplaint(ActionEvent event) {
        try {
            // TODO: Create TrackComplaint.fxml and controller
            System.out.println("Opening Track Complaint for user: " + currentUserEmail);
            showInfoAlert("Track Complaint", "Track Complaint feature will be implemented soon!");

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open Track Complaint page.");
        }
    }

    /**
     * Opens user profile page
     */
    @FXML
    private void openProfile(ActionEvent event) {
        try {
            // TODO: Create UserProfile.fxml and controller
            System.out.println("Opening Profile for user: " + currentUserEmail);
            showInfoAlert("User Profile", "User Profile management will be implemented soon!");

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open User Profile page.");
        }
    }

    /**
     * Opens help and support page
     */
    @FXML
    private void openHelp(ActionEvent event) {
        try {
            // TODO: Create Help.fxml and controller
            System.out.println("Opening Help & Support");
            showInfoAlert("Help & Support", "Help & Support section will be implemented soon!");

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open Help page.");
        }
    }

    /**
     * Opens settings page
     */
    @FXML
    private void openSettings(ActionEvent event) {
        try {
            // TODO: Create Settings.fxml and controller
            System.out.println("Opening Settings for user: " + currentUserEmail);
            showInfoAlert("Settings", "Settings page will be implemented soon!");

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open Settings page.");
        }
    }

    /**
     * Refreshes dashboard data
     */
    public void refreshDashboard() {
        loadDashboardData();
        System.out.println("Dashboard refreshed for user: " + currentUserEmail);
    }

    /**
     * Shows information alert
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows error alert
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
