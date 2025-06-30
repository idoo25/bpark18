package entities;

/**
 * Enumeration of all message types in the system.
 * Single Responsibility: Defines all possible message types for communication.
 */
public enum MessageType {
    // Authentication messages
    SUBSCRIBER_LOGIN,
    SUBSCRIBER_LOGIN_RESPONSE,
    MANAGER_LOGIN,
    MANAGER_LOGIN_RESPONSE,
    REGISTER_SUBSCRIBER,
    REGISTRATION_RESPONSE,
    GENERATE_USERNAME,
    USERNAME_RESPONSE,
    
    // Parking operations
    CHECK_PARKING_AVAILABILITY,
    PARKING_AVAILABILITY_RESPONSE,
    ENTER_PARKING,
    ENTER_PARKING_RESPONSE,
    EXIT_PARKING,
    EXIT_PARKING_RESPONSE,
    EXTEND_PARKING,
    EXTEND_PARKING_RESPONSE,
    GET_ACTIVE_PARKINGS,
    ACTIVE_PARKINGS_RESPONSE,
    
    // Reservation operations
    RESERVE_PARKING,
    RESERVATION_RESPONSE,
    CANCEL_RESERVATION,
    CANCELLATION_RESPONSE,
    ACTIVATE_RESERVATION,
    ACTIVATION_RESPONSE,
    GET_TIME_SLOTS,
    TIME_SLOTS_RESPONSE,
    
    // User operations
    REQUEST_LOST_CODE,
    LOST_CODE_RESPONSE,
    UPDATE_SUBSCRIBER_INFO,
    UPDATE_SUBSCRIBER_RESPONSE,
    REQUEST_SUBSCRIBER_DATA,
    SUBSCRIBER_DATA_RESPONSE,
    REQUEST_EXTENSION,
    EXTENSION_RESPONSE,
    
    // Report operations
    GET_PARKING_HISTORY,
    PARKING_HISTORY_RESPONSE,
    MANAGER_GET_REPORTS,
    MANAGER_SEND_REPORTS,
    GENERATE_MONTHLY_REPORTS,
    MONTHLY_REPORTS_RESPONSE,
    
    // Admin operations
    GET_ALL_SUBSCRIBERS,
    SHOW_ALL_SUBSCRIBERS,
    GET_SUBSCRIBER_BY_NAME,
    SHOW_SUBSCRIBER_DETAILS,
    
    // System messages
    ERROR,
    SUCCESS,
    HEARTBEAT,
    DISCONNECT,
    CLIENT_CONNECTED,
    CLIENT_DISCONNECTED,
    CLIENT_EXCEPTION,
    SERVER_STARTED,
    SERVER_STOPPED,
    SERVER_CLOSED,
    LISTENING_EXCEPTION;
    
    /**
     * Check if this is a request message type
     */
    public boolean isRequest() {
        return !name().endsWith("_RESPONSE") && 
               !name().endsWith("_EXCEPTION") &&
               !name().startsWith("CLIENT_") &&
               !name().startsWith("SERVER_") &&
               this != ERROR && 
               this != SUCCESS && 
               this != HEARTBEAT && 
               this != DISCONNECT;
    }
    
    /**
     * Check if this is a response message type
     */
    public boolean isResponse() {
        return name().endsWith("_RESPONSE") || this == ERROR || this == SUCCESS;
    }
    
    /**
     * Check if this is a system message type
     */
    public boolean isSystemMessage() {
        return name().startsWith("CLIENT_") || 
               name().startsWith("SERVER_") || 
               name().endsWith("_EXCEPTION") ||
               this == HEARTBEAT || 
               this == DISCONNECT;
    }
    
    /**
     * Get user-friendly display name
     */
    public String getDisplayName() {
        return name().replace("_", " ").toLowerCase()
                    .replaceFirst("^.", String.valueOf(Character.toUpperCase(name().charAt(0))));
    }
}