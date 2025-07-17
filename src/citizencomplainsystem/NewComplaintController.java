package citizencomplainsystem;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * FXML Controller class for New Complaint submission
 * 
 * @author ferda
 */
public class NewComplaintController implements Initializable {

    @FXML
    private Button backButton;
    
    @FXML
    private Label userEmailLabel;
    
    @FXML
    private TextField titleField;
    
    @FXML
    private ComboBox<String> categoryComboBox;
    
    @FXML
    private ComboBox<String> priorityComboBox;
    
    @FXML
    private TextField locationField;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private TextField contactPhoneField;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private Button submitButton;
    
    @FXML
    private VBox statusContainer;
    
    @FXML
    private Label statusLabel;
    
    private String currentUserEmail;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBoxes();
        setupFieldValidation();
    }
    
    /**
     * Sets up the combo boxes with predefined values
     */
    private void setupComboBoxes() {
        // Category options
        categoryComboBox.getItems().addAll(
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
        
        // Priority options
        priorityComboBox.getItems().addAll(
            "Low",
            "Medium", 
            "High",
            "Critical"
        );
    }
    
    /**
     * Sets up field validation
     */
    private void setupFieldValidation() {
        // Limit title field to reasonable length
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 100) {
                titleField.setText(oldValue);
            }
        });
        
        // Phone number validation
        contactPhoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("[+0-9]*")) {
                contactPhoneField.setText(newValue.replaceAll("[^+0-9]", ""));
            }
        });
        
        // Description character limit
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 1000) {
                descriptionArea.setText(oldValue);
            }
        });
    }
    
    /**
     * Sets the current user email
     */
    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        userEmailLabel.setText(email);
    }
    
    /**
     * Handles back button action
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Load the dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserDashboard.fxml"));
            Parent root = loader.load();
            
            // Set the current user in the dashboard controller
            UserDashboardController dashboardController = loader.getController();
            dashboardController.setCurrentUser(currentUserEmail);
            
            // Get the current stage and set the dashboard scene
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setMaximized(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard - Citizen Complaint System");
            
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
            });
            
            System.out.println("Returned to dashboard");
            
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not return to dashboard.");
        }
    }
    
    /**
     * Clears all form fields
     */
    @FXML
    private void clearForm(ActionEvent event) {
        titleField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        priorityComboBox.getSelectionModel().clearSelection();
        locationField.clear();
        descriptionArea.clear();
        contactPhoneField.clear();
        hideStatusMessage();
        
        showInfoAlert("Form Cleared", "All form fields have been cleared.");
    }
    
    /**
     * Submits the complaint to the database
     */
    // Update the submitComplaint method in NewComplaintController.java

@FXML
private void submitComplaint(ActionEvent event) {
    try {
        // Validate form
        if (!validateForm()) {
            return;
        }
        
        // Disable submit button to prevent double submission
        submitButton.setDisable(true);
        submitButton.setText("Submitting...");
        
        // Generate complaint ID and timestamp
        String complaintId = generateComplaintId();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // Extract form data
        String title = titleField.getText().trim();
        String category = categoryComboBox.getValue();
        String priority = priorityComboBox.getValue();
        String location = locationField.getText().trim();
        String description = descriptionArea.getText().trim();
        String contactPhone = contactPhoneField.getText().trim();
        
        // Insert complaint using MongoDBConnector
        boolean success = MongoDBConnector.insertComplaint(
            complaintId,
            currentUserEmail,
            title,
            category,
            priority,
            location,
            description,
            contactPhone,
            timestamp
        );
        
        if (success) {
            // Show success message
            showStatusMessage("Success! Your complaint has been submitted successfully.\nComplaint ID: " + complaintId, "success");
            
            // Clear form after successful submission
            clearFormFields();
            
            System.out.println("Complaint submitted successfully with ID: " + complaintId);
        } else {
            showStatusMessage("Error: Failed to submit complaint. Please try again.", "error");
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        showStatusMessage("Error: Failed to submit complaint. Please try again.", "error");
    } finally {
        // Re-enable submit button
        submitButton.setDisable(false);
        submitButton.setText("Submit Complaint");
    }
}
    /**
     * Validates the form fields
     */
    private boolean validateForm() {
        // Check required fields
        if (titleField.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Please enter a complaint title.");
            titleField.requestFocus();
            return false;
        }
        
        if (categoryComboBox.getValue() == null) {
            showErrorAlert("Validation Error", "Please select a category.");
            categoryComboBox.requestFocus();
            return false;
        }
        
        if (priorityComboBox.getValue() == null) {
            showErrorAlert("Validation Error", "Please select a priority level.");
            priorityComboBox.requestFocus();
            return false;
        }
        
        if (locationField.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Please enter the location.");
            locationField.requestFocus();
            return false;
        }
        
        if (descriptionArea.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Please provide a detailed description.");
            descriptionArea.requestFocus();
            return false;
        }
        
        // Validate minimum description length
        if (descriptionArea.getText().trim().length() < 20) {
            showErrorAlert("Validation Error", "Description must be at least 20 characters long.");
            descriptionArea.requestFocus();
            return false;
        }
        
        // Validate phone number if provided
        String phone = contactPhoneField.getText().trim();
        if (!phone.isEmpty() && !phone.matches("^[+]?[0-9]{10,15}$")) {
            showErrorAlert("Validation Error", "Please enter a valid phone number (10-15 digits).");
            contactPhoneField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Generates a unique complaint ID
     */
    private String generateComplaintId() {
        String prefix = "CMP";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return prefix + timestamp + randomSuffix;
    }
    
    /**
     * Clears all form fields without showing alert
     */
    private void clearFormFields() {
        titleField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        priorityComboBox.getSelectionModel().clearSelection();
        locationField.clear();
        descriptionArea.clear();
        contactPhoneField.clear();
    }
    
    /**
     * Shows status message
     */
    private void showStatusMessage(String message, String type) {
        statusLabel.setText(message);
        statusContainer.setVisible(true);
        
        if ("success".equals(type)) {
            statusContainer.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 8; -fx-padding: 15;");
            statusLabel.setTextFill(javafx.scene.paint.Color.web("#22c55e"));
        } else if ("error".equals(type)) {
            statusContainer.setStyle("-fx-background-color: #fef2f2; -fx-background-radius: 8; -fx-padding: 15;");
            statusLabel.setTextFill(javafx.scene.paint.Color.web("#ef4444"));
        }
    }
    
    /**
     * Hides status message
     */
    private void hideStatusMessage() {
        statusContainer.setVisible(false);
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
}