package com.fitcore.validation;

import com.fitcore.util.DateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Date Arithmetic & Inclusive End-Date Tests")
class DateTimeUtilTest {

    @Test
    @DisplayName("Should calculate inclusive end dates consistently: (start + duration - 1)")
    void testInclusiveEndDateCalculation() {
        LocalDate start = LocalDate.of(2026, 10, 1);

        // 1-day membership starting Oct 1 must end on Oct 1
        LocalDate oneDayEnd = DateTimeUtil.calculateEndDate(start, 1);
        assertEquals(LocalDate.of(2026, 10, 1), oneDayEnd);

        // 30-day membership starting Oct 1 must end on Oct 30
        LocalDate thirtyDayEnd = DateTimeUtil.calculateEndDate(start, 30);
        assertEquals(LocalDate.of(2026, 10, 30), thirtyDayEnd);

        // 365-day annual membership starting Jan 1, 2026 must end on Dec 31, 2026
        LocalDate annualStart = LocalDate.of(2026, 1, 1);
        LocalDate annualEnd = DateTimeUtil.calculateEndDate(annualStart, 365);
        assertEquals(LocalDate.of(2026, 12, 31), annualEnd);
    }

    @Test
    @DisplayName("Should throw exception when duration is zero or negative")
    void testInvalidDuration() {
        LocalDate start = LocalDate.of(2026, 10, 1);
        assertThrows(IllegalArgumentException.class, () -> DateTimeUtil.calculateEndDate(start, 0));
        assertThrows(IllegalArgumentException.class, () -> DateTimeUtil.calculateEndDate(start, -10));
    }

    @Test
    @DisplayName("Should detect expired dates relative to current date")
    void testExpiredDetection() {
        LocalDate past = LocalDate.now().minusDays(5);
        LocalDate future = LocalDate.now().plusDays(5);

        assertTrue(DateTimeUtil.isExpired(past));
        assertFalse(DateTimeUtil.isExpired(future));
    }

    @Test
    @DisplayName("Should detect memberships expiring within threshold days")
    void testExpiringSoon() {
        LocalDate today = LocalDate.now();
        LocalDate in3Days = today.plusDays(3);
        LocalDate in10Days = today.plusDays(10);

        assertTrue(DateTimeUtil.isExpiringSoon(in3Days, 7));
        assertFalse(DateTimeUtil.isExpiringSoon(in10Days, 7));
        assertTrue(DateTimeUtil.isExpiringSoon(today, 7));
    }
}
