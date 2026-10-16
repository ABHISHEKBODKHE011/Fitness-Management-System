package com.fitcore.model;

/**
 * System roles defining access levels and permissions in FitCore Pro.
 */
public enum Role {
    ADMIN("Administrator", "Full system access including staff, financial, and configuration management"),
    RECEPTIONIST("Receptionist", "Front-desk operations: member registrations, memberships, check-ins, and payments"),
    TRAINER("Fitness Trainer", "Specialized access: view assigned members, training schedules, and member attendance");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static Role fromString(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            return RECEPTIONIST;
        }
        for (Role r : values()) {
            if (r.name().equalsIgnoreCase(roleStr.trim())) {
                return r;
            }
        }
        return RECEPTIONIST;
    }
}
