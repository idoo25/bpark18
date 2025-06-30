package client;

import client.config.ClientConfig;
import client.core.ParkingClient;
import client.utils.ClientLogger;
import java.util.Scanner;

/**
 * Main entry point for the client application.
 * Single Responsibility: Initializes and starts the client application.
 */
public class ClientApp {
    
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        ClientLogger logger = ClientLogger.getInstance();
        
        try {
            logger.log("Starting Parking Client Application...");
            
            // Initialize client
            ParkingClient client = ParkingClient.getInstance();
            
            // Connect to server
            logger.log("Connecting to server at " + ClientConfig.SERVER_HOST + ":" + ClientConfig.SERVER_PORT);
            client.connect();
            
            // Show main menu
            showMainMenu();
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.log("Shutting down client...");
                client.disconnect();
                scanner.close();
            }));
            
        } catch (Exception e) {
            logger.logError("Failed to start client: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Show main menu (simplified for example)
     */
    private static void showMainMenu() {
        System.out.println("\n=== Parking System Client ===");
        System.out.println("1. Login as Subscriber");
        System.out.println("2. Login as Manager/Attendant");
        System.out.println("3. Register New Subscriber");
        System.out.println("4. Exit");
        System.out.print("Select option: ");
        
        // Menu handling would be implemented here
        // This is just a placeholder to show structure
    }
}