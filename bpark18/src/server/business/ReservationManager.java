package server.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import entities.Message;
import entities.MessageType;
import server.DBController;
import server.DatabaseQueries;
import server.utils.ServerLogger;

/**
 * Manages parking reservations.
 * Single Responsibility: Handles all reservation-related business logic.
 */
public class ReservationManager {
    
    private static ReservationManager instance = null;
    private final DBController db;
    private final ServerLogger logger;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private ReservationManager() {
        this.db = DBController.getInstance();
        this.logger = ServerLogger.getInstance();
    }
    
    public static synchronized ReservationManager getInstance() {
        if (instance == null) {
            instance = new ReservationManager();
        }
        return instance;
    }
    
    /**
     * Handle new reservation
     */
    public Message handleReservation(Message request) {
        try {
            String[] data = ((String) request.getContent()).split(",");
            if (data.length < 2) {
                return new Message(MessageType.RESERVATION_RESPONSE, "ERROR: Invalid reservation data");
            }
            
            String username = data[0].trim();
            String dateTimeStr = data[1].trim();
            
            // Parse reservation date
            LocalDateTime reservationDate;
            try {
                reservationDate = LocalDateTime.parse(dateTimeStr, dateFormatter);
            } catch (Exception e) {
                return new Message(MessageType.RESERVATION_RESPONSE, 
                    "ERROR: Invalid date format. Use: yyyy-MM-dd HH:mm");
            }
            
            // Validate future date
            if (reservationDate.isBefore(LocalDateTime.now())) {
                return new Message(MessageType.RESERVATION_RESPONSE, 
                    "ERROR: Reservation must be for future date/time");
            }
            
            // Check for existing active reservations
            ResultSet existing = db.executeQuery(
                DatabaseQueries.SELECT_ACTIVE_RESERVATIONS_BY_USER, username);
            
            if (existing.next()) {
                return new Message(MessageType.RESERVATION_RESPONSE, 
                    "ERROR: You already have an active reservation");
            }
            
            // Generate reservation code
            int reservationCode = generateReservationCode();
            
            // Create reservation
            int result = db.executeUpdate(DatabaseQueries.INSERT_RESERVATION, 
                username, reservationCode, reservationDate);
            
            if (result > 0) {
                logger.log("Reservation created for " + username + ". Code: " + reservationCode);
                return new Message(MessageType.RESERVATION_RESPONSE, 
                    "SUCCESS: Reservation confirmed. Code: " + reservationCode);
            } else {
                return new Message(MessageType.RESERVATION_RESPONSE, "ERROR: Failed to create reservation");
            }
            
        } catch (SQLException e) {
            logger.logError("Reservation error: " + e.getMessage());
            return new Message(MessageType.RESERVATION_RESPONSE, "ERROR: Database error");
        }
    }
    
    /**
     * Handle reservation cancellation
     */
    public Message handleCancellation(Message request) {
        try {
            String[] data = ((String) request.getContent()).split(",");
            if (data.length < 2) {
                return new Message(MessageType.CANCELLATION_RESPONSE, "ERROR: Invalid cancellation data");
            }
            
            String username = data[0].trim();
            int reservationCode;
            
            try {
                reservationCode = Integer.parseInt(data[1].trim());
            } catch (NumberFormatException e) {
                return new Message(MessageType.CANCELLATION_RESPONSE, "ERROR: Invalid reservation code");
            }
            
            // Verify reservation ownership
            ResultSet rs = db.executeQuery(
                "SELECT * FROM reservations WHERE reservation_code = ? AND username = ? AND status = 'ACTIVE'",
                reservationCode, username);
            
            if (!rs.next()) {
                return new Message(MessageType.CANCELLATION_RESPONSE, 
                    "ERROR: Reservation not found or not active");
            }
            
            // Cancel reservation
            int result = db.executeUpdate(DatabaseQueries.UPDATE_RESERVATION_STATUS, 
                "CANCELLED", reservationCode);
            
            if (result > 0) {
                logger.log("Reservation " + reservationCode + " cancelled by " + username);
                return new Message(MessageType.CANCELLATION_RESPONSE, 
                    "SUCCESS: Reservation cancelled");
            } else {
                return new Message(MessageType.CANCELLATION_RESPONSE, "ERROR: Failed to cancel reservation");
            }
            
        } catch (SQLException e) {
            logger.logError("Cancellation error: " + e.getMessage());
            return new Message(MessageType.CANCELLATION_RESPONSE, "ERROR: Database error");
        }
    }
    
    /**
     * Handle reservation activation (entering with reservation)
     */
    public Message handleActivation(Message request) {
        try {
            String[] data = ((String) request.getContent()).split(",");
            if (data.length < 2) {
                return new Message(MessageType.ACTIVATION_RESPONSE, "ERROR: Invalid activation data");
            }
            
            String username = data[0].trim();
            int reservationCode;
            
            try {
                reservationCode = Integer.parseInt(data[1].trim());
            } catch (NumberFormatException e) {
                return new Message(MessageType.ACTIVATION_RESPONSE, "ERROR: Invalid reservation code");
            }
            
            // Verify reservation
            ResultSet rs = db.executeQuery(DatabaseQueries.SELECT_RESERVATION_BY_CODE, reservationCode);
            
            if (!rs.next()) {
                return new Message(MessageType.ACTIVATION_RESPONSE, 
                    "ERROR: Reservation not found or not active");
            }
            
            String resUsername = rs.getString("username");
            LocalDateTime resDate = rs.getTimestamp("reservation_date").toLocalDateTime();
            
            // Verify ownership
            if (!resUsername.equals(username)) {
                return new Message(MessageType.ACTIVATION_RESPONSE, 
                    "ERROR: Reservation does not belong to you");
            }
            
            // Check reservation time (allow 30 minutes window)
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(resDate.minusMinutes(30)) || now.isAfter(resDate.plusMinutes(30))) {
                return new Message(MessageType.ACTIVATION_RESPONSE, 
                    "ERROR: Reservation can only be activated within 30 minutes of reserved time");
            }
            
            // Start transaction
            db.beginTransaction();
            
            // Update reservation status
            db.executeUpdate(DatabaseQueries.UPDATE_RESERVATION_STATUS, "ACTIVATED", reservationCode);
            
            // Create parking entry
            ParkingManager parkingManager = ParkingManager.getInstance();
            Message parkingResult = parkingManager.handleEnterParking(
                new Message(MessageType.ENTER_PARKING, username));
            
            db.commitTransaction();
            
            if (parkingResult.getContent().toString().startsWith("SUCCESS")) {
                logger.log("Reservation " + reservationCode + " activated");
                return new Message(MessageType.ACTIVATION_RESPONSE, 
                    "SUCCESS: Reservation activated. " + parkingResult.getContent());
            } else {
                db.rollbackTransaction();
                return new Message(MessageType.ACTIVATION_RESPONSE, 
                    "ERROR: Failed to activate reservation");
            }
            
        } catch (Exception e) {
            db.rollbackTransaction();
            logger.logError("Activation error: " + e.getMessage());
            return new Message(MessageType.ACTIVATION_RESPONSE, "ERROR: Activation failed");
        }
    }
    
    /**
     * Generate unique reservation code
     */
    private int generateReservationCode() {
        return 10000 + (int)(Math.random() * 90000);
    }
}
