package server.config;

/**
 * Server configuration constants.
 * Single Responsibility: Holds all server configuration in one place.
 */
public class ServerConfig {
    // Server settings
    public static final int SERVER_PORT = 5555;
    public static final int MAX_CLIENTS = 100;
    public static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
    
    // Database settings
    public static final String DB_NAME = "parking_system";
    public static final String DB_PASSWORD = "Aa123456";
    
    // Parking system settings
    public static final int TOTAL_PARKING_SPOTS = 100;
    public static final double HOURLY_RATE = 10.0;
    public static final double PENALTY_RATE = 1.5;
    public static final int MAX_PARKING_HOURS = 24;
    
    // Logging settings
    public static final boolean ENABLE_DEBUG_LOGGING = true;
    public static final String LOG_FILE_PATH = "logs/server.log";
    
    private ServerConfig() {
        // Private constructor to prevent instantiation
    }
}