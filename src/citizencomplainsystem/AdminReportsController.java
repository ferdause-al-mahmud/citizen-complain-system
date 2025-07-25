package citizencomplainsystem;

import java.io.IOException;
import java.net.URL;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * Controller for Admin Reports
 */
public class AdminReportsController implements Initializable {

    @FXML private Button backButton;
    @FXML private Button categoryReportButton;
    @FXML private Button statusReportButton;
    @FXML private Button refreshReportsButton;
    @FXML private VBox categoryReportSection;
    @FXML private VBox categoryReportContainer;
    @FXML private VBox statusReportSection;
    @FXML private VBox statusReportContainer;
    @FXML private Label totalComplaintsLabel;
    @FXML private Label pendingComplaintsLabel;
    @FXML private Label resolvedComplaintsLabel;
    @FXML private Label totalUsersLabel;

    private String currentAdminEmail;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        MongoDBConnector.connect();
        loadSystemOverview();
        generateCategoryReport(null);
        generateStatusReport(null);
    }

    public void setCurrentAdmin(String adminEmail) {
        this.currentAdminEmail = adminEmail;
    }

    private void loadSystemOverview() {
        try {
            Document stats = MongoDBConnector.getSystemStats();
            
            totalComplaintsLabel.setText(String.valueOf(stats.getLong("totalComplaints")));
            pendingComplaintsLabel.setText(String.valueOf(stats.getLong("pendingComplaints")));
            resolvedComplaintsLabel.setText(String.valueOf(stats.getLong("resolvedComplaints")));
            totalUsersLabel.setText(String.valueOf(stats.getLong("totalUsers")));
            
        } catch (Exception e) {
            showAlert("Error", "Could not load system overview: " + e.getMessage());
        }
    }

    @FXML
    private void generateCategoryReport(ActionEvent event) {
        try {
            categoryReportContainer.getChildren().clear();
            
            List<Document> categoryStats = MongoDBConnector.getComplaintsByCategory();
            
            if (categoryStats.isEmpty()) {
                Label noDataLabel = new Label("No complaint data available");
                noDataLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
                categoryReportContainer.getChildren().add(noDataLabel);
                return;
            }
            
            // Calculate total for percentage calculation
            long total = categoryStats.stream().mapToLong(doc -> doc.getLong("count")).sum();
            
            for (Document categoryData : categoryStats) {
                String category = categoryData.getString("category");
                long count = categoryData.getLong("count");
                double percentage = total > 0 ? (double) count / total : 0;
                
                HBox reportItem = createReportItem(category, count, percentage, getCategoryColor(category));
                categoryReportContainer.getChildren().add(reportItem);
            }
            
            if (event != null) {
                showAlert("Success", "Category report generated successfully!");
            }
            
        } catch (Exception e) {
            showAlert("Error", "Could not generate category report: " + e.getMessage());
        }
    }

    @FXML
    private void generateStatusReport(ActionEvent event) {
        try {
            statusReportContainer.getChildren().clear();
            
            List<Document> statusStats = MongoDBConnector.getComplaintsByStatusReport();
            
            if (statusStats.isEmpty()) {
                Label noDataLabel = new Label("No complaint data available");
                noDataLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
                statusReportContainer.getChildren().add(noDataLabel);
                return;
            }
            
            // Calculate total for percentage calculation
            long total = statusStats.stream().mapToLong(doc -> doc.getLong("count")).sum();
            
            for (Document statusData : statusStats) {
                String status = statusData.getString("status");
                long count = statusData.getLong("count");
                double percentage = total > 0 ? (double) count / total : 0;
                
                HBox reportItem = createReportItem(status, count, percentage, getStatusColor(status));
                statusReportContainer.getChildren().add(reportItem);
            }
            
            if (event != null) {
                showAlert("Success", "Status report generated successfully!");
            }
            
        } catch (Exception e) {
            showAlert("Error", "Could not generate status report: " + e.getMessage());
        }
    }

    private HBox createReportItem(String label, long count, double percentage, String color) {
        HBox reportItem = new HBox(15);
        reportItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        reportItem.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 15;");

        // Label
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");
        nameLabel.setPrefWidth(180);

        // Progress bar
        ProgressBar progressBar = new ProgressBar(percentage);
        progressBar.setPrefWidth(200);
        progressBar.setStyle("-fx-accent: " + color + ";");
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        // Count and percentage
        Label countLabel = new Label(count + " (" + String.format("%.1f", percentage * 100) + "%)");
        countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-font-weight: bold;");
        countLabel.setPrefWidth(100);

        reportItem.getChildren().addAll(nameLabel, progressBar, countLabel);

        return reportItem;
    }

    private String getCategoryColor(String category) {
        switch (category.toLowerCase()) {
            case "road & transportation":
                return "#ef4444";
            case "water supply":
                return "#3b82f6";
            case "electricity":
                return "#f59e0b";
            case "garbage collection":
                return "#10b981";
            case "street lighting":
                return "#8b5cf6";
            case "public safety":
                return "#dc2626";
            case "noise pollution":
                return "#f97316";
            case "building & construction":
                return "#6b7280";
            case "parks & recreation":
                return "#059669";
            case "healthcare":
                return "#e11d48";
            case "education":
                return "#7c3aed";
            default:
                return "#6b7280";
        }
    }

    private String getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "#f59e0b";
            case "in progress":
                return "#3b82f6";
            case "resolved":
                return "#10b981";
            case "rejected":
                return "#ef4444";
            default:
                return "#6b7280";
        }
    }

    @FXML
    private void refreshReports(ActionEvent event) {
        loadSystemOverview();
        generateCategoryReport(null);
        generateStatusReport(null);
        showAlert("Success", "All reports refreshed successfully!");
    }

    @FXML
    private void goBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
            Parent root = loader.load();
            
            AdminDashboardController controller = loader.getController();
            controller.setCurrentAdmin(currentAdminEmail);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setMaximized(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard - Citizen Complaint System");
            
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });
            
        } catch (Exception e) {
            showAlert("Error", "Could not return to dashboard: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
