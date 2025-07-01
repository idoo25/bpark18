package client.utils;

import client.config.ClientConfig;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton logger for client-side logging.
 * Single Responsibility: Handles all client logging operations.
 */
public class ClientLogger {
    
    private static ClientLogger instance = null;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private PrintWriter fileWriter;
    
    private ClientLogger() {
        initializeFileWriter();
    }
    
    public static synchronized ClientLogger getInstance() {
        if (instance == null) {
            instance = new ClientLogger();
        }
        return instance;
    }
    
    /**
     * Initialize file writer for log file
     */
    private void initializeFileWriter() {
        try {
            // Create logs directory if it doesn't exist
            new java.io.File("logs").mkdirs();
            fileWriter = new PrintWriter(new FileWriter(ClientConfig.LOG_FILE_PATH, true));
        } catch (IOException e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
        }
    }
    
    /**
     * Log general message
     */
    public void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] INFO: %s", timestamp, message);
        
        if (ClientConfig.ENABLE_DEBUG_LOGGING) {
            System.out.println(logEntry);
        }
        writeToFile(logEntry);
    }
    
    /**
     * Log error message
     */
    public void logError(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] ERROR: %s", timestamp, message);
        
        System.err.println(logEntry);
        writeToFile(logEntry);
    }
    
    /**
     * Log debug message
     */
    public void logDebug(String message) {
        if (ClientConfig.ENABLE_DEBUG_LOGGING) {
            String timestamp = LocalDateTime.now().format(formatter);
            String logEntry = String.format("[%s] DEBUG: %s", timestamp, message);
            
            System.out.println(logEntry);
            writeToFile(logEntry);
        }
    }
    
    /**
     * Write to log file
     */
    private synchronized void writeToFile(String logEntry) {
        if (fileWriter != null) {
            fileWriter.println(logEntry);
            fileWriter.flush();
        }
    }
    
    /**
     * Close logger resources
     */
    public void close() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }
}