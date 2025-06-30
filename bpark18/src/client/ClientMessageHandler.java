package client.utils;

import entities.Message;
import entities.MessageType;

/**
 * Handles common message processing for the client.
 * Single Responsibility: Processes common message types and errors.
 */
public class ClientMessageHandler {
    
    private final ClientLogger logger;
    
    public ClientMessageHandler() {
        this.logger = ClientLogger.getInstance();
    }
    
    /**
     * Handle error messages
     */
    public void handleErrorMessage(Message message) {
        String error = (String) message.getContent();
        logger.logError("Server error: " + error);
        System.out.println("❌ Server Error: " + error);
    }
    
    /**
     * Display success message
     */
    public void displaySuccess(String message) {
        System.out.println("✅ " + message);
    }
    
    /**
     * Display error message
     */
    public void displayError(String message) {
        System.out.println("❌ " + message);
    }
    
    /**
     * Display info message
     */
    public void displayInfo(String message) {
        System.out.println("ℹ️ " + message);
    }
    
    /**
     * Display warning message
     */
    public void displayWarning(String message) {
        System.out.println("⚠️ " + message);
    }
    
    /**
     * Format monetary value
     */
    public String formatMoney(double amount) {
        return String.format("$%.2f", amount);
    }
    
    /**
     * Validate date format
     */
    public boolean isValidDateFormat(String date) {
        try {
            // Simple validation - in real app, use proper date parsing
            return date.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");
        } catch (Exception e) {
            return false;
        }
    }
}