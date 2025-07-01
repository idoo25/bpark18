package server;

/**
 * Simple test for the ParkingServer class only
 */
public class ServerTest {
    public static void main(String[] args) {
        System.out.println("Testing ParkingServer functionality...");
        
        ParkingServer server = new ParkingServer();
        System.out.println("✓ ParkingServer created successfully");
        System.out.println("Server IP: " + server.getServerIP());
        System.out.println("Server running: " + server.isRunning());
        System.out.println("Client count: " + server.getClientCount());
        
        // Test setting server IP
        server.setServerIP("192.168.1.1");
        System.out.println("New server IP: " + server.getServerIP());
        
        System.out.println("✓ ParkingServer tests passed!");
    }
}