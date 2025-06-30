package entities;

/**
 * Enumeration of user roles in the system.
 * Single Responsibility: Defines all possible user roles and their permissions.
 */
public enum UserRole {
    SUBSCRIBER("Subscriber", "Regular parking system user", 1),
    ATTENDANT("Attendant", "Parking attendant who can register users", 2),
    MANAGER("Manager", "System manager with full administrative access", 3),
    ADMIN("Admin", "System administrator with highest privileges", 4);
    
    private final String displayName;
    private final String description;
    private final int privilegeLevel;
    
    UserRole(String displayName, String description, int privilegeLevel) {
        this.displayName = displayName;
        this.description = description;
        this.privilegeLevel = privilegeLevel;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getPrivilegeLevel() {
        return privilegeLevel;
    }
    
    /**
     * Check if role has administrative privileges
     */
    public boolean hasAdminPrivileges() {
        return this == MANAGER || this == ADMIN;
    }
    
    /**
     * Check if role can register new users
     */
    public boolean canRegisterUsers() {
        return this == ATTENDANT || hasAdminPrivileges();
    }
    
    /**
     * Check if role can generate reports
     */
    public boolean canGenerateReports() {
        return hasAdminPrivileges();
    }
    
    /**
     * Check if role can manage parking spots
     */
    public boolean canManageParkingSpots() {
        return this == ATTENDANT || hasAdminPrivileges();
    }
    
    /**
     * Check if role can view all users
     */
    public boolean canViewAllUsers() {
        return hasAdminPrivileges();
    }
    
    /**
     * Check if role has higher privileges than another role
     */
    public boolean hasHigherPrivilegesThan(UserRole other) {
        return this.privilegeLevel > other.privilegeLevel;
    }
    
    /**
     * Get role from string (case-insensitive)
     */
    public static UserRole fromString(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equalsIgnoreCase(role)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
    
    /**
     * Check if string is a valid role
     */
    public static boolean isValidRole(String role) {
        if (role == null) return false;
        
        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get all roles with at least the specified privilege level
     */
    public static UserRole[] getRolesWithMinPrivilege(int minPrivilegeLevel) {
        return java.util.Arrays.stream(values())
                .filter(role -> role.privilegeLevel >= minPrivilegeLevel)
                .toArray(UserRole[]::new);
    }
}