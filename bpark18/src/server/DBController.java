package server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import server.utils.ServerLogger;

/**
 * Singleton database controller managing database connections.
 * Single Responsibility: Manages database connection lifecycle and provides connection access.
 */
public class DBController {
    
    private static DBController instance = null;
    private Connection connection;
    private int successFlag;
    private final ServerLogger logger;
    
    /**
     * Private constructor for singleton pattern
     */
    private DBController(String dbName, String password) {
        this.logger = ServerLogger.getInstance();
        initConnection(dbName, password);
    }
    
    /**
     * Initialize database connection
     */
    private void initConnection(String dbName, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/" + dbName + "?serverTimezone=IST&useSSL=false";
            
            this.connection = DriverManager.getConnection(url, "root", password);
            this.connection.setAutoCommit(false); // Use transactions
            this.successFlag = 1;
            
            logger.log("✅ Database connection established successfully");
        } catch (Exception e) {
            this.successFlag = 0;
            logger.logError("❌ Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize singleton instance
     */
    public static synchronized void initializeConnection(String dbName, String password) {
        if (instance == null) {
            instance = new DBController(dbName, password);
        }
    }
    
    /**
     * Get singleton instance
     */
    public static DBController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DBController not initialized. Call initializeConnection() first.");
        }
        return instance;
    }
    
    /**
     * Get database connection
     */
    public Connection getConnection() {
        try {
            // Check if connection is still valid
            if (connection == null || connection.isClosed()) {
                logger.logError("Database connection lost. Attempting to reconnect...");
                // You might want to implement reconnection logic here
                throw new SQLException("Database connection is closed");
            }
        } catch (SQLException e) {
            logger.logError("Error checking connection status: " + e.getMessage());
        }
        return connection;
    }
    
    /**
     * Execute query with prepared statement
     */
    public ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(query);
            setParameters(stmt, params);
            return stmt.executeQuery();
        } catch (SQLException e) {
            logger.logError("Error executing query: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Execute update with prepared statement
     */
    public int executeUpdate(String query, Object... params) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(query);
            setParameters(stmt, params);
            int result = stmt.executeUpdate();
            connection.commit();
            return result;
        } catch (SQLException e) {
            connection.rollback();
            logger.logError("Error executing update: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }
    
    /**
     * Begin transaction
     */
    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }
    
    /**
     * Commit transaction
     */
    public void commitTransaction() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }
    
    /**
     * Rollback transaction
     */
    public void rollbackTransaction() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            logger.logError("Error during rollback: " + e.getMessage());
        }
    }
    
    /**
     * Set parameters for prepared statement
     */
    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
    
    /**
     * Get success flag
     */
    public int getSuccessFlag() {
        return successFlag;
    }
    
    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.log("Database connection closed");
            }
        } catch (SQLException e) {
            logger.logError("Error closing database connection: " + e.getMessage());
        }
    }
}