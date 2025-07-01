package client.config;

/**
 * Client configuration constants.
 * Single Responsibility: Holds all client configuration in one place.
 */
public class ClientConfig {
    // Server connection settings
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 5555;
    public static final int CONNECTION_TIMEOUT = 5000; // 5 seconds
    public static final int RECONNECT_ATTEMPTS = 3;
    
    // Client settings
    public static final boolean AUTO_RECONNECT = true;
    public static final int HEARTBEAT_INTERVAL = 30000; // 30 seconds
    
    // UI settings
    public static final int MAX_INPUT_LENGTH = 255;
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    
    // Logging settings
    public static final boolean ENABLE_DEBUG_LOGGING = true;
    public static final String LOG_FILE_PATH = "logs/client.log";
    
    private ClientConfig() {
        // Private constructor to prevent instantiation
    }
}