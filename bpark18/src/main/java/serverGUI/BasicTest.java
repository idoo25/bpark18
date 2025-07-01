package serverGUI;

import server.ParkingServer;

/**
 * Simple test to verify the basic functionality of our classes
 */
public class BasicTest {
    public static void main(String[] args) {
        System.out.println("Testing basic functionality...");
        
        // Test ParkingServer
        ParkingServer server = new ParkingServer();
        System.out.println("✓ ParkingServer created successfully");
        System.out.println("Server IP: " + server.getServerIP());
        System.out.println("Server running: " + server.isRunning());
        System.out.println("Client count: " + server.getClientCount());
        
        // Test that our controller class exists
        try {
            ServerPortFrame controller = new ServerPortFrame();
            System.out.println("✓ ServerPortFrame created successfully");
        } catch (Exception e) {
            System.out.println("✗ Error creating ServerPortFrame: " + e.getMessage());
        }
        
        System.out.println("Basic tests completed!");
    }
}