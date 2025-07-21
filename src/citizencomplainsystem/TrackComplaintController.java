/*
 * Track Complaint Controller with Full Functionality
 */
package citizencomplainsystem;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * FXML Controller class for Track Complaint
 * 
 * @author ferda
 */
public class TrackComplaintController implements Initializable {

    @FXML private Button backButton;
    @FXML private TextField complaintIdTextField;
    @FXML private Button trackButton;
    @FXML private Button clearButton;
    @FXML private VBox complaintDetailsContainer;
    @FXML private VBox initialMessageContainer;
    @FXML private VBox notFoundContainer;
    @FXML private Label notFoundMessageLabel;
    @FXML private VBox complaintCard;
    @FXML private Label complaintTitleLabel;
    @FXML private Label complaintStatusLabel;
    @FXML private Label complaintIdLabel;
    @FXML private VBox timelineContainer;
    @FXML private Label categoryLabel;
    @FXML private Label priorityLabel;
    @FXML private Label locationLabel;
    @FXML private Label contactPhoneLabel;
    @FXML private Label descriptionLabel;
    @FXML private VBox adminResponseContainer;
    @FXML private Label adminResponseLabel;
    @FXML private Button refreshTrackingButton;

    private String currentUserEmail;
    private Document currentComplaint;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize MongoDB connection
        MongoDBConnector.connect();
        
        // Setup initial state
        showInitialMessage();
        
