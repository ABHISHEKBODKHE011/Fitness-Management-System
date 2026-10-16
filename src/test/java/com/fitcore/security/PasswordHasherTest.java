package com.fitcore.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Hashing & Verification Tests")
class PasswordHasherTest {

    @Test
    @DisplayName("Should hash password and successfully verify with correct plaintext")
    void testHashAndVerifyCorrect() {
        String plain = "SuperSecret@2026";
        String hash = PasswordHasher.hash(plain);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        assertTrue(PasswordHasher.verify(plain, hash));
    }

    @Test
    @DisplayName("Should reject verification with incorrect password")
    void testVerifyIncorrect() {
        String plain = "AdminPassword@1";
        String hash = PasswordHasher.hash(plain);

        assertFalse(PasswordHasher.verify("WrongPassword", hash));
        assertFalse(PasswordHasher.verify("adminpassword@1", hash)); // case sensitive
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when hashing null or empty password")
    void testNullOrEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash("   "));
    }

    @Test
    @DisplayName("Should safely return false for null candidate or malformed hash")
    void testVerifyMalformed() {
        assertFalse(PasswordHasher.verify(null, "someHash"));
        assertFalse(PasswordHasher.verify("pass", null));
        assertFalse(PasswordHasher.verify("pass", "invalid_not_a_bcrypt_hash"));
    }
}
