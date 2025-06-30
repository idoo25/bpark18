package client.core;

import ocsf.client.AbstractClient;
import client.config.ClientConfig;
import client.utils.ClientLogger;
import entities.Message;
import java.io.IOException;

/**
 * Singleton client implementation extending OCSF AbstractClient.
 * Single Responsibility: Manages client connection and message handling.
 */
public class ParkingClient extends AbstractClient {
    
    private static ParkingClient instance = null;
    private final ServerCommunicator communicator;
    private final ClientLogger logger;
    private boolean isConnected = false;
    
    /**
     * Private constructor for singleton pattern
     */
    private ParkingClient() {
        super(ClientConfig.SERVER_HOST, ClientConfig.SERVER_PORT);
        this.communicator = new ServerCommunicator(this);
        this.logger = ClientLogger.getInstance();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ParkingClient getInstance() {
        if (instance == null) {
            instance = new ParkingClient();
        }
        return instance;
    }
    
    /**
     * Connect to server
     */
    public void connect() throws IOException {
        if (!isConnected()) {
            openConnection();
            isConnected = true;
            logger.log("Connected to server successfully");
        }
    }
    
    /**
     * Disconnect from server
     */
    public void disconnect() {
        try {
            if (isConnected()) {
                // Send disconnect message
                communicator.sendDisconnectMessage();
                closeConnection();
                isConnected = false;
                logger.log("Disconnected from server");
            }
        } catch (IOException e) {
            logger.logError("Error during disconnect: " + e.getMessage());
        }
    }
    
    /**
     * Send message to server
     */
    public void sendMessage(Message message) {
        try {
            sendToServer(communicator.prepareMessage(message));
        } catch (IOException e) {
            logger.logError("Failed to send message: " + e.getMessage());
            handleConnectionError();
        }
    }
    
    /**
     * Send string message to server (for legacy support)
     */
    public void sendStringMessage(String message) {
        try {
            sendToServer(message);
        } catch (IOException e) {
            logger.logError("Failed to send string message: " + e.getMessage());
            handleConnectionError();
        }
    }
    
    @Override
    protected void handleMessageFromServer(Object msg) {
        logger.logDebug("Message received from server");
        communicator.handleServerMessage(msg);
    }
    
    @Override
    protected void connectionClosed() {
        isConnected = false;
        logger.log("Connection to server closed");
        
        if (ClientConfig.AUTO_RECONNECT) {
            attemptReconnect();
        }
    }
    
    @Override
    protected void connectionException(Exception exception) {
        logger.logError("Connection exception: " + exception.getMessage());
        handleConnectionError();
    }
    
    @Override
    protected void connectionEstablished() {
        isConnected = true;
        logger.log("Connection established with server");
    }
    
    /**
     * Handle connection errors
     */
    private void handleConnectionError() {
        if (ClientConfig.AUTO_RECONNECT && !isConnected) {
            attemptReconnect();
        }
    }
    
    /**
     * Attempt to reconnect to server
     */
    private void attemptReconnect() {
        logger.log("Attempting to reconnect...");
        
        for (int i = 1; i <= ClientConfig.RECONNECT_ATTEMPTS; i++) {
            try {
                Thread.sleep(2000 * i); // Exponential backoff
                connect();
                logger.log("Reconnection successful");
                return;
            } catch (Exception e) {
                logger.logError("Reconnection attempt " + i + " failed: " + e.getMessage());
            }
        }
        
        logger.logError("Failed to reconnect after " + ClientConfig.RECONNECT_ATTEMPTS + " attempts");
    }
    
    /**
     * Get communicator instance
     */
    public ServerCommunicator getCommunicator() {
        return communicator;
    }
    
    /**
     * Check if client is connected
     */
    @Override
    public boolean isConnected() {
        return super.isConnected() && isConnected;
    }
}