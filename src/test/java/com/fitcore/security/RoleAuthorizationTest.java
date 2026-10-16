package com.fitcore.security;

import com.fitcore.model.Role;
import com.fitcore.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role-Based Authorization & Permission Tests")
class RoleAuthorizationTest {

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
    }

    @Test
    @DisplayName("Admin role should have all system permissions")
    void testAdminHasAllPermissions() {
        Set<Permission> adminPerms = Permission.getPermissionsForRole(Role.ADMIN);
        assertEquals(Permission.values().length, adminPerms.size());
        assertTrue(adminPerms.contains(Permission.MANAGE_USERS));
        assertTrue(adminPerms.contains(Permission.MANAGE_TRAINERS));
        assertTrue(adminPerms.contains(Permission.SYSTEM_CONFIGURATION));
    }

    @Test
    @DisplayName("Receptionist role should have front-desk permissions but lack administrative configuration")
    void testReceptionistPermissions() {
        Set<Permission> receptionPerms = Permission.getPermissionsForRole(Role.RECEPTIONIST);
        assertTrue(receptionPerms.contains(Permission.MANAGE_MEMBERS));
        assertTrue(receptionPerms.contains(Permission.RECORD_ATTENDANCE));
        assertTrue(receptionPerms.contains(Permission.MANAGE_PAYMENTS));

        // Must NOT have admin configuration or user management
        assertFalse(receptionPerms.contains(Permission.MANAGE_USERS));
        assertFalse(receptionPerms.contains(Permission.SYSTEM_CONFIGURATION));
    }

    @Test
    @DisplayName("Trainer role should have member and attendance views but lack payments management")
    void testTrainerPermissions() {
        Set<Permission> trainerPerms = Permission.getPermissionsForRole(Role.TRAINER);
        assertTrue(trainerPerms.contains(Permission.VIEW_ASSIGNED_MEMBERS));
        assertTrue(trainerPerms.contains(Permission.RECORD_ATTENDANCE));

        assertFalse(trainerPerms.contains(Permission.MANAGE_PAYMENTS));
        assertFalse(trainerPerms.contains(Permission.MANAGE_PLANS));
        assertFalse(trainerPerms.contains(Permission.MANAGE_USERS));
    }

    @Test
    @DisplayName("SecurityContext should enforce permissions and throw AuthorizationException on violation")
    void testSecurityContextEnforcement() {
        User trainerUser = new User(10, "trainer1", "hash", "Trainer Bob", "bob@example.com", Role.TRAINER, "ACTIVE");
        SecurityContext.setCurrentUser(trainerUser);

        // Allowed
        assertDoesNotThrow(() -> SecurityContext.requirePermission(Permission.VIEW_ASSIGNED_MEMBERS));

        // Unauthorized
        assertThrows(AuthorizationException.class, () -> SecurityContext.requirePermission(Permission.MANAGE_USERS));
        assertThrows(AuthorizationException.class, () -> SecurityContext.requirePermission(Permission.MANAGE_PAYMENTS));
        assertThrows(AuthorizationException.class, SecurityContext::requireAdmin);
    }
}
