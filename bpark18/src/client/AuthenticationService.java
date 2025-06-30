package client.service;

import client.core.ParkingClient;
import client.utils.ClientLogger;
import entities.*;

/**
 * Handles authentication operations on the client side.
 * Single Responsibility: Manages login, logout, and registration requests/responses.
 */
public class AuthenticationService {
    
    private final ParkingClient client;
    private final ClientLogger logger;
    private ParkingSubscriber currentUser;
    private String userRole;
    
    public AuthenticationService(ParkingClient client) {
        this.client = client;
        this.logger = ClientLogger.getInstance();
    }
    
    /**
     * Login as subscriber
     */
    public void loginSubscriber(String username, String userCode) {
        logger.log("Attempting subscriber login for: " + username);
        
        Message loginMessage = new Message(
            MessageType.SUBSCRIBER_LOGIN, 
            username + "," + userCode
        );
        
        client.sendMessage(loginMessage);
    }
    
    /**
     * Login as manager/attendant
     */
    public void loginManager(String username, String password) {
        logger.log("Attempting manager/attendant login for: " + username);
        
        Message loginMessage = new Message(
            MessageType.MANAGER_LOGIN, 
            username + "," + password
        );
        
        client.sendMessage(loginMessage);
    }
    
    /**
     * Register new subscriber
     */
    public void registerSubscriber(String attendantUsername, String name, 
                                 String phone, String email, String carNumber, String username) {
        logger.log("Registering new subscriber: " + username);
        
        String registrationData = String.join(",", 
            attendantUsername, name, phone, email, carNumber, username);
        
        Message registerMessage = new Message(
            MessageType.REGISTER_SUBSCRIBER, 
            registrationData
        );
        
        client.sendMessage(registerMessage);
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        if (currentUser != null) {
            logger.log("Logging out user: " + currentUser.getUsername());
            client.sendStringMessage("LoggedOut " + currentUser.getUsername());
            currentUser = null;
            userRole = null;
        }
    }
    
    /**
     * Request lost code
     */
    public void requestLostCode(String username) {
        logger.log("Requesting lost code for: " + username);
        
        Message lostCodeMessage = new Message(
            MessageType.REQUEST_LOST_CODE, 
            username
        );
        
        client.sendMessage(lostCodeMessage);
    }
    
    /**
     * Handle login response
     */
    public void handleLoginResponse(Message response) {
        Object content = response.getContent();
        
        if (content instanceof ParkingSubscriber) {
            currentUser = (ParkingSubscriber) content;
            userRole = "SUBSCRIBER";
            logger.log("Login successful for: " + currentUser.getUsername());
            System.out.println("✅ Login successful! Welcome " + currentUser.getName());
        } else if (content == null) {
            logger.log("Login failed - invalid credentials");
            System.out.println("❌ Login failed: Invalid username or code");
        } else if (content instanceof String && ((String) content).startsWith("ERROR")) {
            System.out.println("❌ " + content);
        }
    }
    
    /**
     * Handle manager login response
     */
    public void handleManagerLoginResponse(Message response) {
        Object content = response.getContent();
        
        if (content instanceof String && !((String) content).startsWith("ERROR")) {
            userRole = (String) content;
            logger.log("Manager/Attendant login successful. Role: " + userRole);
            System.out.println("✅ Login successful! Role: " + userRole);
        } else if (content == null) {
            logger.log("Manager login failed");
            System.out.println("❌ Login failed: Invalid credentials");
        } else {
            System.out.println("❌ " + content);
        }
    }
    
    /**
     * Handle registration response
     */
    public void handleRegistrationResponse(Message response) {
        String content = (String) response.getContent();
        
        if (content.startsWith("SUCCESS")) {
            logger.log("Registration successful");
            System.out.println("✅ " + content);
        } else {
            logger.log("Registration failed: " + content);
            System.out.println("❌ " + content);
        }
    }
    
    /**
     * Handle string login response (legacy)
     */
    public void handleStringLoginResponse(String response) {
        if ("LoginSuccessful".equals(response)) {
            System.out.println("✅ Login successful!");
        } else {
            System.out.println("❌ Login failed: " + response);
        }
    }
    
    // Getters
    public ParkingSubscriber getCurrentUser() { return currentUser; }
    public String getUserRole() { return userRole; }
    public boolean isLoggedIn() { return currentUser != null || userRole != null; }
}