package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic parking server implementation
 */
public class ParkingServer {
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private List<Socket> clientConnections;
    private String serverIP;
    
    public ParkingServer() {
        this.clientConnections = new ArrayList<>();
        this.serverIP = "localhost";
    }
    
    public void startServer(int port) throws IOException {
        if (!isRunning) {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("Server started on port " + port);
        }
    }
    
    public void stopServer() throws IOException {
        if (isRunning && serverSocket != null) {
            isRunning = false;
            serverSocket.close();
            // Close all client connections
            for (Socket client : clientConnections) {
                if (!client.isClosed()) {
                    client.close();
                }
            }
            clientConnections.clear();
            System.out.println("Server stopped");
        }
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public int getClientCount() {
        return clientConnections.size();
    }
    
    public String getServerIP() {
        return serverIP;
    }
    
    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }
    
    public void addClientConnection(Socket client) {
        clientConnections.add(client);
    }
    
    public void removeClientConnection(Socket client) {
        clientConnections.remove(client);
    }
}