package com.fitcore.service;

import com.fitcore.dao.AuditLogDao;
import com.fitcore.dao.UserDao;
import com.fitcore.model.AuditLog;
import com.fitcore.model.Role;
import com.fitcore.model.User;
import com.fitcore.security.PasswordHasher;
import com.fitcore.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication, session management, and credential verification service.
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDao userDao;
    private final AuditLogDao auditLogDao;

    public AuthService() {
        this(new UserDao(), new AuditLogDao());
    }

    public AuthService(UserDao userDao, AuditLogDao auditLogDao) {
        this.userDao = userDao;
        this.auditLogDao = auditLogDao;
    }

    /**
     * Authenticates a user against stored BCrypt hashes.
     */
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        User user = userDao.findByUsername(username.trim());
        if (user == null) {
            logger.warn("Failed login attempt: username '{}' not found", username);
            throw new IllegalArgumentException("Invalid username or password.");
        }

        if (!user.isActive()) {
            logger.warn("Login attempt for deactivated user '{}'", username);
            throw new IllegalStateException("Account is deactivated. Please contact the administrator.");
        }

        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            logger.warn("Failed login attempt: incorrect password for user '{}'", username);
            throw new IllegalArgumentException("Invalid username or password.");
        }

        SecurityContext.setCurrentUser(user);
        logger.info("User '{}' ({}) successfully logged in.", user.getUsername(), user.getRole());

        auditLogDao.log(new AuditLog(
                null, user.getId(), user.getUsername(), "LOGIN", "AUTH",
                String.valueOf(user.getId()), "Successful login to FitCore Pro desktop interface"
        ));

        return user;
    }

    /**
     * Logs out the current user and clears the security context.
     */
    public void logout() {
        User current = SecurityContext.getCurrentUser();
        if (current != null) {
            logger.info("User '{}' logged out.", current.getUsername());
            auditLogDao.log(new AuditLog(
                    null, current.getId(), current.getUsername(), "LOGOUT", "AUTH",
                    String.valueOf(current.getId()), "User initiated session logout"
            ));
        }
        SecurityContext.clear();
    }

    /**
     * Changes a user password with old-password verification.
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (!PasswordHasher.verify(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password verification failed.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long.");
        }

        String newHash = PasswordHasher.hash(newPassword);
        boolean updated = userDao.updatePassword(userId, newHash);
        if (updated) {
            auditLogDao.log(new AuditLog(
                    null, user.getId(), user.getUsername(), "CHANGE_PASSWORD", "USER",
                    String.valueOf(user.getId()), "User changed their account password"
            ));
        }
        return updated;
    }

    /**
     * Safe bootstrap procedure: creates default administrator only if the users table is completely empty.
     */
    public void bootstrapAdminIfNeeded() {
        if (userDao.count() == 0) {
            logger.info("No system users detected. Initializing default admin setup...");
            User admin = new User(
                    null,
                    "admin",
                    PasswordHasher.hash("Admin@123"),
                    "System Administrator",
                    "admin@fitcorepro.com",
                    Role.ADMIN,
                    "ACTIVE"
            );
            userDao.create(admin);
            logger.info("Safe default administrator created successfully (username: 'admin').");
        }
    }
}
