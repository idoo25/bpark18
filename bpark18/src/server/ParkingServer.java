package server.core;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import server.config.ServerConfig;
import server.utils.ServerLogger;
import java.io.IOException;

/**
 * Singleton server implementation extending OCSF AbstractServer.
 * Single Responsibility: Manages server lifecycle and client connections.
 */
public class ParkingServer extends AbstractServer {
    
    private static ParkingServer instance = null;
    private final ClientHandler clientHandler;
    private final ServerLogger logger;
    
    /**
     * Private constructor for singleton pattern
     */
    private ParkingServer() {
        super(ServerConfig.SERVER_PORT);
        this.clientHandler = new ClientHandler();
        this.logger = ServerLogger.getInstance();
        
        // Configure server settings
        setTimeout(ServerConfig.CONNECTION_TIMEOUT);
        setBacklog(ServerConfig.MAX_CLIENTS);
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ParkingServer getInstance() {
        if (instance == null) {
            instance = new ParkingServer();
        }
        return instance;
    }
    
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        try {
            logger.log("Message received from client " + client.getId());
            clientHandler.handleMessage(msg, client);
        } catch (Exception e) {
            logger.logError("Error handling message from client " + client.getId() + ": " + e.getMessage());
        }
    }
    
    @Override
    protected void clientConnected(ConnectionToClient client) {
        logger.log("Client connected: " + client);
        clientHandler.onClientConnected(client);
    }
    
    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        logger.log("Client disconnected: " + client);
        clientHandler.onClientDisconnected(client);
    }
    
    @Override
    protected void clientException(ConnectionToClient client, Throwable exception) {
        logger.logError("Client " + client + " exception: " + exception.getMessage());
        clientHandler.onClientException(client, exception);
    }
    
    @Override
    protected void serverStarted() {
        logger.log("Server started on port " + getPort());
    }
    
    @Override
    protected void serverStopped() {
        logger.log("Server stopped");
    }
    
    @Override
    protected void serverClosed() {
        logger.log("Server closed");
    }
    
    /**
     * Broadcast message to all connected clients
     */
    public void broadcast(Object msg) {
        try {
            sendToAllClients(msg);
        } catch (Exception e) {
            logger.logError("Error broadcasting message: " + e.getMessage());
        }
    }
    
    /**
     * Send message to specific client
     */
    public void sendToClient(ConnectionToClient client, Object msg) {
        try {
            client.sendToClient(msg);
        } catch (IOException e) {
            logger.logError("Error sending message to client " + client.getId() + ": " + e.getMessage());
        }
    }
}