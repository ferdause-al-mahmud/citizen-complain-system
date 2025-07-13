/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML2.java to edit this template
 */
package citizencomplainsystem;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * FXML Controller class for User Profile Management
 *
 * @author ferda
 */
public class UserProfileController implements Initializable {

    @FXML
    private Button backButton;
    
    @FXML
    private Label userEmailLabel;
    
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private PasswordField currentPasswordField;
    
    @FXML
    private PasswordField newPasswordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Button updateInfoButton;
    
    @FXML
    private Button resetInfoButton;
    
    @FXML
    private Button updatePasswordButton;
    
    @FXML
    private Button resetPasswordButton;
    
    @FXML
    private Button refreshDataButton;
    
    @FXML
    private Button deleteAccountButton;
    
    private String currentUserEmail;
    private Document currentUserData;
    
    // Email validation pattern
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    
    // Phone validation pattern (accepts various formats)
    private static final String PHONE_PATTERN = "^[+]?[0-9]{10,15}$";
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize any default values or listeners here
        setupFieldValidation();
    }
    
    /**
     * Sets up field validation and formatting
     */
    private void setupFieldValidation() {
        // Add input validation listeners if needed
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Remove any non-digit characters except +
            if (newValue != null && !newValue.matches("[+0-9]*")) {
                phoneField.setText(newValue.replaceAll("[^+0-9]", ""));
            }
        });
    }
    
    /**
     * Sets the current user and loads their data
     */
    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
        userEmailLabel.setText(email);
        loadUserData();
    }
    
    /**
     * Loads user data from MongoDB and populates the fields
     */
    private void loadUserData() {
        try {
            MongoDatabase db = MongoDBConnector.getDatabase();
            MongoCollection<Document> users = db.getCollection("users");
            
            Document query = new Document("email", currentUserEmail);
            currentUserData = users.find(query).first();
            
            if (currentUserData != null) {
                // Populate fields with current user data
                fullNameField.setText(currentUserData.getString("fullName") != null ? 
                    currentUserData.getString("fullName") : "");
                emailField.setText(currentUserData.getString("email") != null ? 
                    currentUserData.getString("email") : "");
                phoneField.setText(currentUserData.getString("phone") != null ? 
                    currentUserData.getString("phone") : "");
                
                System.out.println("User data loaded successfully for: " + currentUserEmail);
            } else {
                showErrorAlert("Error", "User data not found in database.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to load user data: " + e.getMessage());
        }
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
                                    stage.setMaximized(false);  // First set to false

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard - Citizen Complaint System");
              javafx.application.Platform.runLater(() -> {
                    stage.setMaximized(true);  // Then set to true
                });
            System.out.println("Returned to dashboard");
            
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not return to dashboard.");
        }
    }
    
    /**
     * Updates personal information
     */
    @FXML
    private void updatePersonalInfo(ActionEvent event) {
        try {
            // Validate input fields
            if (!validatePersonalInfo()) {
                return;
            }
            
            String newFullName = fullNameField.getText().trim();
            String newEmail = emailField.getText().trim().toLowerCase();
            String newPhone = phoneField.getText().trim();
            
            // Check if email is being changed and if it already exists
            if (!newEmail.equals(currentUserEmail) && emailExists(newEmail)) {
                showErrorAlert("Email Error", "This email address is already registered.");
                return;
            }
            
            // Update user data in MongoDB
            MongoDatabase db = MongoDBConnector.getDatabase();
            MongoCollection<Document> users = db.getCollection("users");
            
            Document query = new Document("email", currentUserEmail);
            Document update = new Document("$set", new Document()
                .append("fullName", newFullName)
                .append("email", newEmail)
                .append("phone", newPhone));
            
            UpdateResult result = users.updateOne(query, update);
            
            if (result.getModifiedCount() > 0) {
                // Update current user email if it was changed
                if (!newEmail.equals(currentUserEmail)) {
                    currentUserEmail = newEmail;
                    userEmailLabel.setText(newEmail);
                }
                
                showSuccessAlert("Success", "Personal information updated successfully!");
                loadUserData(); // Refresh the data
            } else {
                showErrorAlert("Update Error", "Failed to update personal information.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to update personal information: " + e.getMessage());
        }
    }
    
    /**
     * Validates personal information fields
     */
    private boolean validatePersonalInfo() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        
        // Validate full name
        if (fullName.isEmpty() || fullName.length() < 2) {
            showErrorAlert("Validation Error", "Full name must be at least 2 characters long.");
            fullNameField.requestFocus();
            return false;
        }
        
        // Validate email
        if (email.isEmpty() || !Pattern.matches(EMAIL_PATTERN, email)) {
            showErrorAlert("Validation Error", "Please enter a valid email address.");
            emailField.requestFocus();
            return false;
        }
        
        // Validate phone (optional but if provided, must be valid)
        if (!phone.isEmpty() && !Pattern.matches(PHONE_PATTERN, phone)) {
            showErrorAlert("Validation Error", "Please enter a valid phone number (10-15 digits).");
            phoneField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if email already exists in database
     */
    private boolean emailExists(String email) {
        try {
            MongoDatabase db = MongoDBConnector.getDatabase();
            MongoCollection<Document> users = db.getCollection("users");
            
            Document query = new Document("email", email);
            return users.find(query).first() != null;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Resets personal information fields to original values
     */
    @FXML
    private void resetPersonalInfo(ActionEvent event) {
        loadUserData(); // Reload original data
        showInfoAlert("Reset", "Personal information fields have been reset to original values.");
    }
    
    /**
     * Updates user password
     */
    @FXML
    private void updatePassword(ActionEvent event) {
        try {
            // Validate password fields
            if (!validatePasswordFields()) {
                return;
            }
            
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            
            // Verify current password
            if (!verifyCurrentPassword(currentPassword)) {
                showErrorAlert("Password Error", "Current password is incorrect.");
                currentPasswordField.requestFocus();
                return;
            }
            
            // Update password in MongoDB
            MongoDatabase db = MongoDBConnector.getDatabase();
            MongoCollection<Document> users = db.getCollection("users");
            
            Document query = new Document("email", currentUserEmail);
            Document update = new Document("$set", new Document("password", newPassword));
            
            UpdateResult result = users.updateOne(query, update);
            
            if (result.getModifiedCount() > 0) {
                showSuccessAlert("Success", "Password updated successfully!");
                resetPasswordFields(null); // Clear password fields
            } else {
                showErrorAlert("Update Error", "Failed to update password.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to update password: " + e.getMessage());
        }
    }
    
    /**
     * Validates password fields
     */
    private boolean validatePasswordFields() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Check if all password fields are filled
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showErrorAlert("Validation Error", "All password fields must be filled.");
            return false;
        }
        
        // Validate new password strength
        if (!isValidPassword(newPassword)) {
            showErrorAlert("Password Error", 
                "New password must be at least 8 characters long and contain:\n" +
                "• At least one uppercase letter\n" +
                "• At least one lowercase letter\n" +
                "• At least one number");
            newPasswordField.requestFocus();
            return false;
        }
        
        // Check if new passwords match
        if (!newPassword.equals(confirmPassword)) {
            showErrorAlert("Password Error", "New password and confirmation do not match.");
            confirmPasswordField.requestFocus();
            return false;
        }
        
        // Check if new password is different from current
        if (newPassword.equals(currentPassword)) {
            showErrorAlert("Password Error", "New password must be different from current password.");
            newPasswordField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates password strength
     */
    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&  // At least one uppercase
               password.matches(".*[a-z].*") &&  // At least one lowercase
               password.matches(".*[0-9].*");    // At least one number
    }
    
    /**
     * Verifies current password
     */
    private boolean verifyCurrentPassword(String currentPassword) {
        return currentUserData != null && 
               currentPassword.equals(currentUserData.getString("password"));
    }
    
    /**
     * Resets password fields
     */
    @FXML
    private void resetPasswordFields(ActionEvent event) {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        
        if (event != null) { // Only show message if called by button click
            showInfoAlert("Reset", "Password fields have been cleared.");
        }
    }
    
    /**
     * Refreshes user data from database
     */
    @FXML
    private void refreshUserData(ActionEvent event) {
        loadUserData();
        showSuccessAlert("Refresh", "User data has been refreshed from database.");
    }
    
    /**
     * Handles account deletion
     */
    @FXML
    private void deleteAccount(ActionEvent event) {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Account");
        confirmAlert.setHeaderText("Are you sure you want to delete your account?");
        confirmAlert.setContentText("This action cannot be undone. All your data will be permanently deleted.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            
            // Second confirmation for critical action
            Alert finalConfirmAlert = new Alert(Alert.AlertType.WARNING);
            finalConfirmAlert.setTitle("Final Confirmation");
            finalConfirmAlert.setHeaderText("FINAL WARNING");
            finalConfirmAlert.setContentText("Type 'DELETE' to confirm account deletion:");
            
            // Create a custom dialog with text input
            javafx.scene.control.TextInputDialog deleteDialog = 
                new javafx.scene.control.TextInputDialog();
            deleteDialog.setTitle("Delete Account");
            deleteDialog.setHeaderText("FINAL WARNING - This action cannot be undone!");
            deleteDialog.setContentText("Type 'DELETE' to confirm:");
            
            Optional<String> deleteResult = deleteDialog.showAndWait();
            if (deleteResult.isPresent() && "DELETE".equals(deleteResult.get())) {
                performAccountDeletion();
            } else {
                showInfoAlert("Cancelled", "Account deletion cancelled.");
            }
        }
    }
    
    /**
     * Performs the actual account deletion
     */
    private void performAccountDeletion() {
        try {
            MongoDatabase db = MongoDBConnector.getDatabase();
            MongoCollection<Document> users = db.getCollection("users");
            
            Document query = new Document("email", currentUserEmail);
            long deletedCount = users.deleteOne(query).getDeletedCount();
            
            if (deletedCount > 0) {
                showSuccessAlert("Account Deleted", "Your account has been permanently deleted.");
                
                // Return to login page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) deleteAccountButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Login - Citizen Complaint System");
                
            } else {
                showErrorAlert("Deletion Error", "Failed to delete account. Please try again.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to delete account: " + e.getMessage());
        }
    }
    
    /**
     * Shows success alert
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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