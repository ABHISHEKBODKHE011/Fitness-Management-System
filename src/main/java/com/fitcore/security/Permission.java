package com.fitcore.security;

import com.fitcore.model.Role;
import java.util.EnumSet;
import java.util.Set;

/**
 * Fine-grained permissions enforced at the service layer.
 */
public enum Permission {
    // Admin permissions
    MANAGE_USERS,
    MANAGE_TRAINERS,
    MANAGE_PLANS,
    VIEW_FINANCIAL_AUDIT,
    SYSTEM_CONFIGURATION,

    // Front-desk & Reception permissions
    MANAGE_MEMBERS,
    ASSIGN_MEMBERSHIP,
    RENEW_MEMBERSHIP,
    RECORD_ATTENDANCE,
    MANAGE_PAYMENTS,
    VIEW_REPORTS,
    PRINT_RECEIPTS,

    // Trainer specific permissions
    VIEW_ASSIGNED_MEMBERS,
    VIEW_MEMBER_ATTENDANCE;

    public static Set<Permission> getPermissionsForRole(Role role) {
        if (role == null) {
            return EnumSet.noneOf(Permission.class);
        }
        return switch (role) {
            case ADMIN -> EnumSet.allOf(Permission.class);
            case RECEPTIONIST -> EnumSet.of(
                    MANAGE_MEMBERS,
                    ASSIGN_MEMBERSHIP,
                    RENEW_MEMBERSHIP,
                    RECORD_ATTENDANCE,
                    MANAGE_PAYMENTS,
                    VIEW_REPORTS,
                    PRINT_RECEIPTS,
                    VIEW_ASSIGNED_MEMBERS,
                    VIEW_MEMBER_ATTENDANCE
            );
            case TRAINER -> EnumSet.of(
                    VIEW_ASSIGNED_MEMBERS,
                    VIEW_MEMBER_ATTENDANCE,
                    RECORD_ATTENDANCE
            );
        };
    }
}
