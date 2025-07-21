/*
 * View All Complaints Controller with Updated UI and Removed Track Button
 */
package citizencomplainsystem;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * FXML Controller class for View All Complaints
 *
 * @author ferda
 */
public class ViewAllComplaintsController implements Initializable {

    @FXML
    private Button backButton;
    @FXML
    private Label totalComplaintsLabel;
    @FXML
    private ComboBox<String> statusFilterComboBox;
    @FXML
    private ComboBox<String> categoryFilterComboBox;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button refreshButton;
    @FXML
    private Label pendingCountLabel;
    @FXML
    private Label inProgressCountLabel;
    @FXML
    private Label resolvedCountLabel;
    @FXML
    private Label rejectedCountLabel;
    @FXML
    private VBox complaintsContainer;
    @FXML
    private VBox noComplaintsContainer;

    private String currentUserEmail;
    private List<Document> allComplaints;
    private List<Document> filteredComplaints;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize MongoDB connection
        MongoDBConnector.connect();

        // Setup filter combo boxes
        setupFilterComboBoxes();

        // Load complaints data
        loadAllComplaints();
    }

    /**
     * Sets the current user email
     */
    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        loadAllComplaints();
    }

    /**
     * Sets up the filter combo boxes with options
     */
    private void setupFilterComboBoxes() {
        // Status filter options
        statusFilterComboBox.getItems().addAll(
                "All Status", "Pending", "In Progress", "Resolved", "Rejected"
        );
        statusFilterComboBox.setValue("All Status");

        // Category filter options
        categoryFilterComboBox.getItems().addAll(
                "All Categories",
                "Road & Transportation",
                "Water Supply",
                "Electricity",
                "Garbage Collection",
                "Street Lighting",
                "Public Safety",
                "Noise Pollution",
                "Building & Construction",
                "Parks & Recreation",
                "Healthcare",
                "Education",
                "Other"
        );
        categoryFilterComboBox.setValue("All Categories");
    }

    /**
     * Loads all complaints for the current user
     */
    private void loadAllComplaints() {
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            return;
        }

        try {
            // Get all complaints from database
            allComplaints = MongoDBConnector.getUserComplaints(currentUserEmail);
            filteredComplaints = allComplaints;

            // Update statistics
            updateStatistics();

            // Display complaints
            displayComplaints(filteredComplaints);

            System.out.println("Loaded " + allComplaints.size() + " complaints for user: " + currentUserEmail);

        } catch (Exception e) {
            System.err.println("Error loading complaints: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Data Loading Error", "Could not load complaints: " + e.getMessage());
        }
    }

    /**
     * Updates the statistics labels
     */
    private void updateStatistics() {
        if (allComplaints == null) {
            return;
        }

        long total = allComplaints.size();
        long pending = allComplaints.stream().filter(c -> "Pending".equals(c.getString("status"))).count();
        long inProgress = allComplaints.stream().filter(c -> "In Progress".equals(c.getString("status"))).count();
        long resolved = allComplaints.stream().filter(c -> "Resolved".equals(c.getString("status"))).count();
        long rejected = allComplaints.stream().filter(c -> "Rejected".equals(c.getString("status"))).count();

        totalComplaintsLabel.setText("Total: " + total);
        pendingCountLabel.setText(String.valueOf(pending));
        inProgressCountLabel.setText(String.valueOf(inProgress));
        resolvedCountLabel.setText(String.valueOf(resolved));
        rejectedCountLabel.setText(String.valueOf(rejected));
    }

    /**
     * Displays the complaints in the UI
     */
    private void displayComplaints(List<Document> complaints) {
        complaintsContainer.getChildren().clear();

        if (complaints == null || complaints.isEmpty()) {
            noComplaintsContainer.setVisible(true);
            complaintsContainer.setVisible(false);
            return;
        }

        noComplaintsContainer.setVisible(false);
        complaintsContainer.setVisible(true);

        for (Document complaint : complaints) {
            VBox complaintCard = createDetailedComplaintCard(complaint);
            complaintsContainer.getChildren().add(complaintCard);
        }
    }

    /**
     * Creates a detailed complaint card with updated styling
     */
    private VBox createDetailedComplaintCard(Document complaint) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-border-color: #8B5CF6; "
                + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-border-width: 1; "
                + "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.2), 8, 0, 0, 4);");
        card.setPadding(new Insets(25));

        // Header with title and status
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLabel = new Label(complaint.getString("title"));
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #333333;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(complaint.getString("status"));
        statusLabel.setStyle(getStatusStyle(complaint.getString("status")));

        header.getChildren().addAll(titleLabel, spacer, statusLabel);

        // Complaint ID and Date
        HBox idDateBox = new HBox(25);
        Label idLabel = new Label("ID: " + complaint.getString("complaintId"));
        idLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #8B5CF6; -fx-font-weight: bold;");

        Label dateLabel = new Label("Submitted: " + complaint.getString("submittedDate"));
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        idDateBox.getChildren().addAll(idLabel, dateLabel);

        // Category, Priority, and Location
        HBox detailsBox = new HBox(25);
        Label categoryLabel = new Label("📂 " + complaint.getString("category"));
        categoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057;");

        Label priorityLabel = new Label("⚡ " + complaint.getString("priority"));
        priorityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: "
                + getPriorityColor(complaint.getString("priority")) + "; -fx-font-weight: bold;");

        Label locationLabel = new Label("📍 " + complaint.getString("location"));
        locationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057;");

        detailsBox.getChildren().addAll(categoryLabel, priorityLabel, locationLabel);

        // Description
        Label descriptionLabel = new Label("Description:");
        descriptionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #495057;");

        Label descriptionText = new Label(complaint.getString("description"));
        descriptionText.setWrapText(true);
        descriptionText.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 8 0 0 0;");

        // Admin Comments (if any)
        VBox adminSection = new VBox(8);
        String adminComments = complaint.getString("adminComments");
        if (adminComments != null && !adminComments.trim().isEmpty()) {
            Label adminLabel = new Label("Admin Response:");
            adminLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #8B5CF6;");

            Label adminText = new Label(adminComments);
            adminText.setWrapText(true);
            adminText.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-background-color: #f8f9fa; "
                    + "-fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #8B5CF6; -fx-border-radius: 8;");

            adminSection.getChildren().addAll(adminLabel, adminText);
        }

        // Action button (only View Details now)
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button viewDetailsButton = new Button("View Full Details");
        viewDetailsButton.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-background-radius: 8; "
                + "-fx-padding: 10 20; -fx-font-size: 14px; -fx-font-weight: bold;");
        viewDetailsButton.setOnAction(e -> viewComplaintDetails(complaint));

        actionBox.getChildren().add(viewDetailsButton);

        // Add all components to card
        card.getChildren().addAll(header, idDateBox, detailsBox, descriptionLabel, descriptionText);

        if (!adminSection.getChildren().isEmpty()) {
            card.getChildren().add(adminSection);
        }

        card.getChildren().add(actionBox);

        return card;
    }

    /**
     * Gets appropriate style for complaint status
     */
    private String getStatusStyle(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "-fx-background-color: #fef3c7; -fx-text-fill: #f59e0b; "
                        + "-fx-padding: 8 15 8 15; -fx-background-radius: 20; -fx-font-size: 13px; -fx-font-weight: bold;";
            case "in progress":
                return "-fx-background-color: #dbeafe; -fx-text-fill: #3b82f6; "
                        + "-fx-padding: 8 15 8 15; -fx-background-radius: 20; -fx-font-size: 13px; -fx-font-weight: bold;";
            case "resolved":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #10b981; "
                        + "-fx-padding: 8 15 8 15; -fx-background-radius: 20; -fx-font-size: 13px; -fx-font-weight: bold;";
            case "rejected":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; "
                        + "-fx-padding: 8 15 8 15; -fx-background-radius: 20; -fx-font-size: 13px; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; "
                        + "-fx-padding: 8 15 8 15; -fx-background-radius: 20; -fx-font-size: 13px; -fx-font-weight: bold;";
        }
    }

    /**
     * Gets appropriate color for priority
     */
    private String getPriorityColor(String priority) {
        switch (priority.toLowerCase()) {
            case "high":
                return "#ef4444";
            case "medium":
                return "#f59e0b";
            case "low":
                return "#10b981";
            default:
                return "#6b7280";
        }
    }

    /**
     * Filters complaints by status
     */
    @FXML
    private void filterByStatus(ActionEvent event) {
        applyFilters();
    }

    /**
     * Filters complaints by category
     */
    @FXML
    private void filterByCategory(ActionEvent event) {
        applyFilters();
    }

    /**
     * Searches complaints based on text input
     */
    @FXML
    private void searchComplaints(KeyEvent event) {
        applyFilters();
    }

    /**
     * Applies all active filters
     */
    private void applyFilters() {
        if (allComplaints == null) {
            return;
        }

        filteredComplaints = allComplaints.stream()
                .filter(this::matchesStatusFilter)
                .filter(this::matchesCategoryFilter)
                .filter(this::matchesSearchFilter)
                .collect(Collectors.toList());

        displayComplaints(filteredComplaints);
    }

    /**
     * Checks if complaint matches status filter
     */
    private boolean matchesStatusFilter(Document complaint) {
        String selectedStatus = statusFilterComboBox.getValue();
        return "All Status".equals(selectedStatus)
                || selectedStatus.equals(complaint.getString("status"));
    }

    /**
     * Checks if complaint matches category filter
     */
    private boolean matchesCategoryFilter(Document complaint) {
        String selectedCategory = categoryFilterComboBox.getValue();
        return "All Categories".equals(selectedCategory)
                || selectedCategory.equals(complaint.getString("category"));
    }

    /**
     * Checks if complaint matches search filter
     */
    private boolean matchesSearchFilter(Document complaint) {
        String searchText = searchTextField.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            return true;
        }

        searchText = searchText.toLowerCase();
        return complaint.getString("title").toLowerCase().contains(searchText)
                || complaint.getString("description").toLowerCase().contains(searchText)
                || complaint.getString("complaintId").toLowerCase().contains(searchText)
                || complaint.getString("location").toLowerCase().contains(searchText);
    }

    /**
     * Refreshes the complaints list
     */
    @FXML
    private void refreshComplaints(ActionEvent event) {
        loadAllComplaints();
        showInfoAlert("Refresh Complete", "Complaints list has been refreshed successfully!");
    }

    /**
     * Views detailed information about a complaint
     */
    private void viewComplaintDetails(Document complaint) {
        StringBuilder details = new StringBuilder();
        details.append("Complaint ID: ").append(complaint.getString("complaintId")).append("\n\n");
        details.append("Title: ").append(complaint.getString("title")).append("\n\n");
        details.append("Category: ").append(complaint.getString("category")).append("\n");
        details.append("Priority: ").append(complaint.getString("priority")).append("\n");
        details.append("Status: ").append(complaint.getString("status")).append("\n");
        details.append("Location: ").append(complaint.getString("location")).append("\n\n");
        details.append("Description:\n").append(complaint.getString("description")).append("\n\n");
        details.append("Contact Phone: ").append(complaint.getString("contactPhone")).append("\n");
        details.append("Submitted Date: ").append(complaint.getString("submittedDate")).append("\n");
        details.append("Last Updated: ").append(complaint.getString("lastUpdated")).append("\n");

        String adminComments = complaint.getString("adminComments");
        if (adminComments != null && !adminComments.trim().isEmpty()) {
            details.append("\nAdmin Comments:\n").append(adminComments);
        }

        String resolvedDate = complaint.getString("resolvedDate");
        if (resolvedDate != null && !resolvedDate.trim().isEmpty()) {
            details.append("\nResolved Date: ").append(resolvedDate);
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Complaint Details");
        alert.setHeaderText("Complete Complaint Information");
        alert.setContentText(details.toString());
        alert.getDialogPane().setPrefWidth(700);
        alert.getDialogPane().setPrefHeight(500);
        alert.showAndWait();
    }

    /**
     * Goes back to the dashboard
     */
    @FXML
    private void goBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserDashboard.fxml"));
            Parent root = loader.load();

            UserDashboardController controller = loader.getController();
            controller.setCurrentUser(currentUserEmail);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setMaximized(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("User Dashboard - Citizen Complaint System");

            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not return to dashboard: " + e.getMessage());
        }
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
