package serverGUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX Application class for the Parking Server GUI
 */
public class ServerApp extends Application {
    
    private ServerPortFrame controller;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/serverGUI/ServerGUI.fxml"));
            Parent root = loader.load();
            
            // Get the controller instance
            controller = loader.getController();
            
            // Create the scene
            Scene scene = new Scene(root, 600, 400);
            
            // Configure the stage
            primaryStage.setTitle("Parking Server Management");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            
            // Handle window close event
            primaryStage.setOnCloseRequest(event -> {
                try {
                    if (controller != null && controller.getParkingServer().isRunning()) {
                        controller.getParkingServer().stopServer();
                    }
                } catch (Exception e) {
                    System.err.println("Error stopping server on exit: " + e.getMessage());
                }
                System.exit(0);
            });
            
            // Show the stage
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Main method to launch the JavaFX application
     */
    public static void main(String[] args) {
        launch(args);
    }
}