        // Add enter key listener to complaint ID field
        complaintIdTextField.setOnAction(e -> trackComplaint(null));
    }

    /**
     * Sets the current user email
     */
    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
    }

    /**
     * Sets a specific complaint ID to track (when coming from other pages)
     */
    public void setComplaintIdToTrack(String complaintId) {
        if (complaintId != null && !complaintId.trim().isEmpty()) {
            complaintIdTextField.setText(complaintId);
            trackComplaint(null);
        }
    }

    /**
     * Shows the initial message
     */
    private void showInitialMessage() {
        initialMessageContainer.setVisible(true);
        notFoundContainer.setVisible(false);
        complaintCard.setVisible(false);
    }

    /**
     * Shows the not found message
     */
    private void showNotFoundMessage(String message) {
        initialMessageContainer.setVisible(false);
        notFoundContainer.setVisible(true);
        complaintCard.setVisible(false);
        notFoundMessageLabel.setText(message);
    }

    /**
     * Shows the complaint details
     */
    private void showComplaintDetails() {
        initialMessageContainer.setVisible(false);
        notFoundContainer.setVisible(false);
        complaintCard.setVisible(true);
    }

    /**
     * Tracks a complaint by ID
     */
    @FXML
    private void trackComplaint(ActionEvent event) {
        String complaintId = complaintIdTextField.getText();
        
        if (complaintId == null || complaintId.trim().isEmpty()) {
            showErrorAlert("Input Error", "Please enter a complaint ID to track.");
            return;
        }

        complaintId = complaintId.trim();

        try {
            // Get complaint from database
            Document complaint = MongoDBConnector.getComplaintById(complaintId);
            
            if (complaint == null) {
                showNotFoundMessage("Complaint with ID '" + complaintId + "' was not found in the system.");
                return;
            }

            // Check if complaint belongs to current user
            String complaintUserEmail = complaint.getString("userEmail");
            if (!currentUserEmail.equals(complaintUserEmail)) {
                showNotFoundMessage("Complaint with ID '" + complaintId + "' was not found in your account.\n" +
                                  "You can only track complaints that you have submitted.");
                return;
            }

            // Store current complaint and display details
            currentComplaint = complaint;
            displayComplaintDetails(complaint);
            showComplaintDetails();

        } catch (Exception e) {
            System.err.println("Error tracking complaint: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Tracking Error", "Could not track complaint: " + e.getMessage());
        }
    }

    /**
     * Displays the complaint details
     */
    private void displayComplaintDetails(Document complaint) {
        // Basic information
        complaintTitleLabel.setText(complaint.getString("title"));
        complaintIdLabel.setText("Complaint ID: " + complaint.getString("complaintId"));
        
        // Status with styling
        String status = complaint.getString("status");
        complaintStatusLabel.setText(status);
        complaintStatusLabel.setStyle(getStatusStyle(status));
        
        // Complaint information
        categoryLabel.setText(complaint.getString("category"));
        priorityLabel.setText(complaint.getString("priority"));
        priorityLabel.setStyle("-fx-text-fill: " + getPriorityColor(complaint.getString("priority")) + "; -fx-font-weight: bold;");
        locationLabel.setText(complaint.getString("location"));
        contactPhoneLabel.setText(complaint.getString("contactPhone"));
        descriptionLabel.setText(complaint.getString("description"));
        
        // Admin response
        String adminComments = complaint.getString("adminComments");
        if (adminComments != null && !adminComments.trim().isEmpty()) {
            adminResponseContainer.setVisible(true);
            adminResponseLabel.setText(adminComments);
        } else {
            adminResponseContainer.setVisible(false);
        }
        
        // Create timeline
        createProgressTimeline(complaint);
    }

    /**
     * Creates the progress timeline
     */
    private void createProgressTimeline(Document complaint) {
        // Clear existing timeline items (keep the title)
        timelineContainer.getChildren().removeIf(node -> !(node instanceof Label) || 
                                                !((Label) node).getText().equals("Progress Timeline"));

        String status = complaint.getString("status");
        String submittedDate = complaint.getString("submittedDate");
        String lastUpdated = complaint.getString("lastUpdated");
        String resolvedDate = complaint.getString("resolvedDate");

        // Timeline item 1: Submitted
        VBox submittedItem = createTimelineItem("✅", "Complaint Submitted", 
                                              "Your complaint has been successfully submitted to our system.", 
                                              submittedDate, true);
        timelineContainer.getChildren().add(submittedItem);

        // Timeline item 2: Under Review
        boolean underReview = !status.equals("Pending");
        VBox reviewItem = createTimelineItem(underReview ? "✅" : "⏳", "Under Review", 
                                           underReview ? "Your complaint is being reviewed by our team." : 
                                                       "Your complaint is waiting to be reviewed.", 
                                           underReview ? lastUpdated : "", underReview);
        timelineContainer.getChildren().add(reviewItem);

        // Timeline item 3: In Progress
        boolean inProgress = status.equals("In Progress") || status.equals("Resolved");
        VBox progressItem = createTimelineItem(inProgress ? "✅" : "⏳", "In Progress", 
                                             inProgress ? "Our team is actively working on your complaint." : 
                                                        "Your complaint will be processed soon.", 
                                             inProgress ? lastUpdated : "", inProgress);
        timelineContainer.getChildren().add(progressItem);

        // Timeline item 4: Resolution
        boolean isResolved = status.equals("Resolved");
        boolean isRejected = status.equals("Rejected");
        String resolutionIcon = isResolved ? "✅" : (isRejected ? "❌" : "⏳");
        String resolutionTitle = isResolved ? "Resolved" : (isRejected ? "Rejected" : "Resolution Pending");
        String resolutionDesc = isResolved ? "Your complaint has been successfully resolved." : 
                               (isRejected ? "Your complaint has been reviewed and rejected." : 
                                           "Your complaint resolution is pending.");
        String resolutionDate = isResolved ? resolvedDate : (isRejected ? lastUpdated : "");
        
        VBox resolutionItem = createTimelineItem(resolutionIcon, resolutionTitle, resolutionDesc, 
                                               resolutionDate, isResolved || isRejected);
        timelineContainer.getChildren().add(resolutionItem);
    }

    /**
     * Creates a timeline item
     */
    private VBox createTimelineItem(String icon, String title, String description, String date, boolean completed) {
        VBox item = new VBox(5);
        item.setStyle("-fx-padding: 15; -fx-background-color: " + (completed ? "#f8f9fa" : "#ffffff") + 
                     "; -fx-border-color: " + (completed ? "#28a745" : "#dee2e6") + 
                     "; -fx-border-width: 0 0 0 4; -fx-background-radius: 0 5 5 0;");

        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + 
                           (completed ? "#28a745" : "#6c757d") + ";");

        header.getChildren().addAll(iconLabel, titleLabel);

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #495057;");
        descLabel.setWrapText(true);

        item.getChildren().addAll(header, descLabel);

        if (date != null && !date.trim().isEmpty()) {
            Label dateLabel = new Label("Date: " + formatDate(date));
            dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-font-style: italic;");
            item.getChildren().add(dateLabel);
        }

        return item;
    }

    /**
     * Formats date string for display
     */
    private String formatDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "Not available";
        }

        try {
            // Try to parse as LocalDateTime
            LocalDateTime dateTime = LocalDateTime.parse(dateString);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
            return dateTime.format(formatter);
        } catch (DateTimeParseException e) {
            // If parsing fails, return original string
            return dateString;
        }
    }

    /**
     * Gets appropriate style for complaint status
     */
    private String getStatusStyle(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "-fx-background-color: #fff3cd; -fx-text-fill: #856404; " +
                       "-fx-padding: 8 15 8 15; -fx-background-radius: 20; -fx-font-weight: bold;";
            case "in progress":
                return "-fx-background-color: #cce5ff; -fx-text-fill: #004085; " +
                       "-fx-padding: 8 15 8 15; -fx-background-radius: 20; -fx-font-weight: bold;";
            case "resolved":
                return "-fx-background-color: #d4edda; -fx-text-fill: #155724; " +
                       "-fx-padding: 8 15 8 15; -fx-background-radius: 20; -fx-font-weight: bold;";
            case "rejected":
                return "-fx-background-color: #f8d7da; -fx-text-fill: #721c24; " +
                       "-fx-padding: 8 15 8 15; -fx-background-radius: 20; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #e2e3e5; -fx-text-fill: #383d41; " +
                       "-fx-padding: 8 15 8 15; -fx-background-radius: 20; -fx-font-weight: bold;";
        }
    }

    /**
     * Gets appropriate color for priority
     */
    private String getPriorityColor(String priority) {
        switch (priority.toLowerCase()) {
            case "high":
                return "#dc3545";
            case "medium":
                return "#fd7e14";
            case "low":
                return "#28a745";
            default:
                return "#6c757d";
        }
    }

    /**
     * Clears the search and resets the view
     */
    @FXML
    private void clearSearch(ActionEvent event) {
        complaintIdTextField.clear();
        currentComplaint = null;
        showInitialMessage();
    }

    /**
     * Refreshes the tracking information
     */
    @FXML
    private void refreshTracking(ActionEvent event) {
        if (currentComplaint != null) {
            String complaintId = currentComplaint.getString("complaintId");
            complaintIdTextField.setText(complaintId);
            trackComplaint(null);
            showInfoAlert("Refresh Complete", "Complaint tracking information has been updated!");
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
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setMaximized(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("View All Complaints - Citizen Complaint System");
            
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not open View All Complaints page: " + e.getMessage());
        }
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
