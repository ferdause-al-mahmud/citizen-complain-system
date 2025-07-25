/* * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template */
package citizencomplainsystem;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.FindIterable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 * MongoDB Connector with User and Complaint Management
 * @author ferda
 */
public class MongoDBConnector {
    
//    private static final String CONNECTION_STRING = 
//        "mongodb+srv://CitizenComplainSystem:iY9KxVXMwVeg2qLl@cluster0.dnxxphb.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
//    
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    
   private static final String CONNECTION_STRING = "mongodb://localhost:27017";

public static void connect() {
    ConnectionString connString = new ConnectionString(CONNECTION_STRING);
    MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connString)
            .build();
    mongoClient = MongoClients.create(settings);
    database = mongoClient.getDatabase("CitizenComplainSystem");
    System.out.println("Connected to local MongoDB.");
}
    
    // Get the database object
    public static MongoDatabase getDatabase() {
        if (database == null) {
            connect();
        }
        return database;
    }
    
    // Close the connection when app exits
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed.");
        }
    }
    
    // ==================== USER MANAGEMENT METHODS ====================
    
    public static void insertUser(String fullName, String email, String phone, String password) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> users = db.getCollection("users");
            Document doc = new Document("fullName", fullName)
                    .append("email", email)
                    .append("phone", phone)
                    .append("role", "user")
                    .append("password", password);  // WARNING: Plaintext, only okay for testing
            users.insertOne(doc);
            System.out.println("Inserted user into MongoDB.");
        } catch (Exception e) {
            System.out.println("MongoDB insert failed: " + e.getMessage());
        }
    }
    
    public static Document authenticateAndGetUser(String email, String password) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> users = db.getCollection("users");
            Document query = new Document("email", email).append("password", password);
            Document user = users.find(query).first();  // returns null if not found
            return user;
        } catch (Exception e) {
            System.out.println("MongoDB authentication failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Updates user information in the database
     */
    public static boolean updateUserInfo(String currentEmail, String newFullName, String newEmail, String newPhone) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> users = db.getCollection("users");
            
            Document query = new Document("email", currentEmail);
            Document update = new Document("$set", new Document()
                .append("fullName", newFullName)
                .append("email", newEmail)
                .append("phone", newPhone));
            
            UpdateResult result = users.updateOne(query, update);
            return result.getModifiedCount() > 0;
            
        } catch (Exception e) {
            System.out.println("MongoDB update user info failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates user password in the database
     */
    public static boolean updateUserPassword(String email, String newPassword) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> users = db.getCollection("users");
            
            Document query = new Document("email", email);
            Document update = new Document("$set", new Document("password", newPassword));
            
            UpdateResult result = users.updateOne(query, update);
            return result.getModifiedCount() > 0;
            
        } catch (Exception e) {
            System.out.println("MongoDB update password failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a user from the database
     */
    public static boolean deleteUser(String email) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> users = db.getCollection("users");
            
            Document query = new Document("email", email);
            DeleteResult result = users.deleteOne(query);
            return result.getDeletedCount() > 0;
            
        } catch (Exception e) {
            System.out.println("MongoDB delete user failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if an email already exists in the database
     */
    public static boolean emailExists(String email) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> users = db.getCollection("users");
            
            Document query = new Document("email", email);
            return users.find(query).first() != null;
            
        } catch (Exception e) {
            System.out.println("MongoDB email check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets user data by email
     */
    public static Document getUserByEmail(String email) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> users = db.getCollection("users");
            
            Document query = new Document("email", email);
            return users.find(query).first();
            
        } catch (Exception e) {
            System.out.println("MongoDB get user failed: " + e.getMessage());
            return null;
        }
    }
    
    // ==================== COMPLAINT MANAGEMENT METHODS ====================
    
    /**
     * Inserts a new complaint into the database
     */
    public static boolean insertComplaint(String complaintId, String userEmail, String title, 
                                        String category, String priority, String location, 
                                        String description, String contactPhone, String submittedDate) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");
            
            Document complaint = new Document("complaintId", complaintId)
                    .append("userEmail", userEmail)
                    .append("title", title)
                    .append("category", category)
                    .append("priority", priority)
                    .append("location", location)
                    .append("description", description)
                    .append("contactPhone", contactPhone)
                    .append("status", "Pending")
                    .append("submittedDate", submittedDate)
                    .append("lastUpdated", submittedDate)
                    .append("adminComments", "")
                    .append("resolvedDate", "");
            
            complaints.insertOne(complaint);
            System.out.println("Complaint inserted successfully with ID: " + complaintId);
            return true;
            
        } catch (Exception e) {
            System.out.println("MongoDB complaint insert failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets all complaints for a specific user
     */
    public static List<Document> getUserComplaints(String userEmail) {
        List<Document> userComplaints = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");
            
            Document query = new Document("userEmail", userEmail);
            FindIterable<Document> results = complaints.find(query)
                    .sort(new Document("submittedDate", -1)); // Sort by newest first
            
            for (Document complaint : results) {
                userComplaints.add(complaint);
            }
            
            System.out.println("Retrieved " + userComplaints.size() + " complaints for user: " + userEmail);
            
        } catch (Exception e) {
            System.out.println("MongoDB get user complaints failed: " + e.getMessage());
        }
        return userComplaints;
    }
    
    /**
     * Gets all complaints (for admin use)
     */
    public static List<Document> getAllComplaints() {
        List<Document> allComplaints = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");
            
            FindIterable<Document> results = complaints.find()
                    .sort(new Document("submittedDate", -1)); // Sort by newest first
            
            for (Document complaint : results) {
                allComplaints.add(complaint);
            }
            
            System.out.println("Retrieved " + allComplaints.size() + " total complaints");
            
        } catch (Exception e) {
            System.out.println("MongoDB get all complaints failed: " + e.getMessage());
        }
        return allComplaints;
    }
    
    /**
     * Gets a specific complaint by ID
     */
    public static Document getComplaintById(String complaintId) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");
            
            Document query = new Document("complaintId", complaintId);
            return complaints.find(query).first();
            
        } catch (Exception e) {
            System.out.println("MongoDB get complaint by ID failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Updates complaint status
     */
    public static boolean updateComplaintStatus(String complaintId, String newStatus, String adminComments) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");
            
            Document query = new Document("complaintId", complaintId);
            Document update = new Document("$set", new Document()
                .append("status", newStatus)
                .append("adminComments", adminComments)
                .append("lastUpdated", java.time.LocalDateTime.now().toString()));
            
            // If status is resolved, add resolved date
            if ("Resolved".equalsIgnoreCase(newStatus)) {
                update.get("$set", Document.class).append("resolvedDate", 
                    java.time.LocalDateTime.now().toString());
            }
            
            UpdateResult result = complaints.updateOne(query, update);
            return result.getModifiedCount() > 0;
            
        } catch (Exception e) {
            System.out.println("MongoDB update complaint status failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a complaint
     */
    public static boolean deleteComplaint(String complaintId) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");
            
            Document query = new Document("complaintId", complaintId);
            DeleteResult result = complaints.deleteOne(query);
            return result.getDeletedCount() > 0;
            
        } catch (Exception e) {
            System.out.println("MongoDB delete complaint failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets complaint statistics for a user
     */
    public static Document getUserComplaintStats(String userEmail) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");
            
            // Count total complaints
            long totalComplaints = complaints.countDocuments(new Document("userEmail", userEmail));
            
            // Count pending complaints
            long pendingComplaints = complaints.countDocuments(
                new Document("userEmail", userEmail).append("status", "Pending"));
            
            // Count in-progress complaints
            long inProgressComplaints = complaints.countDocuments(
                new Document("userEmail", userEmail).append("status", "In Progress"));
            
            // Count resolved complaints
            long resolvedComplaints = complaints.countDocuments(
                new Document("userEmail", userEmail).append("status", "Resolved"));
            
            // Count rejected complaints
            long rejectedComplaints = complaints.countDocuments(
                new Document("userEmail", userEmail).append("status", "Rejected"));
            
            return new Document("total", totalComplaints)
                    .append("pending", pendingComplaints)
                    .append("inProgress", inProgressComplaints)
                    .append("resolved", resolvedComplaints)
                    .append("rejected", rejectedComplaints);
            
        } catch (Exception e) {
            System.out.println("MongoDB get complaint stats failed: " + e.getMessage());
            return new Document("total", 0)
                    .append("pending", 0)
                    .append("inProgress", 0)
                    .append("resolved", 0)
                    .append("rejected", 0);
        }
    }
    
    /**
     * Gets recent complaints for a user (last 5)
     */
    public static List<Document> getRecentUserComplaints(String userEmail, int limit) {
        List<Document> recentComplaints = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");
            
            Document query = new Document("userEmail", userEmail);
            FindIterable<Document> results = complaints.find(query)
                    .sort(new Document("submittedDate", -1))
                    .limit(limit);
            
            for (Document complaint : results) {
                recentComplaints.add(complaint);
            }
            
        } catch (Exception e) {
            System.out.println("MongoDB get recent complaints failed: " + e.getMessage());
        }
        return recentComplaints;
    }
    
    /**
     * Searches complaints by keyword
     */
    public static List<Document> searchComplaints(String userEmail, String keyword) {
        List<Document> searchResults = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");
            
            // Create a regex pattern for case-insensitive search
            Document regexQuery = new Document("$regex", keyword).append("$options", "i");
            
            Document query = new Document("userEmail", userEmail)
                    .append("$or", List.of(
                        new Document("title", regexQuery),
                        new Document("description", regexQuery),
                        new Document("category", regexQuery),
                        new Document("location", regexQuery),
                        new Document("complaintId", regexQuery)
                    ));
            
            FindIterable<Document> results = complaints.find(query)
                    .sort(new Document("submittedDate", -1));
            
            for (Document complaint : results) {
                searchResults.add(complaint);
            }
            
        } catch (Exception e) {
            System.out.println("MongoDB search complaints failed: " + e.getMessage());
        }
        return searchResults;
    }
    
    /**
     * Gets complaints by status for a user
     */
    public static List<Document> getComplaintsByStatus(String userEmail, String status) {
        List<Document> statusComplaints = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");
            
            Document query = new Document("userEmail", userEmail).append("status", status);
            FindIterable<Document> results = complaints.find(query)
                    .sort(new Document("submittedDate", -1));
            
            for (Document complaint : results) {
                statusComplaints.add(complaint);
            }
            
        } catch (Exception e) {
            System.out.println("MongoDB get complaints by status failed: " + e.getMessage());
        }
        return statusComplaints;
    }
    
    /**
     * Checks if a complaint ID already exists
     */
    public static boolean complaintIdExists(String complaintId) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");
            
            Document query = new Document("complaintId", complaintId);
            return complaints.find(query).first() != null;
            
        } catch (Exception e) {
            System.out.println("MongoDB complaint ID check failed: " + e.getMessage());
            return false;
        }
    }
    
    
     // ==================== ADMIN MANAGEMENT METHODS ====================

    /**
     * Gets all users (for admin use)
     */
  public static List<Document> getAllUsers() {
        List<Document> allUsers = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> users = db.getCollection("users");

            FindIterable<Document> results = users.find()
                    .sort(new Document("fullName", 1)); // Sort by name

            for (Document user : results) {
                allUsers.add(user);
            }

            System.out.println("Retrieved " + allUsers.size() + " total users");

        } catch (Exception e) {
            System.out.println("MongoDB get all users failed: " + e.getMessage());
        }
        return allUsers;
    }
    /**
     * Gets overall system statistics
     */
   public static Document getSystemStats() {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");
            MongoCollection<Document> users = db.getCollection("users");

            // Count total complaints
            long totalComplaints = complaints.countDocuments();

            // Count pending complaints
            long pendingComplaints = complaints.countDocuments(new Document("status", "Pending"));

            // Count in-progress complaints
            long inProgressComplaints = complaints.countDocuments(new Document("status", "In Progress"));

            // Count resolved complaints
            long resolvedComplaints = complaints.countDocuments(new Document("status", "Resolved"));

            // Count rejected complaints
            long rejectedComplaints = complaints.countDocuments(new Document("status", "Rejected"));

            // Count total users
            long totalUsers = users.countDocuments();

            return new Document("totalComplaints", totalComplaints)
                    .append("pendingComplaints", pendingComplaints)
                    .append("inProgressComplaints", inProgressComplaints)
                    .append("resolvedComplaints", resolvedComplaints)
                    .append("rejectedComplaints", rejectedComplaints)
                    .append("totalUsers", totalUsers);

        } catch (Exception e) {
            System.out.println("MongoDB get system stats failed: " + e.getMessage());
            return new Document("totalComplaints", 0)
                    .append("pendingComplaints", 0)
                    .append("inProgressComplaints", 0)
                    .append("resolvedComplaints", 0)
                    .append("rejectedComplaints", 0)
                    .append("totalUsers", 0);
        }
    }
    /**
     * Gets complaints by category for reports
     */
  public static List<Document> getComplaintsByCategory() {
        List<Document> categoryStats = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");

            String[] categories = {
                    "Road & Transportation", "Water Supply", "Electricity", "Garbage Collection",
                    "Street Lighting", "Public Safety", "Noise Pollution", "Building & Construction",
                    "Parks & Recreation", "Healthcare", "Education", "Other"
            };

            for (String category : categories) {
                long count = complaints.countDocuments(new Document("category", category));
                if (count > 0) {
                    categoryStats.add(new Document("category", category).append("count", count));
                }
            }

        } catch (Exception e) {
            System.out.println("MongoDB get complaints by category failed: " + e.getMessage());
        }
        return categoryStats;
    }

    /**
     * Gets complaints by status for reports
     */
   public static List<Document> getComplaintsByStatusReport() {
        List<Document> statusStats = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");

            String[] statuses = {"Pending", "In Progress", "Resolved", "Rejected"};

            for (String status : statuses) {
                long count = complaints.countDocuments(new Document("status", status));
                statusStats.add(new Document("status", status).append("count", count));
            }

        } catch (Exception e) {
            System.out.println("MongoDB get complaints by status report failed: " + e.getMessage());
        }
        return statusStats;
    }
    /**
     * Gets recent complaints for admin dashboard
     */
     public static List<Document> getRecentComplaints(int limit) {
        List<Document> recentComplaints = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");

            FindIterable<Document> results = complaints.find()
                    .sort(new Document("submittedDate", -1))
                    .limit(limit);

            for (Document complaint : results) {
                recentComplaints.add(complaint);
            }

        } catch (Exception e) {
            System.out.println("MongoDB get recent complaints failed: " + e.getMessage());
        }
        return recentComplaints;
    }
    /**
     * Gets pending complaints for admin review
     */
