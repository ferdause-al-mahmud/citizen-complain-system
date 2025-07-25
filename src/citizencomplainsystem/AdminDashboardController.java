package citizencomplainsystem;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
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
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * AdminDashboardController handles the admin dashboard functionality
 * 
 * @author ferda
 */
public class AdminDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private Label totalComplaintsLabel;
    @FXML private Label pendingComplaintsLabel;
    @FXML private Label resolvedComplaintsLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Button viewAllComplaintsButton;
    @FXML private Button manageUsersButton;
    @FXML private Button generateReportsButton;
    @FXML private Button refreshButton;
    @FXML private VBox recentComplaintsContainer;
    @FXML private VBox noComplaintsContainer;

    private String currentAdminEmail;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize MongoDB connection
        MongoDBConnector.connect();
        
        // Load dashboard data
        loadDashboardData();
    }

    /**
     * Set the current admin email and update the welcome message
     */
    public void setCurrentAdmin(String adminEmail) {
        this.currentAdminEmail = adminEmail;
        welcomeLabel.setText("Welcome, Administrator!");
        System.out.println("Admin Dashboard loaded for: " + adminEmail);
        
        // Reload data for the admin
        loadDashboardData();
    }

    /**
     * Load real dashboard data from database
     */
    private void loadDashboardData() {
        try {
            // Get system statistics
            Document stats = MongoDBConnector.getSystemStats();
            
            // Update statistics labels
            totalComplaintsLabel.setText(String.valueOf(stats.getLong("totalComplaints")));
            pendingComplaintsLabel.setText(String.valueOf(stats.getLong("pendingComplaints")));
            resolvedComplaintsLabel.setText(String.valueOf(stats.getLong("resolvedComplaints")));
            totalUsersLabel.setText(String.valueOf(stats.getLong("totalUsers")));
            
            // Load recent complaints
            loadRecentComplaints();
            
            System.out.println("Admin dashboard data loaded successfully");
            
        } catch (Exception e) {
            System.err.println("Error loading admin dashboard data: " + e.getMessage());
            e.printStackTrace();
            showAlert("Data Loading Error", "Could not load dashboard data: " + e.getMessage());
        }
    }

    /**
     * Load recent complaints requiring attention
     */
    private void loadRecentComplaints() {
        try {
            // Clear existing complaints
            recentComplaintsContainer.getChildren().clear();
            
            // Get recent pending complaints (last 5)
            List<Document> recentComplaints = MongoDBConnector.getPendingComplaints();
            
            if (recentComplaints.isEmpty()) {
                noComplaintsContainer.setVisible(true);
                return;
            }
            
            noComplaintsContainer.setVisible(false);
            
            // Limit to 5 most recent
            int limit = Math.min(recentComplaints.size(), 5);
            for (int i = 0; i < limit; i++) {
                Document complaint = recentComplaints.get(i);
                HBox complaintItem = createRecentComplaintItem(complaint);
                recentComplaintsContainer.getChildren().add(complaintItem);
            }
            
            System.out.println("Loaded " + limit + " recent complaints for admin dashboard");
            
        } catch (Exception e) {
            System.err.println("Error loading recent complaints: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a recent complaint item for the dashboard
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

        // User and Date info
        String submittedDate = formatDateForDisplay(complaint.getString("submittedDate"));
        String userEmail = complaint.getString("userEmail");
        Label infoLabel = new Label("Submitted by: " + userEmail + " | Date: " + submittedDate);
        infoLabel.setTextFill(javafx.scene.paint.Color.web("#666666"));
        infoLabel.setFont(Font.font(12.0));

        // Complaint ID and Priority
        String complaintId = complaint.getString("complaintId");
        String priority = complaint.getString("priority");
        Label idLabel = new Label("ID: " + complaintId + " | Priority: " + priority);
        idLabel.setTextFill(javafx.scene.paint.Color.web("#888888"));
        idLabel.setFont(Font.font(11.0));

        detailsBox.getChildren().addAll(titleLabel, infoLabel, idLabel);

        // Right side - Status and action
        VBox actionBox = new VBox(5.0);
        actionBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // Status badge
        Label statusBadge = new Label(complaint.getString("status"));
        statusBadge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #f59e0b; " +
                           "-fx-background-radius: 15; -fx-padding: 5 10; -fx-font-size: 11px;");

        // Review button
        Button reviewButton = new Button("Review");
        reviewButton.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; " +
                            "-fx-background-radius: 5; -fx-font-size: 10px; -fx-padding: 5 10;");
        reviewButton.setOnAction(e -> reviewComplaint(complaintId));

        actionBox.getChildren().addAll(statusBadge, reviewButton);

        complaintItem.getChildren().addAll(detailsBox, actionBox);

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
     * Format date string for display
     */
    private String formatDateForDisplay(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "Unknown";
        }

        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateString);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return dateTime.format(formatter);
        } catch (DateTimeParseException e) {
            if (dateString.contains("T")) {
                return dateString.split("T")[0];
            }
            return dateString;
        }
    }

    /**
     * Review a specific complaint
     */
    private void reviewComplaint(String complaintId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminComplaintManagement.fxml"));
            Parent root = loader.load();
            
            AdminComplaintManagementController controller = loader.getController();
            controller.setCurrentAdmin(currentAdminEmail);
            controller.setComplaintToReview(complaintId);
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setMaximized(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Complaint Management - Admin Panel");
            
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not open complaint management: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            System.out.println("Admin logout: " + currentAdminEmail);
            
            // Close MongoDB connection
            MongoDBConnector.close();
            
            // Load the login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(new Scene(root));
            stage.setTitle("Login - Citizen Complaint System");
            
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to logout. Please try again.");
        }
    }

    @FXML
    private void viewAllComplaints(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminComplaintManagement.fxml"));
            Parent root = loader.load();
            
            AdminComplaintManagementController controller = loader.getController();
            controller.setCurrentAdmin(currentAdminEmail);
            
            Stage stage = (Stage) viewAllComplaintsButton.getScene().getWindow();
            stage.setMaximized(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Complaint Management - Admin Panel");
            
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not open complaint management: " + e.getMessage());
        }
    }

    @FXML
    private void manageUsers(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminUserManagement.fxml"));
            Parent root = loader.load();
            
            AdminUserManagementController controller = loader.getController();
            controller.setCurrentAdmin(currentAdminEmail);
            
            Stage stage = (Stage) manageUsersButton.getScene().getWindow();
            stage.setMaximized(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("User Management - Admin Panel");
            
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not open user management: " + e.getMessage());
        }
    }

    @FXML
    private void generateReports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminReports.fxml"));
            Parent root = loader.load();
            
            AdminReportsController controller = loader.getController();
            controller.setCurrentAdmin(currentAdminEmail);
            
            Stage stage = (Stage) generateReportsButton.getScene().getWindow();
            stage.setMaximized(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Reports - Admin Panel");
            
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not open reports: " + e.getMessage());
        }
    }

    @FXML
    private void refreshDashboard(ActionEvent event) {
        loadDashboardData();
        showAlert("Refresh Complete", "Dashboard data has been refreshed successfully!");
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Get current admin email
     */
    public String getCurrentAdminEmail() {
        return currentAdminEmail;
    }
}
