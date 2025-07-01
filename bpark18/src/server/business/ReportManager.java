package server.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entities.Message;
import entities.MessageType;
import entities.ParkingOrder;
import server.DBController;
import server.DatabaseQueries;
import server.utils.ServerLogger;

/**
 * Manages report generation and analytics.
 * Single Responsibility: Handles all reporting and analytics logic.
 */
public class ReportManager {
    
    private static ReportManager instance = null;
    private final DBController db;
    private final ServerLogger logger;
    
    private ReportManager() {
        this.db = DBController.getInstance();
        this.logger = ServerLogger.getInstance();
    }
    
    public static synchronized ReportManager getInstance() {
        if (instance == null) {
            instance = new ReportManager();
        }
        return instance;
    }
    
    /**
     * Generate reports for manager
     */
    public Message generateReports(Message request) {
        try {
            String reportType = (String) request.getContent();
            Map<String, Object> reports = new HashMap<>();
            
            switch (reportType) {
                case "DAILY":
                    reports.put("revenue", getDailyRevenue());
                    reports.put("occupancy", getOccupancyStats());
                    reports.put("violations", getParkingViolations());
                    break;
                    
                case "MONTHLY":
                    reports.put("monthlyRevenue", getMonthlyRevenue());
                    reports.put("averageOccupancy", getAverageOccupancy());
                    reports.put("topUsers", getTopUsers());
                    break;
                    
                case "SUMMARY":
                    reports.put("totalRevenue", getTotalRevenue());
                    reports.put("totalParkings", getTotalParkings());
                    reports.put("activeUsers", getActiveUsers());
                    break;
                    
                default:
                    return new Message(MessageType.MANAGER_SEND_REPORTS, 
                        "ERROR: Unknown report type");
            }
            
            logger.log("Generated " + reportType + " reports");
            return new Message(MessageType.MANAGER_SEND_REPORTS, (java.io.Serializable) reports);
            
        } catch (Exception e) {
            logger.logError("Report generation error: " + e.getMessage());
            return new Message(MessageType.MANAGER_SEND_REPORTS, "ERROR: Failed to generate reports");
        }
    }
    
    /**
     * Get parking history for user
     */
    public Message getParkingHistory(Message request) {
        try {
            String username = (String) request.getContent();
            List<ParkingOrder> history = new ArrayList<>();
            
            ResultSet rs = db.executeQuery(DatabaseQueries.SELECT_PARKING_HISTORY, username, 50);
            
            while (rs.next()) {
                ParkingOrder order = new ParkingOrder();
                order.setParkingCode(rs.getString("parking_code"));
                order.setUsername(username);
                order.setEntryTime(rs.getTimestamp("entry_time").toLocalDateTime());
                
                if (rs.getTimestamp("exit_time") != null) {
                    order.setExitTime(rs.getTimestamp("exit_time").toLocalDateTime());
                    order.setTotalCost(rs.getDouble("total_cost"));
                }
                
                history.add(order);
            }
            
            logger.log("Retrieved parking history for " + username);
            return new Message(MessageType.PARKING_HISTORY_RESPONSE, (java.io.Serializable) history);
            
        } catch (SQLException e) {
            logger.logError("Parking history error: " + e.getMessage());
            return new Message(MessageType.PARKING_HISTORY_RESPONSE, "ERROR: Failed to retrieve history");
        }
    }
    
    /**
     * Get daily revenue
     */
    private Map<String, Double> getDailyRevenue() throws SQLException {
        Map<String, Double> revenue = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        ResultSet rs = db.executeQuery(DatabaseQueries.SELECT_DAILY_REVENUE, 
            today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        
        while (rs.next()) {
            revenue.put(rs.getString("date"), rs.getDouble("revenue"));
        }
        
        return revenue;
    }
    
    /**
     * Get monthly revenue
     */
    private Map<String, Double> getMonthlyRevenue() throws SQLException {
        Map<String, Double> revenue = new HashMap<>();
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1);
        
        ResultSet rs = db.executeQuery(DatabaseQueries.SELECT_DAILY_REVENUE, 
            startOfMonth.atStartOfDay(), endOfMonth.atStartOfDay());
        
        double total = 0;
        while (rs.next()) {
            total += rs.getDouble("revenue");
        }
        
        revenue.put("total", total);
        revenue.put("average", total / startOfMonth.lengthOfMonth());
        
        return revenue;
    }
    
