package com.fitcore.security;

import com.fitcore.model.Role;
import com.fitcore.model.User;
import java.util.Set;

/**
 * Manages the current authenticated user session in the desktop application.
 */
public final class SecurityContext {

    private static volatile User currentUser = null;

    private SecurityContext() {
    }

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isAuthenticated() {
        return currentUser != null;
    }

    public static synchronized void clear() {
        currentUser = null;
    }

    public static synchronized Role getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static synchronized boolean hasPermission(Permission permission) {
        if (currentUser == null) {
            return false;
        }
        Set<Permission> permissions = Permission.getPermissionsForRole(currentUser.getRole());
        return permissions.contains(permission);
    }

    public static synchronized void requirePermission(Permission permission) {
        if (!hasPermission(permission)) {
            String roleName = (currentUser != null && currentUser.getRole() != null)
                    ? currentUser.getRole().getDisplayName()
                    : "Anonymous";
            throw new AuthorizationException(permission,
                    "Access Denied: Role '" + roleName + "' lacks permission '" + permission + "'.");
        }
    }

    public static synchronized void requireAdmin() {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            throw new AuthorizationException("Administrative privileges required for this action.");
        }
    }
}
