package com.fitcore.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Enterprise-grade BCrypt password hashing and verification utility.
 */
public final class PasswordHasher {

    private static final int LOG_ROUNDS = 10;

    private PasswordHasher() {
        // Utility class
    }

    /**
     * Hashes a plaintext password with a salt using BCrypt.
     */
    public static String hash(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifies whether a plaintext candidate password matches the BCrypt hash.
     */
    public static boolean verify(String candidatePassword, String storedHash) {
        if (candidatePassword == null || storedHash == null || storedHash.trim().isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(candidatePassword, storedHash);
        } catch (IllegalArgumentException e) {
            // Malformed hash
            return false;
        }
    }
}
