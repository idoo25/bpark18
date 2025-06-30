package server.utils;

import server.config.ServerConfig;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton logger for server-side logging.
 * Single Responsibility: Handles all server logging operations.
 */
public class ServerLogger {
    
    private static ServerLogger instance = null;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private PrintWriter fileWriter;
    
    private ServerLogger() {
        initializeFileWriter();
    }
    
    public static synchronized ServerLogger getInstance() {
        if (instance == null) {
            instance = new ServerLogger();
        }
        return instance;
    }
    
    /**
     * Initialize file writer for log file
     */
    private void initializeFileWriter() {
        try {
            fileWriter = new PrintWriter(new FileWriter(ServerConfig.LOG_FILE_PATH, true));
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
        
        System.out.println(logEntry);
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
        if (ServerConfig.ENABLE_DEBUG_LOGGING) {
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