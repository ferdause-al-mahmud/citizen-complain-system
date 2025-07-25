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
 * Controller for updating complaint status
 */
public class UpdateComplaintStatusController implements Initializable {

    @FXML private Label complaintIdLabel;
    @FXML private Label complaintTitleLabel;
    @FXML private Label currentStatusLabel;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextArea commentsTextArea;
    @FXML private Button updateButton;
    @FXML private Button cancelButton;

    private String complaintId;
    private AdminComplaintManagementController parentController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize status options
        statusComboBox.getItems().clear();
        statusComboBox.getItems().addAll("Pending", "In Progress", "Resolved", "Rejected");
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
        loadComplaintInfo();
    }

    public void setParentController(AdminComplaintManagementController parentController) {
        this.parentController = parentController;
    }

    private void loadComplaintInfo() {
        try {
            Document complaint = MongoDBConnector.getComplaintById(complaintId);
            if (complaint != null) {
                complaintIdLabel.setText("Complaint ID: " + complaint.getString("complaintId"));
                complaintTitleLabel.setText("Title: " + complaint.getString("title"));
                currentStatusLabel.setText("Current Status: " + complaint.getString("status"));
                
                // Set current status as selected
                statusComboBox.setValue(complaint.getString("status"));
                
                // Load existing admin comments
                String existingComments = complaint.getString("adminComments");
                if (existingComments != null && !existingComments.trim().isEmpty()) {
                    commentsTextArea.setText(existingComments);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Could not load complaint information: " + e.getMessage());
        }
    }

    @FXML
    private void updateStatus(ActionEvent event) {
        String newStatus = statusComboBox.getValue();
        String comments = commentsTextArea.getText();

        if (newStatus == null || newStatus.trim().isEmpty()) {
            showAlert("Validation Error", "Please select a status.");
            return;
        }

        if (comments == null || comments.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter admin comments.");
            return;
        }

        try {
            boolean success = MongoDBConnector.updateComplaintStatus(complaintId, newStatus, comments);
            if (success) {
                showAlert("Success", "Complaint status updated successfully!");
                
                // Refresh parent table
                if (parentController != null) {
                    parentController.refreshTable();
                }
                
                // Close dialog
                closeDialog();
            } else {
                showAlert("Error", "Failed to update complaint status.");
            }
        } catch (Exception e) {
            showAlert("Error", "Could not update complaint status: " + e.getMessage());
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
