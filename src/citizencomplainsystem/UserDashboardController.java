/* 
 * Updated User Dashboard Controller with Real Data Integration
 */
package citizencomplainsystem;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * FXML Controller class for User Dashboard with Real Data Integration
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
    private Label inProgressComplaintsLabel;
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
        // Initialize MongoDB connection
        MongoDBConnector.connect();
        
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
        loadDashboardData(); // Reload data for the specific user
    }

    /**
     * Updates the welcome message with user email
     */
    private void updateWelcomeMessage() {
        if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
            // Get user's full name from database
            Document user = MongoDBConnector.getUserByEmail(currentUserEmail);
            if (user != null && user.containsKey("fullName")) {
                welcomeLabel.setText("Welcome, " + user.getString("fullName") + "!");
            } else {
                welcomeLabel.setText("Welcome, " + currentUserEmail + "!");
            }
        } else {
            welcomeLabel.setText("Welcome, User!");
        }
    }

    /**
     * Loads real dashboard data from MongoDB
     */
    private void loadDashboardData() {
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            return;
        }

        try {
            // Get complaint statistics from database
            Document stats = MongoDBConnector.getUserComplaintStats(currentUserEmail);
            
            // Update labels with real data
            totalComplaintsLabel.setText(String.valueOf(stats.getLong("total")));
            pendingComplaintsLabel.setText(String.valueOf(stats.getLong("pending")));
            resolvedComplaintsLabel.setText(String.valueOf(stats.getLong("resolved")));
            
            // Add in-progress label if it exists in your FXML
            if (inProgressComplaintsLabel != null) {
                inProgressComplaintsLabel.setText(String.valueOf(stats.getLong("inProgress")));
            }

            // Show/hide no complaints message based on real data
            boolean hasComplaints = stats.getLong("total") > 0;
            if (noComplaintsContainer != null) {
                noComplaintsContainer.setVisible(!hasComplaints);
            }
            if (recentComplaintsContainer != null) {
                recentComplaintsContainer.setVisible(hasComplaints);
            }

            // Load recent complaints
            loadRecentComplaints();

        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Data Loading Error", "Could not load dashboard data: " + e.getMessage());
        }
    }

    /**
     * Loads recent complaints from database and displays them
     */
    private void loadRecentComplaints() {
        if (currentUserEmail == null || recentComplaintsContainer == null) {
            return;
        }

        try {
            // Clear existing complaints (remove all children except the no complaints container)
            recentComplaintsContainer.getChildren().removeIf(node -> 
                node != noComplaintsContainer);

            // Get recent complaints from database (last 5)
            List<Document> recentComplaints = MongoDBConnector.getRecentUserComplaints(currentUserEmail, 5);

            if (recentComplaints.isEmpty()) {
                // Show no complaints message
                if (noComplaintsContainer != null) {
                    noComplaintsContainer.setVisible(true);
                }
                return;
            }

            // Hide no complaints message
            if (noComplaintsContainer != null) {
                noComplaintsContainer.setVisible(false);
            }

            // Create UI elements for each complaint
            for (Document complaint : recentComplaints) {
                HBox complaintItem = createRecentComplaintItem(complaint);
                recentComplaintsContainer.getChildren().add(complaintItem);
            }

            System.out.println("Loaded " + recentComplaints.size() + " recent complaints for user: " + currentUserEmail);

        } catch (Exception e) {
            System.err.println("Error loading recent complaints: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a recent complaint item matching the FXML design
     */
    private HBox createRecentComplaintItem(Document complaint) {
        HBox complaintItem = new HBox(15.0);
        complaintItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        complaintItem.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 15;");

        // Left side - Complaint details
        VBox detailsBox = new VBox(5.0);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);

        // Title
        Label titleLabel = new Label(complaint.getString("title"));
        titleLabel.setTextFill(javafx.scene.paint.Color.web("#333333"));
        titleLabel.setFont(Font.font("System Bold", 14.0));

        // Date and Status info
        String submittedDate = formatDateForDisplay(complaint.getString("submittedDate"));
        String status = complaint.getString("status");
        Label infoLabel = new Label("Submitted on: " + submittedDate + " | Status: " + status);
        infoLabel.setTextFill(javafx.scene.paint.Color.web("#666666"));
        infoLabel.setFont(Font.font(12.0));

        // Complaint ID
        Label idLabel = new Label("Complaint ID: " + complaint.getString("complaintId"));
        idLabel.setTextFill(javafx.scene.paint.Color.web("#888888"));
        idLabel.setFont(Font.font(11.0));

        detailsBox.getChildren().addAll(titleLabel, infoLabel, idLabel);

        // Right side - Status badge
        Label statusBadge = new Label(status);
        statusBadge.setStyle(getStatusBadgeStyle(status));
        statusBadge.setFont(Font.font(11.0));

        complaintItem.getChildren().addAll(detailsBox, statusBadge);

        // Add click handler to track complaint
        complaintItem.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double click to track
                trackSpecificComplaint(complaint.getString("complaintId"));
            }
        });

        // Add hover effect
        complaintItem.setOnMouseEntered(event -> {
            complaintItem.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand;");
        });

        complaintItem.setOnMouseExited(event -> {
            complaintItem.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 15;");
        });

        return complaintItem;
    }

    /**
     * Gets the appropriate style for status badge matching FXML design
     */
    private String getStatusBadgeStyle(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "-fx-background-color: #fef3c7; -fx-text-fill: #f59e0b; -fx-background-radius: 15; -fx-padding: 5 10;";
            case "in progress":
                return "-fx-background-color: #dbeafe; -fx-text-fill: #3b82f6; -fx-background-radius: 15; -fx-padding: 5 10;";
            case "resolved":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #10b981; -fx-background-radius: 15; -fx-padding: 5 10;";
            case "rejected":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-background-radius: 15; -fx-padding: 5 10;";
            default:
                return "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-background-radius: 15; -fx-padding: 5 10;";
        }
    }

    /**
     * Formats date string for display
     */
    private String formatDateForDisplay(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "Unknown";
        }

        try {
            // Try to parse as LocalDateTime
            LocalDateTime dateTime = LocalDateTime.parse(dateString);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return dateTime.format(formatter);
        } catch (DateTimeParseException e) {
            // If parsing fails, try to extract just the date part
            if (dateString.contains("T")) {
                return dateString.split("T")[0];
            }
            return dateString;
        }
    }

    /**
     * Tracks a specific complaint
     */
    private void trackSpecificComplaint(String complaintId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TrackComplaint.fxml"));
            Parent root = loader.load();
            
            TrackComplaintController controller = loader.getController();
            controller.setCurrentUser(currentUserEmail);
            controller.setComplaintIdToTrack(complaintId);
            
            Stage stage = (Stage) trackComplaintButton.getScene().getWindow();
            stage.setMaximized(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Track Complaint - Citizen Complaint System");
            
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not open Track Complaint page: " + e.getMessage());
        }
    }

    /**
     * Handles logout action
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Close MongoDB connection
                MongoDBConnector.close();
                
                // Load the login page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setMaximized(false);
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Login - Citizen Complaint System");
                
                javafx.application.Platform.runLater(() -> {
                    stage.setMaximized(true);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("NewComplaint.fxml"));
            Parent root = loader.load();
            
            NewComplaintController newComplaintController = loader.getController();
            newComplaintController.setCurrentUser(currentUserEmail);
            
            Stage stage = (Stage) newComplaintButton.getScene().getWindow();
            stage.setMaximized(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("New Complaint - Citizen Complaint System");
            
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });
            
            System.out.println("Opened new complaint form for user: " + currentUserEmail);
            
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not open new complaint form: " + e.getMessage());
        }
    }

    /**
     * Opens view all complaints page
     */
    @FXML
    private void viewAllComplaints(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewAllComplaints.fxml"));
            Parent root = loader.load();
            
            ViewAllComplaintsController controller = loader.getController();
            controller.setCurrentUser(currentUserEmail);
            
            Stage stage = (Stage) viewComplaintsButton.getScene().getWindow();
            stage.setMaximized(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("View All Complaints - Citizen Complaint System");
            
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });
            
            System.out.println("Opening View All Complaints for user: " + currentUserEmail);
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open View Complaints page: " + e.getMessage());
        }
    }

    /**
     * Opens track complaint page
     */
    @FXML
    private void trackComplaint(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TrackComplaint.fxml"));
            Parent root = loader.load();
            
            TrackComplaintController controller = loader.getController();
            controller.setCurrentUser(currentUserEmail);
            
            Stage stage = (Stage) trackComplaintButton.getScene().getWindow();
            stage.setMaximized(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Track Complaint - Citizen Complaint System");
            
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });
            
            System.out.println("Opening Track Complaint for user: " + currentUserEmail);
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open Track Complaint page: " + e.getMessage());
        }
    }

    /**
     * Opens user profile page
     */
    @FXML
    private void openProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserProfile.fxml"));
            Parent root = loader.load();
            
            UserProfileController profileController = loader.getController();
            profileController.setCurrentUser(currentUserEmail);
            
            Stage stage = (Stage) profileButton.getScene().getWindow();
            stage.setMaximized(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("User Profile - Citizen Complaint System");
            
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });
            
            System.out.println("Opened User Profile for user: " + currentUserEmail);
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", "Could not open User Profile page: " + e.getMessage());
        }
    }

    /**
     * Opens help and support page
     */
    @FXML
    private void openHelp(ActionEvent event) {
        showInfoAlert("Help & Support", 
            "For assistance with the Citizen Complaint System:\n\n" +
            "• Email: support@citizencomplaint.gov\n" +
            "• Phone: 1-800-CITIZEN\n" +
            "• Online Help: www.citizencomplaint.gov/help\n\n" +
            "Office Hours: Monday-Friday, 8:00 AM - 6:00 PM\n\n" +
            "Quick Tips:\n" +
            "• Double-click on recent complaints to track them\n" +
            "• Use 'View All Complaints' to see complete history\n" +
            "• Track complaints using their unique ID");
    }

    /**
     * Opens settings page
     */
    @FXML
    private void openSettings(ActionEvent event) {
        showInfoAlert("Settings", "Settings page will be implemented soon!\n\n" +
                     "Planned features:\n" +
                     "• Notification preferences\n" +
                     "• Privacy settings\n" +
                     "• Account preferences\n" +
                     "• Theme selection\n" +
                     "• Email notifications\n" +
                     "• Dashboard customization");
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
