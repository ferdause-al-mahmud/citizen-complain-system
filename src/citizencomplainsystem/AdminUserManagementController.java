package citizencomplainsystem;

import java.io.IOException;
import java.net.URL;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * Controller for Admin User Management
 */
public class AdminUserManagementController implements Initializable {

    @FXML private Button backButton;
    @FXML private Label totalUsersLabel;
    @FXML private TextField searchTextField;
    @FXML private Button refreshButton;
    @FXML private Button addUserButton;
    @FXML private TableView<UserTableModel> usersTable;
    @FXML private TableColumn<UserTableModel, String> nameColumn;
    @FXML private TableColumn<UserTableModel, String> emailColumn;
    @FXML private TableColumn<UserTableModel, String> phoneColumn;
    @FXML private TableColumn<UserTableModel, String> roleColumn;
    @FXML private TableColumn<UserTableModel, String> complaintsCountColumn;
    @FXML private TableColumn<UserTableModel, String> actionColumn;
    @FXML private Button viewUserDetailsButton;
    @FXML private Button editUserButton;
    @FXML private Button deleteUserButton;

    private String currentAdminEmail;
    private ObservableList<UserTableModel> allUsers;
    private ObservableList<UserTableModel> filteredUsers;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        MongoDBConnector.connect();
        setupTable();
        loadUsers();
    }

    public void setCurrentAdmin(String adminEmail) {
        this.currentAdminEmail = adminEmail;
    }

    private void setupTable() {
        // Setup table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        complaintsCountColumn.setCellValueFactory(new PropertyValueFactory<>("complaintsCount"));

        // Setup action column with buttons
        actionColumn.setCellFactory(column -> {
            return new TableCell<UserTableModel, String>() {
                private final HBox actionBox = new HBox(5);
                private final Button viewButton = new Button("View");
                private final Button editButton = new Button("Edit");

                {
                    viewButton.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-size: 10px;");
                    editButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-size: 10px;");
                    
                    viewButton.setOnAction(event -> {
                        UserTableModel user = getTableView().getItems().get(getIndex());
                        viewUserDetailsDialog(user.getEmail());
                    });
                    
                    editButton.setOnAction(event -> {
                        UserTableModel user = getTableView().getItems().get(getIndex());
                        editUserDialog(user.getEmail());
                    });
                    
                    actionBox.getChildren().addAll(viewButton, editButton);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(actionBox);
                    }
                }
            };
        });
    }

    private void loadUsers() {
        try {
            List<Document> users = MongoDBConnector.getAllUsers();
            allUsers = FXCollections.observableArrayList();

            for (Document user : users) {
                // Count complaints for each user
                List<Document> userComplaints = MongoDBConnector.getUserComplaints(user.getString("email"));
                
                UserTableModel model = new UserTableModel(
                    user.getString("fullName"),
                    user.getString("email"),
                    user.getString("phone"),
                    user.getString("role") != null ? user.getString("role") : "user",
                    String.valueOf(userComplaints.size())
                );
                allUsers.add(model);
            }

            filteredUsers = FXCollections.observableArrayList(allUsers);
            usersTable.setItems(filteredUsers);
            totalUsersLabel.setText("Total Users: " + allUsers.size());

        } catch (Exception e) {
            showAlert("Error", "Could not load users: " + e.getMessage());
        }
    }

    @FXML
    private void searchUsers(KeyEvent event) {
        String searchText = searchTextField.getText().toLowerCase();
        
        filteredUsers = allUsers.stream()
            .filter(user -> {
                return searchText.isEmpty() || 
                       user.getFullName().toLowerCase().contains(searchText) ||
                       user.getEmail().toLowerCase().contains(searchText);
            })
            .collect(Collectors.toCollection(FXCollections::observableArrayList));

        usersTable.setItems(filteredUsers);
    }

    @FXML
    private void refreshUsers(ActionEvent event) {
        loadUsers();
        showAlert("Success", "Users refreshed successfully!");
    }

    @FXML
    private void addNewUser(ActionEvent event) {
        showAlert("Feature Coming Soon", "Add new user functionality will be implemented soon.");
    }

    @FXML
    private void viewUserDetails(ActionEvent event) {
        UserTableModel selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a user to view details.");
            return;
        }
        viewUserDetailsDialog(selected.getEmail());
    }

    private void viewUserDetailsDialog(String email) {
        try {
            Document user = MongoDBConnector.getUserByEmail(email);
            List<Document> userComplaints = MongoDBConnector.getUserComplaints(email);
            Document stats = MongoDBConnector.getUserComplaintStats(email);
            
            if (user != null) {
                StringBuilder details = new StringBuilder();
                details.append("User Details\n");
                details.append("===================\n\n");
                details.append("Full Name: ").append(user.getString("fullName")).append("\n");
                details.append("Email: ").append(user.getString("email")).append("\n");
                details.append("Phone: ").append(user.getString("phone")).append("\n");
                details.append("Role: ").append(user.getString("role") != null ? user.getString("role") : "user").append("\n\n");
                
                details.append("Complaint Statistics\n");
                details.append("===================\n");
                details.append("Total Complaints: ").append(stats.getLong("total")).append("\n");
                details.append("Pending: ").append(stats.getLong("pending")).append("\n");
                details.append("In Progress: ").append(stats.getLong("inProgress")).append("\n");
                details.append("Resolved: ").append(stats.getLong("resolved")).append("\n");
                details.append("Rejected: ").append(stats.getLong("rejected")).append("\n");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("User Details");
                alert.setHeaderText("Complete User Information");
                alert.setContentText(details.toString());
                alert.getDialogPane().setPrefWidth(500);
                alert.getDialogPane().setPrefHeight(400);
                alert.showAndWait();
            }
        } catch (Exception e) {
            showAlert("Error", "Could not load user details: " + e.getMessage());
        }
    }

    @FXML
    private void editUser(ActionEvent event) {
        UserTableModel selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a user to edit.");
            return;
        }
        editUserDialog(selected.getEmail());
    }

    private void editUserDialog(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditUser.fxml"));
            Parent root = loader.load();
        
            EditUserController controller = loader.getController();
            controller.setUserEmail(email);
            controller.setParentController(this);
        
            Stage stage = new Stage();
            stage.setTitle("Edit User - " + email);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        
        } catch (Exception e) {
            showAlert("Error", "Could not open edit user dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteUser(ActionEvent event) {
        UserTableModel selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a user to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete User");
        confirmAlert.setHeaderText("Are you sure you want to delete this user?");
        confirmAlert.setContentText("This will also delete all their complaints. This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // First delete all user's complaints
                List<Document> userComplaints = MongoDBConnector.getUserComplaints(selected.getEmail());
                for (Document complaint : userComplaints) {
                    MongoDBConnector.deleteComplaint(complaint.getString("complaintId"));
                }
                
                // Then delete the user
                boolean success = MongoDBConnector.deleteUser(selected.getEmail());
                if (success) {
                    loadUsers();
                    showAlert("Success", "User and all their complaints deleted successfully!");
                } else {
                    showAlert("Error", "Failed to delete user.");
                }
            } catch (Exception e) {
                showAlert("Error", "Could not delete user: " + e.getMessage());
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Add this method to refresh the table from external controllers
    public void refreshTable() {
        loadUsers();
    }

    // Table Model Class
    public static class UserTableModel {
        private final SimpleStringProperty fullName;
        private final SimpleStringProperty email;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty role;
        private final SimpleStringProperty complaintsCount;

        public UserTableModel(String fullName, String email, String phone, String role, String complaintsCount) {
            this.fullName = new SimpleStringProperty(fullName);
            this.email = new SimpleStringProperty(email);
            this.phone = new SimpleStringProperty(phone);
            this.role = new SimpleStringProperty(role);
            this.complaintsCount = new SimpleStringProperty(complaintsCount);
        }

        // Getters
        public String getFullName() { return fullName.get(); }
        public String getEmail() { return email.get(); }
        public String getPhone() { return phone.get(); }
        public String getRole() { return role.get(); }
        public String getComplaintsCount() { return complaintsCount.get(); }

        // Property getters for TableView
        public SimpleStringProperty fullNameProperty() { return fullName; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty phoneProperty() { return phone; }
        public SimpleStringProperty roleProperty() { return role; }
        public SimpleStringProperty complaintsCountProperty() { return complaintsCount; }
    }
}
