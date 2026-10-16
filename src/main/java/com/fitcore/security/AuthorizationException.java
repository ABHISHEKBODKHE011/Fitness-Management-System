package com.fitcore.security;

/**
 * Thrown when an authenticated user attempts an operation without sufficient permissions.
 */
public class AuthorizationException extends RuntimeException {

    private final Permission requiredPermission;

    public AuthorizationException(String message) {
        super(message);
        this.requiredPermission = null;
    }

    public AuthorizationException(Permission requiredPermission, String message) {
        super(message);
        this.requiredPermission = requiredPermission;
    }

    public Permission getRequiredPermission() {
        return requiredPermission;
    }
}
