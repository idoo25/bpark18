package server.core;

import entities.Message;
import entities.MessageType;
import ocsf.server.ConnectionToClient;
import server.ParkingServer;
import server.business.ParkingManager;
import server.business.ReportManager;
import server.business.ReservationManager;
import server.business.UserManager;
import server.utils.MessageSerializer;
import server.utils.ServerLogger;

/**
 * Processes different types of messages from clients.
 * Single Responsibility: Routes messages to appropriate business logic managers.
 */
public class MessageProcessor {
    
    private final UserManager userManager;
    private final ParkingManager parkingManager;
    private final ReservationManager reservationManager;
    private final ReportManager reportManager;
    private final ServerLogger logger;
    
    public MessageProcessor() {
        this.userManager = UserManager.getInstance();
        this.parkingManager = ParkingManager.getInstance();
        this.reservationManager = ReservationManager.getInstance();
        this.reportManager = ReportManager.getInstance();
        this.logger = ServerLogger.getInstance();
    }
    
    /**
     * Process Message objects
     */
    public void processMessage(Message message, ConnectionToClient client) {
        try {
            Message response = null;
            
            switch (message.getType()) {
                // Authentication messages
                case SUBSCRIBER_LOGIN:
                    response = userManager.handleLogin(message, client);
                    break;
                    
                case MANAGER_LOGIN:
                    response = userManager.handleManagerLogin(message, client);
                    break;
                    
                case REGISTER_SUBSCRIBER:
                    response = userManager.handleRegistration(message, client);
                    break;
                    
                // Parking operations
                case CHECK_PARKING_AVAILABILITY:
                    response = parkingManager.checkAvailability(message);
                    break;
                    
                case ENTER_PARKING:
                    response = parkingManager.handleEnterParking(message);
                    break;
                    
                case EXIT_PARKING:
                    response = parkingManager.handleExitParking(message);
                    break;
                    
                case EXTEND_PARKING:
                    response = parkingManager.handleExtendParking(message);
                    break;
                    
                // Reservation operations
                case RESERVE_PARKING:
                    response = reservationManager.handleReservation(message);
                    break;
                    
                case CANCEL_RESERVATION:
                    response = reservationManager.handleCancellation(message);
                    break;
                    
                case ACTIVATE_RESERVATION:
                    response = reservationManager.handleActivation(message);
                    break;
                    
                // Report operations
                case MANAGER_GET_REPORTS:
                    response = reportManager.generateReports(message);
                    break;
                    
                case GET_PARKING_HISTORY:
                    response = reportManager.getParkingHistory(message);
                    break;
                    
                // User information
                case REQUEST_LOST_CODE:
                    response = userManager.handleLostCode(message);
                    break;
                    
                case UPDATE_SUBSCRIBER_INFO:
                    response = userManager.updateSubscriberInfo(message);
                    break;
                    
                default:
                    logger.logError("Unknown message type: " + message.getType());
                    response = new Message(MessageType.ERROR, "Unknown request type");
            }
            
            // Send response if available
            if (response != null) {
                sendResponse(client, response);
            }
            
        } catch (Exception e) {
            logger.logError("Error processing message: " + e.getMessage());
            sendErrorResponse(client, "Internal server error");
        }
    }
    
    /**
     * Process legacy string messages
     */
    public void processStringMessage(String message, ConnectionToClient client) {
        try {
            String[] parts = message.split("\\s", 2);
            String command = parts[0];
            
            switch (command) {
                case "ClientDisconnect":
//                     handleClientLogout(ParkingServer.getInstance().getClientHandler().getSession(client), client); // TODO: Fix this method call
                    break;
                    
                case "getParkingSpots":
                    int available = parkingManager.getAvailableSpots();
                    client.sendToClient("availableSpots " + available);
                    break;
                    
                default:
                    logger.log("Unknown string command: " + command);
            }
            
        } catch (Exception e) {
            logger.logError("Error processing string message: " + e.getMessage());
        }
    }
    
    /**
     * Handle client logout
     */
    public void handleClientLogout(ClientHandler.ClientSession session, ConnectionToClient client) {
        if (session != null && session.getUsername() != null) {
            userManager.logoutUser(session.getUsername());
            logger.log("User " + session.getUsername() + " logged out");
        }
    }
    
    /**
     * Send response to client
     */
    private void sendResponse(ConnectionToClient client, Message response) {
        try {
            byte[] serialized = MessageSerializer.serialize(response);
            client.sendToClient(serialized);
        } catch (Exception e) {
            logger.logError("Error sending response: " + e.getMessage());
        }
    }
    
    /**
     * Send error response
     */
    private void sendErrorResponse(ConnectionToClient client, String error) {
        Message errorMsg = new Message(MessageType.ERROR, error);
        sendResponse(client, errorMsg);
    }
}