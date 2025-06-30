package server.business;

import entities.*;
import server.config.ServerConfig;
import server.database.DBController;
import server.database.DatabaseQueries;
import server.utils.ServerLogger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Manages parking operations and spot availability.
 * Single Responsibility: Handles all parking-related business logic.
 */
public class ParkingManager {
    
    private static ParkingManager instance = null;
    private final DBController db;
    private final ServerLogger logger;
    
    private ParkingManager() {
        this.db = DBController.getInstance();
        this.logger = ServerLogger.getInstance();
    }
    
    public static synchronized ParkingManager getInstance() {
        if (instance == null) {
            instance = new ParkingManager();
        }
        return instance;
    }
    
    /**
     * Check parking availability
     */
    public Message checkAvailability(Message request) {
        try {
            ResultSet rs = db.executeQuery(DatabaseQueries.SELECT_AVAILABLE_SPOTS);
            if (rs.next()) {
                int available = rs.getInt(1);
                String status = String.format("Available spots: %d/%d", 
                    available, ServerConfig.TOTAL_PARKING_SPOTS);
                return new Message(MessageType.PARKING_AVAILABILITY_RESPONSE, status);
            }
            return new Message(MessageType.PARKING_AVAILABILITY_RESPONSE, "ERROR: Cannot check availability");
        } catch (SQLException e) {
            logger.logError("Availability check error: " + e.getMessage());
            return new Message(MessageType.PARKING_AVAILABILITY_RESPONSE, "ERROR: Database error");
        }
    }
    
    /**
     * Handle enter parking
     */
    public Message handleEnterParking(Message request) {
        try {
            String username = (String) request.getContent();
            
            // Check if user already has active parking
            ResultSet activeParking = db.executeQuery(
                DatabaseQueries.SELECT_ACTIVE_PARKING_BY_USER, username);
            
            if (activeParking.next()) {
                return new Message(MessageType.ENTER_PARKING_RESPONSE, 
                    "ERROR: You already have an active parking session");
            }
            
            // Find available spot
            int spotId = findAvailableSpot();
            if (spotId == -1) {
                return new Message(MessageType.ENTER_PARKING_RESPONSE, 
                    "ERROR: No available parking spots");
            }
            
            // Generate parking code
            String parkingCode = generateParkingCode();
            
            // Start transaction
            db.beginTransaction();
            
            // Create parking order
            LocalDateTime expectedExit = LocalDateTime.now().plusHours(1);
            db.executeUpdate(DatabaseQueries.INSERT_PARKING_ORDER, 
                username, parkingCode, expectedExit, spotId);
            
            // Update spot status
            db.executeUpdate(DatabaseQueries.UPDATE_PARKING_SPOT_STATUS, true, spotId);
            
            db.commitTransaction();
            
            logger.log("User " + username + " entered parking. Code: " + parkingCode);
            return new Message(MessageType.ENTER_PARKING_RESPONSE, 
                "SUCCESS: Parking code: " + parkingCode + ". Spot: " + spotId);
            
        } catch (SQLException e) {
            db.rollbackTransaction();
            logger.logError("Enter parking error: " + e.getMessage());
            return new Message(MessageType.ENTER_PARKING_RESPONSE, "ERROR: Failed to enter parking");
        }
    }
    
