package client.service;

import client.core.ParkingClient;
import client.utils.ClientLogger;
import entities.*;
import java.util.List;

/**
 * Handles parking operations on the client side.
 * Single Responsibility: Manages parking-related requests and responses.
 */
public class ParkingService {
    
    private final ParkingClient client;
    private final ClientLogger logger;
    
    public ParkingService(ParkingClient client) {
        this.client = client;
        this.logger = ClientLogger.getInstance();
    }
    
    /**
     * Check parking availability
     */
    public void checkAvailability() {
        logger.log("Checking parking availability");
        
        Message availabilityMessage = new Message(
            MessageType.CHECK_PARKING_AVAILABILITY, 
            ""
        );
        
        client.sendMessage(availabilityMessage);
    }
    
    /**
     * Enter parking
     */
    public void enterParking(String username) {
        logger.log("Entering parking for user: " + username);
        
        Message enterMessage = new Message(
            MessageType.ENTER_PARKING, 
            username
        );
        
        client.sendMessage(enterMessage);
    }
    
    /**
     * Exit parking
     */
    public void exitParking(String parkingCode) {
        logger.log("Exiting parking with code: " + parkingCode);
        
        Message exitMessage = new Message(
            MessageType.EXIT_PARKING, 
            parkingCode
        );
        
        client.sendMessage(exitMessage);
    }
    
    /**
     * Extend parking time
     */
    public void extendParking(String parkingCode, int additionalHours) {
        logger.log("Extending parking " + parkingCode + " by " + additionalHours + " hours");
        
        Message extendMessage = new Message(
            MessageType.EXTEND_PARKING, 
            parkingCode + "," + additionalHours
        );
        
        client.sendMessage(extendMessage);
    }
    
    /**
     * Get parking history
     */
    public void getParkingHistory(String username) {
        logger.log("Requesting parking history for: " + username);
        
        Message historyMessage = new Message(
            MessageType.GET_PARKING_HISTORY, 
            username
        );
        
        client.sendMessage(historyMessage);
    }
    
    /**
     * Handle availability response
     */
    public void handleAvailabilityResponse(Message response) {
        String status = (String) response.getContent();
        
        if (status.startsWith("ERROR")) {
            System.out.println("❌ " + status);
        } else {
            System.out.println("🅿️ " + status);
        }
    }
    
    /**
     * Handle enter parking response
     */
    public void handleEnterResponse(Message response) {
        String result = (String) response.getContent();
        
        if (result.startsWith("SUCCESS")) {
            logger.log("Successfully entered parking");
            System.out.println("✅ " + result);
            System.out.println("⚠️  Please keep your parking code safe!");
        } else {
            logger.log("Failed to enter parking: " + result);
            System.out.println("❌ " + result);
        }
    }
    
    /**
     * Handle exit parking response
     */
    public void handleExitResponse(Message response) {
        String result = (String) response.getContent();
        
        if (result.startsWith("SUCCESS")) {
            logger.log("Successfully exited parking");
            System.out.println("✅ " + result);
        } else {
            logger.log("Failed to exit parking: " + result);
            System.out.println("❌ " + result);
        }
    }
    
    /**
     * Handle extend parking response
     */
    public void handleExtendResponse(Message response) {
        String result = (String) response.getContent();
        
        if (result.startsWith("SUCCESS")) {
            logger.log("Successfully extended parking");
            System.out.println("✅ " + result);
        } else {
            logger.log("Failed to extend parking: " + result);
            System.out.println("❌ " + result);
        }
    }
    
    /**
     * Handle parking history response
     */
    @SuppressWarnings("unchecked")
    public void handleHistoryResponse(Message response) {
        Object content = response.getContent();
        
        if (content instanceof List) {
            List<ParkingOrder> history = (List<ParkingOrder>) content;
            
            if (history.isEmpty()) {
                System.out.println("No parking history found.");
            } else {
                System.out.println("\n=== Parking History ===");
                System.out.println("Total records: " + history.size());
                
                for (ParkingOrder order : history) {
                    System.out.println("\n------------------------");
                    System.out.println("Parking Code: " + order.getParkingCode());
                    System.out.println("Entry Time: " + order.getEntryTime());
                    
                    if (order.getExitTime() != null) {
                        System.out.println("Exit Time: " + order.getExitTime());
                        System.out.println("Total Cost: $" + String.format("%.2f", order.getTotalCost()));
                    } else {
                        System.out.println("Status: Currently Parked");
                    }
                }
            }
        } else if (content instanceof String) {
            System.out.println("❌ " + content);
        }
    }
}