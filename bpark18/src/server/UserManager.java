package server.business;

import entities.*;
import server.database.DBController;
import server.database.DatabaseQueries;
import server.utils.ServerLogger;
import ocsf.server.ConnectionToClient;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages user authentication and user-related operations.
 * Single Responsibility: Handles all user management logic.
 */
public class UserManager {
    
    private static UserManager instance = null;
    private final DBController db;
    private final ServerLogger logger;
    private final ConcurrentHashMap<String, Boolean> loggedInUsers;
    
    private UserManager() {
        this.db = DBController.getInstance();
        this.logger = ServerLogger.getInstance();
        this.loggedInUsers = new ConcurrentHashMap<>();
    }
    
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }
    
    /**
     * Handle subscriber login
     */
    public Message handleLogin(Message request, ConnectionToClient client) {
        try {
            String[] loginData = ((String) request.getContent()).split(",");
            if (loginData.length < 2) {
                return new Message(MessageType.SUBSCRIBER_LOGIN_RESPONSE, "ERROR: Invalid login format");
            }
            
            String username = loginData[0].trim();
            String userCode = loginData[1].trim();
            
            // Check if already logged in
            if (loggedInUsers.containsKey(username)) {
                return new Message(MessageType.SUBSCRIBER_LOGIN_RESPONSE, "ERROR: User already logged in");
            }
            
            // Verify credentials
            ResultSet rs = db.executeQuery(DatabaseQueries.SELECT_USER_BY_USERNAME, username);
            
            if (rs.next()) {
                String dbUserCode = rs.getString("user_code");
                if (dbUserCode.equals(userCode)) {
                    // Create subscriber object
                    ParkingSubscriber subscriber = createSubscriberFromResultSet(rs);
                    
                    // Update login status
                    db.executeUpdate(DatabaseQueries.UPDATE_USER_LOGIN_STATUS, true, username);
                    loggedInUsers.put(username, true);
                    
                    logger.log("User " + username + " logged in successfully");
                    return new Message(MessageType.SUBSCRIBER_LOGIN_RESPONSE, subscriber);
                }
            }
            
            return new Message(MessageType.SUBSCRIBER_LOGIN_RESPONSE, null);
            
        } catch (SQLException e) {
            logger.logError("Login error: " + e.getMessage());
            return new Message(MessageType.SUBSCRIBER_LOGIN_RESPONSE, "ERROR: Database error");
        }
    }
    
    /**
     * Handle manager login
     */
    public Message handleManagerLogin(Message request, ConnectionToClient client) {
        try {
            String[] loginData = ((String) request.getContent()).split(",");
            if (loginData.length < 2) {
                return new Message(MessageType.MANAGER_LOGIN_RESPONSE, "ERROR: Invalid login format");
            }
            
            String username = loginData[0].trim();
            String password = loginData[1].trim();
            
            ResultSet rs = db.executeQuery(DatabaseQueries.SELECT_USER_BY_USERNAME, username);
            
            if (rs.next()) {
                String role = rs.getString("role");
                String dbPassword = rs.getString("user_code"); // Assuming password is stored in user_code
                
                if ((role.equals("MANAGER") || role.equals("ATTENDANT")) && dbPassword.equals(password)) {
                    db.executeUpdate(DatabaseQueries.UPDATE_USER_LOGIN_STATUS, true, username);
                    loggedInUsers.put(username, true);
                    
                    logger.log("Manager/Attendant " + username + " logged in");
                    return new Message(MessageType.MANAGER_LOGIN_RESPONSE, role);
                }
            }
            
            return new Message(MessageType.MANAGER_LOGIN_RESPONSE, null);
            
        } catch (SQLException e) {
            logger.logError("Manager login error: " + e.getMessage());
            return new Message(MessageType.MANAGER_LOGIN_RESPONSE, "ERROR: Database error");
        }
    }
    
    /**
     * Handle user registration
     */
    public Message handleRegistration(Message request, ConnectionToClient client) {
        try {
            String[] regData = ((String) request.getContent()).split(",");
            if (regData.length < 6) {
                return new Message(MessageType.REGISTRATION_RESPONSE, "ERROR: Invalid registration data");
            }
            
            String attendantUsername = regData[0].trim();
            String name = regData[1].trim();
            String phone = regData[2].trim();
            String email = regData[3].trim();
            String carNumber = regData[4].trim();
            String username = regData[5].trim();
            
            // Check if username exists
            ResultSet rs = db.executeQuery(DatabaseQueries.CHECK_USERNAME_EXISTS, username);
            if (rs.next() && rs.getInt(1) > 0) {
                return new Message(MessageType.REGISTRATION_RESPONSE, "ERROR: Username already exists");
            }
            
            // Generate unique user code
            int userCode = generateUniqueUserCode();
            
            // Insert new user
            int result = db.executeUpdate(DatabaseQueries.INSERT_USER, 
                username, String.valueOf(userCode), name, phone, email, carNumber, "SUBSCRIBER");
            
            if (result > 0) {
                logger.log("New subscriber registered: " + username);
                return new Message(MessageType.REGISTRATION_RESPONSE, 
                    "SUCCESS: User registered. Code: " + userCode);
            } else {
                return new Message(MessageType.REGISTRATION_RESPONSE, "ERROR: Registration failed");
            }
            
        } catch (SQLException e) {
            logger.logError("Registration error: " + e.getMessage());
            return new Message(MessageType.REGISTRATION_RESPONSE, "ERROR: Database error");
        }
    }
    
    /**
     * Handle lost code request
     */
    public Message handleLostCode(Message request) {
        try {
            String username = (String) request.getContent();
            
            ResultSet rs = db.executeQuery(DatabaseQueries.SELECT_USER_BY_USERNAME, username);
            if (rs.next()) {
                String userCode = rs.getString("user_code");
                String email = rs.getString("email");
                
                // In real implementation, send email
                logger.log("Sent user code to " + email + " for user " + username);
                
                return new Message(MessageType.LOST_CODE_RESPONSE, 
                    "SUCCESS: Code sent to registered email");
            } else {
                return new Message(MessageType.LOST_CODE_RESPONSE, 
                    "ERROR: Username not found");
            }
            
        } catch (SQLException e) {
            logger.logError("Lost code error: " + e.getMessage());
            return new Message(MessageType.LOST_CODE_RESPONSE, "ERROR: Database error");
        }
    }
    
    /**
     * Update subscriber information
     */
    public Message updateSubscriberInfo(Message request) {
        try {
            ParkingSubscriber subscriber = (ParkingSubscriber) request.getContent();
            
            int result = db.executeUpdate(DatabaseQueries.UPDATE_USER, 
                subscriber.getName(), subscriber.getPhone(), 
                subscriber.getEmail(), subscriber.getCarNumber(), 
                subscriber.getUsername());
            
            if (result > 0) {
                return new Message(MessageType.UPDATE_SUBSCRIBER_RESPONSE, "SUCCESS");
            } else {
                return new Message(MessageType.UPDATE_SUBSCRIBER_RESPONSE, "ERROR: Update failed");
            }
            
        } catch (SQLException e) {
            logger.logError("Update subscriber error: " + e.getMessage());
            return new Message(MessageType.UPDATE_SUBSCRIBER_RESPONSE, "ERROR: Database error");
        }
    }
    
    /**
     * Logout user
     */
    public void logoutUser(String username) {
        try {
            db.executeUpdate(DatabaseQueries.UPDATE_USER_LOGIN_STATUS, false, username);
            loggedInUsers.remove(username);
            logger.log("User " + username + " logged out");
        } catch (SQLException e) {
            logger.logError("Logout error: " + e.getMessage());
        }
    }
    
    /**
     * Create subscriber object from ResultSet
     */
    private ParkingSubscriber createSubscriberFromResultSet(ResultSet rs) throws SQLException {
        ParkingSubscriber subscriber = new ParkingSubscriber();
        subscriber.setSubscriberID(rs.getInt("user_id"));
        subscriber.setUsername(rs.getString("username"));
        subscriber.setName(rs.getString("name"));
        subscriber.setPhone(rs.getString("phone"));
        subscriber.setEmail(rs.getString("email"));
        subscriber.setCarNumber(rs.getString("car_number"));
        return subscriber;
    }
    
    /**
     * Generate unique user code
     */
    private int generateUniqueUserCode() {
        return 100000 + (int)(Math.random() * 900000);
    }
}