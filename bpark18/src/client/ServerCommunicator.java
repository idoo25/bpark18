package client.core;

import entities.Message;
import entities.MessageType;
import client.service.*;
import client.utils.ClientLogger;
import client.utils.ClientMessageHandler;
import java.io.*;

/**
 * Handles communication with the server.
 * Single Responsibility: Manages message routing and processing from server.
 */
public class ServerCommunicator {
    
    private final ParkingClient client;
    private final ClientMessageHandler messageHandler;
    private final ClientLogger logger;
    
    // Services
    private final AuthenticationService authService;
    private final ParkingService parkingService;
    private final ReservationService reservationService;
    
    public ServerCommunicator(ParkingClient client) {
        this.client = client;
        this.messageHandler = new ClientMessageHandler();
        this.logger = ClientLogger.getInstance();
        
        // Initialize services
        this.authService = new AuthenticationService(client);
        this.parkingService = new ParkingService(client);
        this.reservationService = new ReservationService(client);
    }
    
    /**
     * Handle message received from server
     */
    public void handleServerMessage(Object msg) {
        try {
            // Deserialize if needed
            if (msg instanceof byte[]) {
                msg = deserializeMessage(msg);
            }
            
            if (msg instanceof Message) {
                Message message = (Message) msg;
                logger.logDebug("Received message type: " + message.getType());
                routeMessage(message);
            } else if (msg instanceof String) {
                logger.logDebug("Received string message: " + msg);
                handleStringMessage((String) msg);
            } else {
                logger.logError("Unknown message type received");
            }
            
        } catch (Exception e) {
            logger.logError("Error handling server message: " + e.getMessage());
        }
    }
    
    /**
     * Route message to appropriate handler
     */
    private void routeMessage(Message message) {
        switch (message.getType()) {
            // Authentication responses
            case SUBSCRIBER_LOGIN_RESPONSE:
                authService.handleLoginResponse(message);
                break;
                
            case MANAGER_LOGIN_RESPONSE:
                authService.handleManagerLoginResponse(message);
                break;
                
            case REGISTRATION_RESPONSE:
                authService.handleRegistrationResponse(message);
                break;
                
            // Parking responses
            case PARKING_AVAILABILITY_RESPONSE:
                parkingService.handleAvailabilityResponse(message);
                break;
                
            case ENTER_PARKING_RESPONSE:
                parkingService.handleEnterResponse(message);
                break;
                
            case EXIT_PARKING_RESPONSE:
                parkingService.handleExitResponse(message);
                break;
                
            case EXTEND_PARKING_RESPONSE:
                parkingService.handleExtendResponse(message);
                break;
                
            case PARKING_HISTORY_RESPONSE:
                parkingService.handleHistoryResponse(message);
                break;
                
            // Reservation responses
            case RESERVATION_RESPONSE:
                reservationService.handleReservationResponse(message);
                break;
                
            case CANCELLATION_RESPONSE:
                reservationService.handleCancellationResponse(message);
                break;
                
            case ACTIVATION_RESPONSE:
                reservationService.handleActivationResponse(message);
                break;
                
            // Error response
            case ERROR:
                messageHandler.handleErrorMessage(message);
                break;
                
            default:
                logger.logError("Unhandled message type: " + message.getType());
        }
    }
    
    /**
     * Handle legacy string messages
     */
    private void handleStringMessage(String message) {
        String[] parts = message.split("\\s", 2);
        
        switch (parts[0]) {
            case "availableSpots":
                if (parts.length > 1) {
                    System.out.println("Available parking spots: " + parts[1]);
                }
                break;
                
            case "login:":
                if (parts.length > 1) {
                    authService.handleStringLoginResponse(parts[1]);
                }
                break;
                
            default:
                logger.logDebug("Unhandled string message: " + message);
        }
    }
    
    /**
     * Prepare message for sending
     */
    public Object prepareMessage(Message message) throws IOException {
        return serializeMessage(message);
    }
    
    /**
     * Send disconnect message
     */
    public void sendDisconnectMessage() {
        client.sendStringMessage("ClientDisconnect");
    }
    
    /**
     * Serialize message
     */
    private byte[] serializeMessage(Object obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            
            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        }
    }
    
    /**
     * Deserialize message
     */
    private Object deserializeMessage(Object data) throws IOException, ClassNotFoundException {
        if (data instanceof byte[]) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) data);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                
                return ois.readObject();
            }
        }
        return data;
    }
    
    // Service getters
    public AuthenticationService getAuthService() { return authService; }
    public ParkingService getParkingService() { return parkingService; }
    public ReservationService getReservationService() { return reservationService; }
}