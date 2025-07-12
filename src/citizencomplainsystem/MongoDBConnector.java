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


}



