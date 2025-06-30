package server;

import server.config.ServerConfig;
import server.core.ParkingServer;
import server.database.DBController;
import server.utils.ServerLogger;

/**
 * Main entry point for the server application.
 * Follows Single Responsibility: Only responsible for server initialization and startup.
 */
public class ServerApp {
    
    public static void main(String[] args) {
        try {
            // Initialize logger
            ServerLogger.getInstance().log("Starting Parking Server Application...");
            
            // Initialize database connection
            DBController.initializeConnection(
                ServerConfig.DB_NAME, 
                ServerConfig.DB_PASSWORD
            );
            
            // Check database connection
            if (DBController.getInstance().getSuccessFlag() != 1) {
                ServerLogger.getInstance().logError("Failed to connect to database. Exiting...");
                System.exit(1);
            }
            
            // Get server instance and start
            ParkingServer server = ParkingServer.getInstance();
            server.listen();
            
            ServerLogger.getInstance().log("Server started successfully on port " + ServerConfig.SERVER_PORT);
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                ServerLogger.getInstance().log("Shutting down server...");
                server.stopListening();
                server.close();
            }));
            
        } catch (Exception e) {
            ServerLogger.getInstance().logError("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}