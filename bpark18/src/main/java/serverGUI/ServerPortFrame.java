package serverGUI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import server.ParkingServer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Server GUI
 */
public class ServerPortFrame implements Initializable {
    
    @FXML
    private TextField serverip;
    
    @FXML
    private TextArea textMessage;
    
    @FXML
    private Button btnExit;
    
    @FXML
    private TextField txtClientConnection;
    
    private ParkingServer parkingServer;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the parking server
        parkingServer = new ParkingServer();
        
        // Set default server IP
        serverip.setText("localhost");
        
        // Initialize client connection count
        txtClientConnection.setText("0");
        
        // Add initial message to text area
        appendMessage("Server GUI initialized. Ready to start server.");
        
        // Start the server automatically on default port
        try {
            parkingServer.startServer(8080);
            appendMessage("Server started on port 8080");
            updateClientCount();
        } catch (IOException e) {
            appendMessage("Failed to start server: " + e.getMessage());
        }
    }
    
    /**
     * Handles the exit button click
     */
    @FXML
    private void handleExit() {
        try {
            if (parkingServer.isRunning()) {
                parkingServer.stopServer();
                appendMessage("Server stopped.");
            }
        } catch (IOException e) {
            appendMessage("Error stopping server: " + e.getMessage());
        }
        
        // Close the application
        Platform.exit();
        System.exit(0);
    }
    
    /**
     * Appends a message to the text area
     */
    private void appendMessage(String message) {
        Platform.runLater(() -> {
            textMessage.appendText(java.time.LocalDateTime.now().toString() + ": " + message + "\n");
        });
    }
    
    /**
     * Updates the client connection count display
     */
    private void updateClientCount() {
        Platform.runLater(() -> {
            txtClientConnection.setText(String.valueOf(parkingServer.getClientCount()));
        });
    }
    
    /**
     * Gets the ParkingServer instance
     */
    public ParkingServer getParkingServer() {
        return parkingServer;
    }
}