    /**
     * Get current occupancy statistics
     */
    private Map<String, Integer> getOccupancyStats() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        
        ResultSet rs = db.executeQuery(DatabaseQueries.SELECT_OCCUPANCY_STATS);
        if (rs.next()) {
            int occupied = rs.getInt("occupied");
            stats.put("occupied", occupied);
            stats.put("available", 100 - occupied); // Assuming 100 total spots
            stats.put("occupancyRate", (occupied * 100) / 100);
        }
        
        return stats;
    }
    
    /**
     * Get parking violations (overtime parkings)
     */
    private List<Map<String, Object>> getParkingViolations() throws SQLException {
        List<Map<String, Object>> violations = new ArrayList<>();
        
        ResultSet rs = db.executeQuery(DatabaseQueries.SELECT_PARKING_VIOLATIONS);
        
        while (rs.next()) {
            Map<String, Object> violation = new HashMap<>();
            violation.put("parkingCode", rs.getString("parking_code"));
            violation.put("username", rs.getString("username"));
            violation.put("expectedExit", rs.getTimestamp("expected_exit_time"));
            violation.put("overtimeHours", 
                java.time.Duration.between(
                    rs.getTimestamp("expected_exit_time").toLocalDateTime(),
                    LocalDateTime.now()
                ).toHours()
            );
            violations.add(violation);
        }
        
        return violations;
    }
    
    /**
     * Get average occupancy for the month
     */
    private double getAverageOccupancy() throws SQLException {
        // Simplified - in real implementation, calculate from historical data
        ResultSet rs = db.executeQuery(
            "SELECT AVG(occupied_count) as avg_occupancy FROM daily_occupancy_stats " +
            "WHERE date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)");
        
        if (rs.next()) {
            return rs.getDouble("avg_occupancy");
        }
        return 0.0;
    }
    
    /**
     * Get top users by parking frequency
     */
    private List<Map<String, Object>> getTopUsers() throws SQLException {
        List<Map<String, Object>> topUsers = new ArrayList<>();
        
        ResultSet rs = db.executeQuery(
            "SELECT username, COUNT(*) as parking_count, SUM(total_cost) as total_spent " +
            "FROM parking_orders WHERE exit_time IS NOT NULL " +
            "GROUP BY username ORDER BY parking_count DESC LIMIT 10");
        
        while (rs.next()) {
            Map<String, Object> user = new HashMap<>();
            user.put("username", rs.getString("username"));
            user.put("parkingCount", rs.getInt("parking_count"));
            user.put("totalSpent", rs.getDouble("total_spent"));
            topUsers.add(user);
        }
        
        return topUsers;
    }
    
    /**
     * Get total revenue
     */
    private double getTotalRevenue() throws SQLException {
        ResultSet rs = db.executeQuery(
            "SELECT SUM(total_cost) as total FROM parking_orders WHERE exit_time IS NOT NULL");
        
        if (rs.next()) {
            return rs.getDouble("total");
        }
        return 0.0;
    }
    
    /**
     * Get total number of parkings
     */
    private int getTotalParkings() throws SQLException {
        ResultSet rs = db.executeQuery(
            "SELECT COUNT(*) as total FROM parking_orders");
        
        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }
    
    /**
     * Get number of active users
     */
    private int getActiveUsers() throws SQLException {
        ResultSet rs = db.executeQuery(
            "SELECT COUNT(DISTINCT username) as active FROM parking_orders " +
            "WHERE entry_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)");
        
        if (rs.next()) {
            return rs.getInt("active");
        }
        return 0;
    }
}
