package citizencomplainsystem;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * Controller for editing user information
 */
public class EditUserController implements Initializable {

    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label currentRoleLabel;
    @FXML private TextField fullNameTextField;
    @FXML private TextField phoneTextField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private CheckBox resetPasswordCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button resetButton;

    private String userEmail;
    private Document originalUserData;
    private AdminUserManagementController parentController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize role options
        roleComboBox.getItems().clear();
        roleComboBox.getItems().addAll("user", "admin", "moderator");
        
        // Set default selection
        roleComboBox.setValue("user");
    }

    /**
     * Set the user email to edit
     */
    public void setUserEmail(String email) {
        this.userEmail = email;
        loadUserInfo();
    }

    /**
     * Set the parent controller for refreshing the table
     */
    public void setParentController(AdminUserManagementController parentController) {
        this.parentController = parentController;
    }

    /**
     * Load user information from database
     */
    private void loadUserInfo() {
        try {
            originalUserData = MongoDBConnector.getUserByEmail(userEmail);
            if (originalUserData != null) {
                // Display current information
                fullNameLabel.setText(originalUserData.getString("fullName"));
                emailLabel.setText(originalUserData.getString("email"));
                phoneLabel.setText(originalUserData.getString("phone"));
                
                String currentRole = originalUserData.getString("role");
                if (currentRole == null || currentRole.trim().isEmpty()) {
                    currentRole = "user";
                }
                currentRoleLabel.setText(currentRole);
                
                // Populate edit fields with current values
                fullNameTextField.setText(originalUserData.getString("fullName"));
                phoneTextField.setText(originalUserData.getString("phone"));
                roleComboBox.setValue(currentRole);
                
                System.out.println("Loaded user info for editing: " + userEmail);
            } else {
                showAlert("Error", "User not found: " + userEmail);
                closeDialog();
            }
        } catch (Exception e) {
            showAlert("Error", "Could not load user information: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save changes to the user
     */
    @FXML
    private void saveChanges(ActionEvent event) {
        // Validate input
        String newFullName = fullNameTextField.getText();
        String newPhone = phoneTextField.getText();
        String newRole = roleComboBox.getValue();
        boolean resetPassword = resetPasswordCheckBox.isSelected();

        if (newFullName == null || newFullName.trim().isEmpty()) {
            showAlert("Validation Error", "Full name cannot be empty.");
            return;
        }

        if (newPhone == null || newPhone.trim().isEmpty()) {
            showAlert("Validation Error", "Phone number cannot be empty.");
            return;
        }

        if (newRole == null || newRole.trim().isEmpty()) {
            showAlert("Validation Error", "Please select a user role.");
            return;
        }

        // Validate phone number format (basic validation)
        if (!newPhone.matches("\\d{10,15}")) {
            showAlert("Validation Error", "Please enter a valid phone number (10-15 digits).");
            return;
        }

        // Confirm role change if changing to admin
        if ("admin".equals(newRole) && !"admin".equals(originalUserData.getString("role"))) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Role Change");
            confirmAlert.setHeaderText("Grant Administrative Privileges");
            confirmAlert.setContentText("Are you sure you want to grant administrative privileges to this user?\n\n" +
                                      "This will allow them to:\n" +
                                      "• Manage all complaints\n" +
                                      "• Manage all users\n" +
                                      "• Access system reports\n" +
                                      "• Perform administrative actions");
            
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        // Confirm password reset if selected
        if (resetPassword) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Password Reset");
            confirmAlert.setHeaderText("Reset User Password");
            confirmAlert.setContentText("Are you sure you want to reset the password for this user?\n\n" +
                                      "The password will be changed to: password123\n" +
                                      "The user will need to change it on their next login.");
            
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        try {
            // Update user information
            boolean success = MongoDBConnector.updateUser(userEmail, newFullName, newPhone, newRole, resetPassword);
            
            if (success) {
                StringBuilder message = new StringBuilder("User information updated successfully!");
                
                if (resetPassword) {
                    message.append("\n\nPassword has been reset to: password123");
                    message.append("\nPlease inform the user to change their password on next login.");
                }
                
                showAlert("Success", message.toString());
                
                // Refresh parent table
                if (parentController != null) {
                    parentController.refreshTable();
                }
                
                // Close dialog
                closeDialog();
            } else {
                showAlert("Error", "Failed to update user information.");
            }
        } catch (Exception e) {
            showAlert("Error", "Could not update user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reset fields to original values
     */
    @FXML
    private void resetFields(ActionEvent event) {
        if (originalUserData != null) {
            fullNameTextField.setText(originalUserData.getString("fullName"));
            phoneTextField.setText(originalUserData.getString("phone"));
            
            String originalRole = originalUserData.getString("role");
            if (originalRole == null || originalRole.trim().isEmpty()) {
                originalRole = "user";
            }
            roleComboBox.setValue(originalRole);
            
            resetPasswordCheckBox.setSelected(false);
            
            showAlert("Reset Complete", "All fields have been reset to original values.");
        }
    }

    /**
     * Cancel editing and close dialog
     */
    @FXML
    private void cancel(ActionEvent event) {
        closeDialog();
    }

    /**
     * Close the dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setPrefWidth(400);
        alert.showAndWait();
    }
}
