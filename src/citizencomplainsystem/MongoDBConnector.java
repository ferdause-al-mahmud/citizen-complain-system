/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package citizencomplainsystem;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

/**
 *
 * @author ferda
 */
public class MongoDBConnector {
    
    private static final String CONNECTION_STRING = 
        "mongodb+srv://CitizenComplainSystem:iY9KxVXMwVeg2qLl@cluster0.dnxxphb.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    // Connect to MongoDB
    public static void connect() {
        ConnectionString connString = new ConnectionString(CONNECTION_STRING);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .build();

        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("CitizenComplainSystem");
        System.out.println("Connected to MongoDB.");
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


}



