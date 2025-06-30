package client.service;

import client.core.ParkingClient;
import client.utils.ClientLogger;
import entities.*;

/**
 * Handles reservation operations on the client side.
 * Single Responsibility: Manages reservation-related requests and responses.
 */
public class ReservationService {
    
    private final ParkingClient client;
    private final ClientLogger logger;
    
    public ReservationService(ParkingClient client) {
        this.client = client;
        this.logger = ClientLogger.getInstance();
    }
    
    /**
     * Make a parking reservation
     */
    public void makeReservation(String username, String dateTime) {
        logger.log("Making reservation for " + username + " at " + dateTime);
        
        Message reservationMessage = new Message(
            MessageType.RESERVE_PARKING, 
            username + "," + dateTime
        );
        
        client.sendMessage(reservationMessage);
    }
    
    /**
     * Cancel reservation
     */
    public void cancelReservation(String username, int reservationCode) {
        logger.log("Cancelling reservation " + reservationCode + " for " + username);
        
        Message cancelMessage = new Message(
            MessageType.CANCEL_RESERVATION, 
            username + "," + reservationCode
        );
        
        client.sendMessage(cancelMessage);
    }
    
    /**
     * Activate reservation (enter parking with reservation)
     */
    public void activateReservation(String username, int reservationCode) {
        logger.log("Activating reservation " + reservationCode + " for " + username);
        
        Message activateMessage = new Message(
            MessageType.ACTIVATE_RESERVATION, 
            username + "," + reservationCode
        );
        
        client.sendMessage(activateMessage);
    }
    
    /**
     * Handle reservation response
     */
    public void handleReservationResponse(Message response) {
        String result = (String) response.getContent();
        
        if (result.startsWith("SUCCESS")) {
            logger.log("Reservation successful");
            System.out.println("✅ " + result);
            System.out.println("⚠️  Please save your reservation code!");
        } else {
            logger.log("Reservation failed: " + result);
            System.out.println("❌ " + result);
        }
    }
    
    /**
     * Handle cancellation response
     */
    public void handleCancellationResponse(Message response) {
        String result = (String) response.getContent();
        
        if (result.startsWith("SUCCESS")) {
            logger.log("Cancellation successful");
            System.out.println("✅ " + result);
        } else {
            logger.log("Cancellation failed: " + result);
            System.out.println("❌ " + result);
        }
    }
    
    /**
     * Handle activation response
     */
    public void handleActivationResponse(Message response) {
        String result = (String) response.getContent();
        
        if (result.startsWith("SUCCESS")) {
            logger.log("Activation successful");
            System.out.println("✅ " + result);
        } else {
            logger.log("Activation failed: " + result);
            System.out.println("❌ " + result);
        }
    }
}