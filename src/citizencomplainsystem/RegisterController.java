/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
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
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author ferda
 */
public class RegisterController implements Initializable {

    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Hyperlink loginLink;
    
    @FXML
    private ImageView registerImageView;
    
    @FXML
    private Button registerButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load image programmatically if FXML path doesn't work
        try {
            Image registerImage = new Image(getClass().getResourceAsStream("/images/loginImage.jpg"));
            if (registerImageView != null && registerImage != null) {
                registerImageView.setImage(registerImage);
            }
        } catch (Exception e) {
            System.out.println("Could not load register image: " + e.getMessage());
            // Try alternative path
            try {
                Image altImage = new Image(getClass().getResourceAsStream("/images/registerImage.jpg"));
                if (registerImageView != null && altImage != null) {
                    registerImageView.setImage(altImage);
                }
            } catch (Exception ex) {
                System.out.println("Could not load alternative register image: " + ex.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRegister(ActionEvent event) {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        
        // Validate all fields are filled
        if (fullName.isEmpty() || email.isEmpty() || 
            phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Registration Error", "Please fill in all required fields to register as a citizen.", Alert.AlertType.ERROR);
            return;
        }
        
        // Validate full name (should contain at least first and last name)
        if (!isValidFullName(fullName)) {
            showAlert("Validation Error", "Please enter your full name with at least first and last name (minimum 2 words).", Alert.AlertType.ERROR);
            return;
        }
        
        // Validate email format
        if (!isValidEmail(email)) {
            showAlert("Validation Error", "Please enter a valid email address (e.g., citizen@example.com).", Alert.AlertType.ERROR);
            return;
        }
        
        // Validate Bangladeshi phone number
        if (!isValidBangladeshiPhone(phone)) {
            showAlert("Validation Error", "Please enter a valid Bangladeshi mobile number starting with 01 (e.g., 01712345678).", Alert.AlertType.ERROR);
            return;
        }
        
        // Validate password strength
        if (!isValidPassword(password)) {
            showAlert("Validation Error", "Password must be at least 6 characters long and contain letters and numbers for security.", Alert.AlertType.ERROR);
            return;
        }
        
        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            showAlert("Validation Error", "Password and confirm password do not match. Please re-enter.", Alert.AlertType.ERROR);
            return;
        }
        
        // Extract first and last name
        String[] nameParts = fullName.split("\\s+");
        String firstName = nameParts[0];
        String lastName = nameParts[nameParts.length - 1];
        
        // Print citizen registration details to terminal
        System.out.println("=== CITIZEN REGISTRATION SUCCESSFUL ===");
        System.out.println("Full Name: " + fullName);
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);
        System.out.println("Email: " + email);
        System.out.println("Mobile Number: " + phone);
        System.out.println("Password: [PROTECTED]"); // Don't print actual password for security
        System.out.println("Registration Date: " + java.time.LocalDateTime.now());
        System.out.println("=======================================");
        
        // Show success message
        showAlert("Registration Successful", 
                 "Citizen registration completed successfully!\n\n" +
                 "Welcome, " + firstName + " " + lastName + "!\n" +
                 "Email: " + email + "\n" +
                 "Mobile: " + phone + "\n\n" +
                 "You can now login to submit complaints and track their status.", 
                 Alert.AlertType.INFORMATION);
        
        // Clear all fields after successful registration
        clearFields();
        
        // Optionally redirect to login page
        // openLogin(event);
    }
    
    @FXML
    private void openLogin(ActionEvent event) {
        try {
            // Load the login FXML file (FXMLDocument.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) loginLink.getScene().getWindow();
            
            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login - Citizen Complaint System");
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not open login page. Please make sure FXMLDocument.fxml exists in the correct location.", Alert.AlertType.ERROR);
        }
    }
    
    private void clearFields() {
        fullNameField.clear();
        emailField.clear();
        phoneField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        
        // Reset focus to first field
        fullNameField.requestFocus();
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }
        
        // Split by whitespace and filter out empty strings
        String[] nameParts = fullName.trim().split("\\s+");
        
        // Must have at least 2 parts (first and last name)
        if (nameParts.length < 2) {
            return false;
        }
        
        // Each part should contain only letters (allowing international characters)
        for (String part : nameParts) {
            if (!part.matches("[\\p{L}]+")) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Enhanced email validation regex
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    
    private boolean isValidBangladeshiPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        // Remove any spaces or dashes
        phone = phone.replaceAll("[\\s-]", "");
        
        // Bangladesh mobile number validation
        // Starts with 01 and is 11 digits total
        // Valid prefixes: 013, 014, 015, 016, 017, 018, 019
        String bangladeshiPhoneRegex = "^01[3-9]\\d{8}$";
        return phone.matches(bangladeshiPhoneRegex);
    }
    
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // Check if password contains at least one letter and one number
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasLetter && hasDigit;
    }
}