package com.fitcore.validation;

import com.fitcore.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation Utility Tests")
class ValidationUtilTest {

    @Test
    @DisplayName("Should validate correct email addresses")
    void testValidEmails() {
        assertTrue(ValidationUtil.isValidEmail("john.doe@example.com"));
        assertTrue(ValidationUtil.isValidEmail("member_100@fitness.org"));
        assertTrue(ValidationUtil.isValidEmail("admin+test@domain.co.uk"));
    }

    @Test
    @DisplayName("Should reject invalid email formats")
    void testInvalidEmails() {
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail("notanemail"));
        assertFalse(ValidationUtil.isValidEmail("missingdomain@"));
        assertFalse(ValidationUtil.isValidEmail("@missingusername.com"));
    }

    @Test
    @DisplayName("Should validate phone numbers")
    void testValidPhoneNumbers() {
        assertTrue(ValidationUtil.isValidPhone("+1-555-1234"));
        assertTrue(ValidationUtil.isValidPhone("9876543210"));
        assertTrue(ValidationUtil.isValidPhone("+44 20 7946 0991"));
    }

    @Test
    @DisplayName("Should reject invalid phone numbers")
    void testInvalidPhoneNumbers() {
        assertFalse(ValidationUtil.isValidPhone(null));
        assertFalse(ValidationUtil.isValidPhone(""));
        assertFalse(ValidationUtil.isValidPhone("abc-phone"));
        assertFalse(ValidationUtil.isValidPhone("123")); // too short
    }

    @Test
    @DisplayName("Should check non-negative and positive monetary values")
    void testMonetaryAmounts() {
        assertTrue(ValidationUtil.isPositive(new BigDecimal("10.50")));
        assertFalse(ValidationUtil.isPositive(BigDecimal.ZERO));
        assertFalse(ValidationUtil.isPositive(new BigDecimal("-5.00")));

        assertTrue(ValidationUtil.isNonNegative(BigDecimal.ZERO));
        assertTrue(ValidationUtil.isNonNegative(new BigDecimal("99.99")));
        assertFalse(ValidationUtil.isNonNegative(new BigDecimal("-0.01")));
    }
}
