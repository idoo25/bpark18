package entities;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Parking reservation entity.
 * Single Responsibility: Represents a parking reservation in the system.
 */
public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int reservationCode;
    private String username;
    private LocalDateTime reservationDate;
    private LocalDateTime createdAt;
    private String status; // ACTIVE, CANCELLED, ACTIVATED, EXPIRED
    
    /**
     * Default constructor
     */
    public Reservation() {
    }
    
    /**
     * Full constructor
     */
    public Reservation(int reservationCode, String username, LocalDateTime reservationDate, 
                      LocalDateTime createdAt, String status) {
        this.reservationCode = reservationCode;
        this.username = username;
        this.reservationDate = reservationDate;
        this.createdAt = createdAt;
        this.status = status;
    }
    
    // Getters and setters
    public int getReservationCode() {
        return reservationCode;
    }
    
    public void setReservationCode(int reservationCode) {
        this.reservationCode = reservationCode;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public LocalDateTime getReservationDate() {
        return reservationDate;
    }
    
    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Check if reservation is active
     */
    public boolean isActive() {
        return "ACTIVE".equals(status) && reservationDate.isAfter(LocalDateTime.now());
    }
    
    /**
     * Check if reservation is expired
     */
    public boolean isExpired() {
        return reservationDate.isBefore(LocalDateTime.now()) && "ACTIVE".equals(status);
    }
    
    /**
     * Check if reservation can be activated (within 30 minute window)
     */
    public boolean canBeActivated() {
        LocalDateTime now = LocalDateTime.now();
        return "ACTIVE".equals(status) && 
               now.isAfter(reservationDate.minusMinutes(30)) && 
               now.isBefore(reservationDate.plusMinutes(30));
    }
    
    @Override
    public String toString() {
        return "Reservation{" +
               "reservationCode=" + reservationCode +
               ", username='" + username + '\'' +
               ", reservationDate=" + reservationDate +
               ", createdAt=" + createdAt +
               ", status='" + status + '\'' +
               '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return reservationCode == that.reservationCode;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(reservationCode);
    }
}