package entities;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Parking order entity.
 * Single Responsibility: Represents a parking session/order in the system.
 */
public class ParkingOrder implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String parkingCode;
    private String username;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private LocalDateTime expectedExitTime;
    private double totalCost;
    private int spotId;
    
    /**
     * Default constructor
     */
    public ParkingOrder() {
    }
    
    /**
     * Constructor for active parking
     */
    public ParkingOrder(String parkingCode, String username, LocalDateTime entryTime, 
                       LocalDateTime expectedExitTime, int spotId) {
        this.parkingCode = parkingCode;
        this.username = username;
        this.entryTime = entryTime;
        this.expectedExitTime = expectedExitTime;
        this.spotId = spotId;
    }
    
    // Getters and setters
    public String getParkingCode() {
        return parkingCode;
    }
    
    public void setParkingCode(String parkingCode) {
        this.parkingCode = parkingCode;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }
    
    public LocalDateTime getExitTime() {
        return exitTime;
    }
    
    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }
    
    public LocalDateTime getExpectedExitTime() {
        return expectedExitTime;
    }
    
    public void setExpectedExitTime(LocalDateTime expectedExitTime) {
        this.expectedExitTime = expectedExitTime;
    }
    
    public double getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
    
    public int getSpotId() {
        return spotId;
    }
    
    public void setSpotId(int spotId) {
        this.spotId = spotId;
    }
    
    /**
     * Check if parking is active
     */
    public boolean isActive() {
        return exitTime == null;
    }
    
    @Override
    public String toString() {
        return "ParkingOrder{" +
               "parkingCode='" + parkingCode + '\'' +
               ", username='" + username + '\'' +
               ", entryTime=" + entryTime +
               ", exitTime=" + exitTime +
               ", expectedExitTime=" + expectedExitTime +
               ", totalCost=" + totalCost +
               ", spotId=" + spotId +
               '}';
    }
}