public static List<Document> getPendingComplaints() {
        List<Document> pendingComplaints = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> complaints = db.getCollection("complaints");

            Document query = new Document("status", "Pending");
            FindIterable<Document> results = complaints.find(query)
                    .sort(new Document("submittedDate", -1));

            for (Document complaint : results) {
                pendingComplaints.add(complaint);
            }

        } catch (Exception e) {
            System.out.println("MongoDB get pending complaints failed: " + e.getMessage());
        }
        return pendingComplaints;
    }

 /**
     * Updates user information including role
     */
    public static boolean updateUser(String email, String fullName, String phone, String role, boolean resetPassword) {
        try {
            MongoDatabase db = getDatabase();
            MongoCollection<Document> users = db.getCollection("users");

            Document filter = new Document("email", email);
            Document update = new Document("$set", new Document()
                    .append("fullName", fullName)
                    .append("phone", phone)
                    .append("role", role)
                    .append("lastUpdated", LocalDateTime.now().toString()));

            // If password reset is requested, set default password
            if (resetPassword) {
                // In a real application, you should hash the password
                update.get("$set", Document.class).append("password", "password123");
                update.get("$set", Document.class).append("passwordResetRequired", true);
            }

            long modifiedCount = users.updateOne(filter, update).getModifiedCount();

            if (modifiedCount > 0) {
                System.out.println("User updated successfully: " + email);
                System.out.println("New role: " + role);
                if (resetPassword) {
                    System.out.println("Password reset to default");
                }
                return true;
            } else {
                System.out.println("No user found with email: " + email);
                return false;
            }

        } catch (Exception e) {
            System.out.println("MongoDB update user failed: " + e.getMessage());
            return false;
        }
    }


}


