package server;

/**
 * Centralized SQL query management.
 * Single Responsibility: Maintains all SQL queries in one place for easy management.
 */
public class DatabaseQueries {
    
    // User queries
    public static final String SELECT_USER_BY_USERNAME = 
        "SELECT * FROM users WHERE username = ?";
    
    public static final String SELECT_USER_BY_ID = 
        "SELECT * FROM users WHERE user_id = ?";
    
    public static final String INSERT_USER = 
        "INSERT INTO users (username, user_code, name, phone, email, car_number, role, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
    
    public static final String UPDATE_USER = 
        "UPDATE users SET name = ?, phone = ?, email = ?, car_number = ? WHERE username = ?";
    
    public static final String UPDATE_USER_LOGIN_STATUS = 
        "UPDATE users SET is_logged_in = ?, last_login = NOW() WHERE username = ?";
    
    // Parking queries
    public static final String SELECT_AVAILABLE_SPOTS = 
        "SELECT COUNT(*) FROM parking_spots WHERE is_occupied = false";
    
    public static final String SELECT_ACTIVE_PARKING_BY_USER = 
        "SELECT * FROM parking_orders WHERE username = ? AND exit_time IS NULL";
    
    public static final String INSERT_PARKING_ORDER = 
        "INSERT INTO parking_orders (username, parking_code, entry_time, expected_exit_time, spot_id) " +
        "VALUES (?, ?, NOW(), ?, ?)";
    
    public static final String UPDATE_PARKING_EXIT = 
        "UPDATE parking_orders SET exit_time = NOW(), total_cost = ? WHERE parking_code = ?";
    
    public static final String UPDATE_PARKING_SPOT_STATUS = 
        "UPDATE parking_spots SET is_occupied = ? WHERE spot_id = ?";
    
    public static final String EXTEND_PARKING_TIME = 
        "UPDATE parking_orders SET expected_exit_time = DATE_ADD(expected_exit_time, INTERVAL ? HOUR) " +
        "WHERE parking_code = ? AND exit_time IS NULL";
    
    // Reservation queries
    public static final String SELECT_RESERVATION_BY_CODE = 
        "SELECT * FROM reservations WHERE reservation_code = ? AND status = 'ACTIVE'";
    
    public static final String INSERT_RESERVATION = 
        "INSERT INTO reservations (username, reservation_code, reservation_date, created_at, status) " +
        "VALUES (?, ?, ?, NOW(), 'ACTIVE')";
    
    public static final String UPDATE_RESERVATION_STATUS = 
        "UPDATE reservations SET status = ? WHERE reservation_code = ?";
    
    public static final String SELECT_ACTIVE_RESERVATIONS_BY_USER = 
        "SELECT * FROM reservations WHERE username = ? AND status = 'ACTIVE' " +
        "AND reservation_date > NOW()";
    
    // Report queries
    public static final String SELECT_PARKING_HISTORY = 
        "SELECT * FROM parking_orders WHERE username = ? ORDER BY entry_time DESC LIMIT ?";
    
    public static final String SELECT_DAILY_REVENUE = 
        "SELECT DATE(exit_time) as date, SUM(total_cost) as revenue " +
        "FROM parking_orders WHERE exit_time BETWEEN ? AND ? " +
        "GROUP BY DATE(exit_time)";
    
    public static final String SELECT_OCCUPANCY_STATS = 
        "SELECT COUNT(*) as occupied FROM parking_spots WHERE is_occupied = true";
    
    public static final String SELECT_PARKING_VIOLATIONS = 
        "SELECT * FROM parking_orders WHERE exit_time IS NULL " +
        "AND expected_exit_time < NOW()";
    
    // Utility queries
    public static final String CHECK_USERNAME_EXISTS = 
        "SELECT COUNT(*) FROM users WHERE username = ?";
    
    public static final String GENERATE_UNIQUE_CODE = 
        "SELECT MAX(?) + 1 FROM ?";
    
    private DatabaseQueries() {
        // Private constructor to prevent instantiation
    }
}