    /**
     * Handle exit parking
     */
    public Message handleExitParking(Message request) {
        try {
            String parkingCode = (String) request.getContent();
            
            // Get parking order
            ResultSet rs = db.executeQuery(
                "SELECT * FROM parking_orders WHERE parking_code = ? AND exit_time IS NULL", 
                parkingCode);
            
            if (!rs.next()) {
                return new Message(MessageType.EXIT_PARKING_RESPONSE, 
                    "ERROR: Invalid parking code or already exited");
            }
            
            LocalDateTime entryTime = rs.getTimestamp("entry_time").toLocalDateTime();
            LocalDateTime expectedExit = rs.getTimestamp("expected_exit_time").toLocalDateTime();
            int spotId = rs.getInt("spot_id");
            
            // Calculate cost
            LocalDateTime now = LocalDateTime.now();
            long hours = ChronoUnit.HOURS.between(entryTime, now) + 1; // Round up
            double cost = hours * ServerConfig.HOURLY_RATE;
            
            // Add penalty if overtime
            if (now.isAfter(expectedExit)) {
                long overtimeHours = ChronoUnit.HOURS.between(expectedExit, now) + 1;
                cost += overtimeHours * ServerConfig.HOURLY_RATE * (ServerConfig.PENALTY_RATE - 1);
            }
            
            // Start transaction
            db.beginTransaction();
            
            // Update parking order
            db.executeUpdate(DatabaseQueries.UPDATE_PARKING_EXIT, cost, parkingCode);
            
            // Free up spot
            db.executeUpdate(DatabaseQueries.UPDATE_PARKING_SPOT_STATUS, false, spotId);
            
            db.commitTransaction();
            
            logger.log("Parking exit: " + parkingCode + ", Cost: $" + cost);
            return new Message(MessageType.EXIT_PARKING_RESPONSE, 
                String.format("SUCCESS: Total cost: $%.2f. Thank you!", cost));
            
        } catch (SQLException e) {
            db.rollbackTransaction();
            logger.logError("Exit parking error: " + e.getMessage());
            return new Message(MessageType.EXIT_PARKING_RESPONSE, "ERROR: Failed to exit parking");
        }
    }
    
    /**
     * Handle extend parking
     */
    public Message handleExtendParking(Message request) {
        try {
            String[] data = ((String) request.getContent()).split(",");
            if (data.length != 2) {
                return new Message(MessageType.EXTEND_PARKING_RESPONSE, "ERROR: Invalid extension format");
            }
            
            String parkingCode = data[0].trim();
            int additionalHours = Integer.parseInt(data[1].trim());
            
            // Validate hours
            if (additionalHours <= 0 || additionalHours > ServerConfig.MAX_PARKING_HOURS) {
                return new Message(MessageType.EXTEND_PARKING_RESPONSE, 
                    "ERROR: Invalid hours. Must be between 1 and " + ServerConfig.MAX_PARKING_HOURS);
            }
            
            // Check if parking exists and is active
            ResultSet rs = db.executeQuery(
                "SELECT expected_exit_time FROM parking_orders WHERE parking_code = ? AND exit_time IS NULL", 
                parkingCode);
            
            if (!rs.next()) {
                return new Message(MessageType.EXTEND_PARKING_RESPONSE, 
                    "ERROR: Invalid parking code or already exited");
            }
            
            // Extend parking time
            int result = db.executeUpdate(DatabaseQueries.EXTEND_PARKING_TIME, 
                additionalHours, parkingCode);
            
            if (result > 0) {
                logger.log("Extended parking " + parkingCode + " by " + additionalHours + " hours");
                return new Message(MessageType.EXTEND_PARKING_RESPONSE, 
                    "SUCCESS: Parking extended by " + additionalHours + " hours");
            } else {
                return new Message(MessageType.EXTEND_PARKING_RESPONSE, "ERROR: Failed to extend parking");
            }
            
        } catch (Exception e) {
            logger.logError("Extend parking error: " + e.getMessage());
            return new Message(MessageType.EXTEND_PARKING_RESPONSE, "ERROR: Extension failed");
        }
    }
    
    /**
     * Get available parking spots count
     */
    public int getAvailableSpots() {
        try {
            ResultSet rs = db.executeQuery(DatabaseQueries.SELECT_AVAILABLE_SPOTS);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.logError("Get available spots error: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Find an available parking spot
     */
    private int findAvailableSpot() throws SQLException {
        ResultSet rs = db.executeQuery(
            "SELECT spot_id FROM parking_spots WHERE is_occupied = false LIMIT 1");
        if (rs.next()) {
            return rs.getInt("spot_id");
        }
        return -1;
    }
    
    /**
     * Generate unique parking code
     */
    private String generateParkingCode() {
        return "P" + System.currentTimeMillis() % 1000000;
    }
}