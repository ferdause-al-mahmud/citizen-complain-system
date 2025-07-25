package citizencomplainsystem;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * Controller for Admin Complaint Management
 */
public class AdminComplaintManagementController implements Initializable {

    @FXML private Button backButton;
    @FXML private Label totalComplaintsLabel;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private TextField searchTextField;
    @FXML private Button refreshButton;
    @FXML private TableView<ComplaintTableModel> complaintsTable;
    @FXML private TableColumn<ComplaintTableModel, String> idColumn;
    @FXML private TableColumn<ComplaintTableModel, String> titleColumn;
    @FXML private TableColumn<ComplaintTableModel, String> userColumn;
    @FXML private TableColumn<ComplaintTableModel, String> categoryColumn;
    @FXML private TableColumn<ComplaintTableModel, String> priorityColumn;
    @FXML private TableColumn<ComplaintTableModel, String> statusColumn;
    @FXML private TableColumn<ComplaintTableModel, String> dateColumn;
    @FXML private TableColumn<ComplaintTableModel, String> actionColumn;
    @FXML private Button updateStatusButton;
    @FXML private Button viewDetailsButton;
    @FXML private Button deleteComplaintButton;

    private String currentAdminEmail;
    private ObservableList<ComplaintTableModel> allComplaints;
    private ObservableList<ComplaintTableModel> filteredComplaints;
    private String complaintToReview;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        MongoDBConnector.connect();
        setupTable();
        setupFilters();
        loadComplaints();
    }

    public void setCurrentAdmin(String adminEmail) {
        this.currentAdminEmail = adminEmail;
    }

    public void setComplaintToReview(String complaintId) {
        this.complaintToReview = complaintId;
    }

    private void setupTable() {
        // Setup table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("complaintId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("submittedDate"));

        // Setup action column with buttons
        actionColumn.setCellFactory(column -> {
            return new TableCell<ComplaintTableModel, String>() {
                private final Button actionButton = new Button("Review");

                {
                    actionButton.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-background-radius: 5;");
                    actionButton.setOnAction(event -> {
                        ComplaintTableModel complaint = getTableView().getItems().get(getIndex());
                        updateComplaintStatusDialog(complaint.getComplaintId());
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(actionButton);
                    }
                }
            };
        });

        // Add row styling based on status
        complaintsTable.setRowFactory(tv -> {
            TableRow<ComplaintTableModel> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else {
                    switch (newItem.getStatus().toLowerCase()) {
                        case "pending":
                            row.setStyle("-fx-background-color: #fef3c7;");
                            break;
                        case "in progress":
                            row.setStyle("-fx-background-color: #dbeafe;");
                            break;
                        case "resolved":
                            row.setStyle("-fx-background-color: #dcfce7;");
                            break;
                        case "rejected":
                            row.setStyle("-fx-background-color: #fee2e2;");
                            break;
                        default:
                            row.setStyle("");
                    }
                }
            });
            return row;
        });
    }

    private void setupFilters() {
        // Status filter
        statusFilterComboBox.getItems().addAll(
            "All Status", "Pending", "In Progress", "Resolved", "Rejected"
        );
        statusFilterComboBox.setValue("All Status");

        // Category filter
        categoryFilterComboBox.getItems().addAll(
            "All Categories", "Road & Transportation", "Water Supply", "Electricity",
            "Garbage Collection", "Street Lighting", "Public Safety", "Noise Pollution",
            "Building & Construction", "Parks & Recreation", "Healthcare", "Education", "Other"
        );
        categoryFilterComboBox.setValue("All Categories");
    }

    private void loadComplaints() {
        try {
            List<Document> complaints = MongoDBConnector.getAllComplaints();
            allComplaints = FXCollections.observableArrayList();

            for (Document complaint : complaints) {
                ComplaintTableModel model = new ComplaintTableModel(
                    complaint.getString("complaintId"),
                    complaint.getString("title"),
                    complaint.getString("userEmail"),
                    complaint.getString("category"),
                    complaint.getString("priority"),
                    complaint.getString("status"),
                    formatDate(complaint.getString("submittedDate"))
                );
                allComplaints.add(model);
            }

            filteredComplaints = FXCollections.observableArrayList(allComplaints);
            complaintsTable.setItems(filteredComplaints);
            totalComplaintsLabel.setText("Total: " + allComplaints.size());

            // If there's a specific complaint to review, select it
            if (complaintToReview != null) {
                selectComplaintById(complaintToReview);
            }

        } catch (Exception e) {
            showAlert("Error", "Could not load complaints: " + e.getMessage());
        }
    }

    private void selectComplaintById(String complaintId) {
        for (ComplaintTableModel complaint : filteredComplaints) {
            if (complaint.getComplaintId().equals(complaintId)) {
                complaintsTable.getSelectionModel().select(complaint);
                complaintsTable.scrollTo(complaint);
                break;
            }
        }
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "Unknown";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateString);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return dateTime.format(formatter);
        } catch (DateTimeParseException e) {
            if (dateString.contains("T")) {
                return dateString.split("T")[0];
            }
            return dateString;
        }
    }

    @FXML
    private void filterComplaints(ActionEvent event) {
        applyFilters();
    }

    @FXML
    private void searchComplaints(KeyEvent event) {
        applyFilters();
    }

    private void applyFilters() {
        String statusFilter = statusFilterComboBox.getValue();
        String categoryFilter = categoryFilterComboBox.getValue();
        String searchText = searchTextField.getText().toLowerCase();

        filteredComplaints = allComplaints.stream()
            .filter(complaint -> {
                boolean statusMatch = "All Status".equals(statusFilter) || 
                                    complaint.getStatus().equals(statusFilter);
                boolean categoryMatch = "All Categories".equals(categoryFilter) || 
                                      complaint.getCategory().equals(categoryFilter);
                boolean searchMatch = searchText.isEmpty() || 
                                    complaint.getTitle().toLowerCase().contains(searchText) ||
                                    complaint.getComplaintId().toLowerCase().contains(searchText) ||
                                    complaint.getUserEmail().toLowerCase().contains(searchText);
                
                return statusMatch && categoryMatch && searchMatch;
            })
            .collect(Collectors.toCollection(FXCollections::observableArrayList));

        complaintsTable.setItems(filteredComplaints);
    }

    @FXML
    private void refreshComplaints(ActionEvent event) {
        loadComplaints();
        showAlert("Success", "Complaints refreshed successfully!");
    }

    @FXML
    private void updateComplaintStatus(ActionEvent event) {
        ComplaintTableModel selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a complaint to update.");
            return;
        }
        updateComplaintStatusDialog(selected.getComplaintId());
    }

    private void updateComplaintStatusDialog(String complaintId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UpdateComplaintStatus.fxml"));
            Parent root = loader.load();
            
            UpdateComplaintStatusController controller = loader.getController();
            controller.setComplaintId(complaintId);
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Update Complaint Status");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            
        } catch (Exception e) {
            showAlert("Error", "Could not open status update dialog: " + e.getMessage());
        }
    }

    @FXML
    private void viewComplaintDetails(ActionEvent event) {
        ComplaintTableModel selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a complaint to view details.");
            return;
        }

        try {
            Document complaint = MongoDBConnector.getComplaintById(selected.getComplaintId());
            if (complaint != null) {
                showComplaintDetailsDialog(complaint);
            }
        } catch (Exception e) {
            showAlert("Error", "Could not load complaint details: " + e.getMessage());
        }
    }

    private void showComplaintDetailsDialog(Document complaint) {
        StringBuilder details = new StringBuilder();
        details.append("Complaint ID: ").append(complaint.getString("complaintId")).append("\n\n");
        details.append("Title: ").append(complaint.getString("title")).append("\n\n");
        details.append("User Email: ").append(complaint.getString("userEmail")).append("\n");
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

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Complaint Details");
        alert.setHeaderText("Complete Complaint Information");
        alert.setContentText(details.toString());
        alert.getDialogPane().setPrefWidth(700);
        alert.getDialogPane().setPrefHeight(500);
        alert.showAndWait();
    }

    @FXML
    private void deleteComplaint(ActionEvent event) {
        ComplaintTableModel selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a complaint to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Complaint");
        confirmAlert.setHeaderText("Are you sure you want to delete this complaint?");
        confirmAlert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = MongoDBConnector.deleteComplaint(selected.getComplaintId());
                if (success) {
                    loadComplaints();
                    showAlert("Success", "Complaint deleted successfully!");
                } else {
                    showAlert("Error", "Failed to delete complaint.");
                }
            } catch (Exception e) {
                showAlert("Error", "Could not delete complaint: " + e.getMessage());
            }
        }
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

    public void refreshTable() {
        loadComplaints();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Table Model Class
    public static class ComplaintTableModel {
        private final SimpleStringProperty complaintId;
        private final SimpleStringProperty title;
        private final SimpleStringProperty userEmail;
        private final SimpleStringProperty category;
        private final SimpleStringProperty priority;
        private final SimpleStringProperty status;
        private final SimpleStringProperty submittedDate;

        public ComplaintTableModel(String complaintId, String title, String userEmail, 
                                 String category, String priority, String status, String submittedDate) {
            this.complaintId = new SimpleStringProperty(complaintId);
            this.title = new SimpleStringProperty(title);
            this.userEmail = new SimpleStringProperty(userEmail);
            this.category = new SimpleStringProperty(category);
            this.priority = new SimpleStringProperty(priority);
            this.status = new SimpleStringProperty(status);
            this.submittedDate = new SimpleStringProperty(submittedDate);
        }

        // Getters
        public String getComplaintId() { return complaintId.get(); }
        public String getTitle() { return title.get(); }
        public String getUserEmail() { return userEmail.get(); }
        public String getCategory() { return category.get(); }
        public String getPriority() { return priority.get(); }
        public String getStatus() { return status.get(); }
        public String getSubmittedDate() { return submittedDate.get(); }

        // Property getters for TableView
        public SimpleStringProperty complaintIdProperty() { return complaintId; }
        public SimpleStringProperty titleProperty() { return title; }
        public SimpleStringProperty userEmailProperty() { return userEmail; }
        public SimpleStringProperty categoryProperty() { return category; }
        public SimpleStringProperty priorityProperty() { return priority; }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleStringProperty submittedDateProperty() { return submittedDate; }
    }
}
