package entities;

import java.io.Serializable;

/**
 * Parking subscriber entity.
 * Single Responsibility: Represents a user/subscriber in the system.
 */
public class ParkingSubscriber implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int subscriberID;
    private String username;
    private String name;
    private String phone;
    private String email;
    private String carNumber;
    
    /**
     * Default constructor
     */
    public ParkingSubscriber() {
    }
    
    /**
     * Full constructor
     */
    public ParkingSubscriber(int subscriberID, String username, String name, 
                           String phone, String email, String carNumber) {
        this.subscriberID = subscriberID;
        this.username = username;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.carNumber = carNumber;
    }
    
    // Getters and setters
    public int getSubscriberID() {
        return subscriberID;
    }
    
    public void setSubscriberID(int subscriberID) {
        this.subscriberID = subscriberID;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCarNumber() {
        return carNumber;
    }
    
    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }
    
    @Override
    public String toString() {
        return "ParkingSubscriber{" +
               "subscriberID=" + subscriberID +
               ", username='" + username + '\'' +
               ", name='" + name + '\'' +
               ", phone='" + phone + '\'' +
               ", email='" + email + '\'' +
               ", carNumber='" + carNumber + '\'' +
               '}';
    }
}