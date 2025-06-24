/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML2.java to edit this template
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author ferda
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Hyperlink signUpLink;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization code if needed
    }    

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        
        // Validate fields
        if (email.isEmpty() && password.isEmpty()) {
            showAlert("Validation Error", "Please fill in both email and password fields.");
            return;
        }
        
        if (email.isEmpty()) {
            showAlert("Validation Error", "Please enter your email address.");
            return;
        }
        
        if (password.isEmpty()) {
            showAlert("Validation Error", "Please enter your password.");
            return;
        }
        
        // Basic email validation
        if (!isValidEmail(email)) {
            showAlert("Validation Error", "Please enter a valid email address.");
            return;
        }
        
        // Print values to terminal
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
        System.out.println("=====================");
        
        // Show success message
        showAlert("Login Successful", "Login details printed to console.\nEmail: " + email);
    }
    
    @FXML
    private void openRegister(ActionEvent event) {
        try {
            // Load the register.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("register.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) signUpLink.getScene().getWindow();
            
            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Register - Citizen Complaint System");
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open registration page. Please make sure register.fxml exists.");
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private boolean isValidEmail(String email) {
        // Basic email validation regex
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}