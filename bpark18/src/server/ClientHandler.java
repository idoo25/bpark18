package server.core;

import ocsf.server.ConnectionToClient;
import entities.Message;
import server.utils.MessageSerializer;
import server.utils.ServerLogger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles individual client connections and message routing.
 * Single Responsibility: Manages client sessions and routes messages to appropriate processor.
 */
public class ClientHandler {
    
    private final MessageProcessor messageProcessor;
    private final ConcurrentHashMap<Long, ClientSession> activeSessions;
    private final ServerLogger logger;
    
    public ClientHandler() {
        this.messageProcessor = new MessageProcessor();
        this.activeSessions = new ConcurrentHashMap<>();
        this.logger = ServerLogger.getInstance();
    }
    
    /**
     * Handle incoming message from client
     */
    public void handleMessage(Object msg, ConnectionToClient client) {
        try {
            // Deserialize if needed
            if (msg instanceof byte[]) {
                msg = MessageSerializer.deserialize(msg);
            }
            
            // Update client activity
            updateClientActivity(client);
            
            // Process message
            if (msg instanceof Message) {
                Message message = (Message) msg;
                logger.log("Processing message type: " + message.getType() + " from client " + client.getId());
                messageProcessor.processMessage(message, client);
            } else if (msg instanceof String) {
                // Handle legacy string messages
                messageProcessor.processStringMessage((String) msg, client);
            } else {
                logger.logError("Unknown message type from client " + client.getId());
            }
            
        } catch (Exception e) {
            logger.logError("Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle client connection
     */
    public void onClientConnected(ConnectionToClient client) {
        ClientSession session = new ClientSession(client.getId());
        activeSessions.put(client.getId(), session);
        logger.log("New client session created: " + client.getId());
    }
    
    /**
     * Handle client disconnection
     */
    public void onClientDisconnected(ConnectionToClient client) {
        ClientSession session = activeSessions.remove(client.getId());
        if (session != null) {
            messageProcessor.handleClientLogout(session, client);
        }
        logger.log("Client session removed: " + client.getId());
    }
    
    /**
     * Handle client exception
     */
    public void onClientException(ConnectionToClient client, Throwable exception) {
        logger.logError("Client " + client.getId() + " exception: " + exception.getMessage());
        try {
            client.close();
        } catch (Exception e) {
            logger.logError("Error closing client connection: " + e.getMessage());
        }
    }
    
    /**
     * Update client last activity time
     */
    private void updateClientActivity(ConnectionToClient client) {
        ClientSession session = activeSessions.get(client.getId());
        if (session != null) {
            session.updateLastActivity();
        }
    }
    
    /**
     * Get active session for client
     */
    public ClientSession getSession(ConnectionToClient client) {
        return activeSessions.get(client.getId());
    }
    
    /**
     * Inner class to track client sessions
     */
    public static class ClientSession {
        private final long clientId;
        private long lastActivity;
        private String username;
        private String userRole;
        
        public ClientSession(long clientId) {
            this.clientId = clientId;
            this.lastActivity = System.currentTimeMillis();
        }
        
        public void updateLastActivity() {
            this.lastActivity = System.currentTimeMillis();
        }
        
        // Getters and setters
        public long getClientId() { return clientId; }
        public long getLastActivity() { return lastActivity; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getUserRole() { return userRole; }
        public void setUserRole(String userRole) { this.userRole = userRole; }
    }
}