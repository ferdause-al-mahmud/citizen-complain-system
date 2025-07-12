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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * AdminDashboardController handles the admin dashboard functionality
 *
 * @author ferda
 */
public class AdminDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Label totalComplaintsLabel;

    @FXML
    private Label pendingComplaintsLabel;

    @FXML
    private Label resolvedComplaintsLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Button viewAllComplaintsButton;

    @FXML
    private Button manageUsersButton;

    @FXML
    private Button generateReportsButton;

    @FXML
    private Button viewAllPendingButton;

    @FXML
    private VBox recentComplaintsContainer;

    @FXML
    private VBox noComplaintsContainer;

    @FXML
    private Button userManagementButton;

    @FXML
    private Button reportsButton;

    @FXML
    private Button systemSettingsButton;

    @FXML
    private Button analyticsButton;

    private String currentAdminEmail;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize admin dashboard with sample data
        updateDashboardStats();
    }

    /**
     * Set the current admin email and update the welcome message
     */
    public void setCurrentAdmin(String adminEmail) {
        this.currentAdminEmail = adminEmail;
        welcomeLabel.setText("Welcome, Administrator!");

        // Log admin login
        System.out.println("Admin Dashboard loaded for: " + adminEmail);
    }

    /**
     * Update dashboard statistics (sample data for now)
     */
    private void updateDashboardStats() {
        // These would typically come from a database
        totalComplaintsLabel.setText("127");
        pendingComplaintsLabel.setText("23");
        resolvedComplaintsLabel.setText("104");
        totalUsersLabel.setText("89");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Log the logout action
            System.out.println("Admin logout: " + currentAdminEmail);

            // Load the login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setMaximized(false);  // First set to false

            // Set the login scene
            stage.setScene(new Scene(root));
            stage.setTitle("Login - Citizen Complaint System");
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);  // Then set to true
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to logout. Please try again.");
        }
    }

    @FXML
    private void viewAllComplaints(ActionEvent event) {
        System.out.println("Admin viewing all complaints");
        showAlert("Feature Coming Soon", "View All Complaints functionality will be implemented soon.");
    }

    @FXML
    private void manageUsers(ActionEvent event) {
        System.out.println("Admin managing users");
        showAlert("Feature Coming Soon", "User Management functionality will be implemented soon.");
    }

    @FXML
    private void generateReports(ActionEvent event) {
        System.out.println("Admin generating reports");
        showAlert("Feature Coming Soon", "Report Generation functionality will be implemented soon.");
    }

    @FXML
    private void viewAllPending(ActionEvent event) {
        System.out.println("Admin viewing all pending complaints");
        showAlert("Feature Coming Soon", "View All Pending Complaints functionality will be implemented soon.");
    }

    @FXML
    private void openSystemSettings(ActionEvent event) {
        System.out.println("Admin opening system settings");
        showAlert("Feature Coming Soon", "System Settings functionality will be implemented soon.");
    }

    @FXML
    private void viewAnalytics(ActionEvent event) {
        System.out.println("Admin viewing analytics");
        showAlert("Feature Coming Soon", "Analytics functionality will be implemented soon.");
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
