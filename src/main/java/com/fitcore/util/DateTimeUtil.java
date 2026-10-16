package com.fitcore.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Consistent date and time formatting, parsing, and arithmetic calculations.
 */
public final class DateTimeUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private DateTimeUtil() {
    }

    /**
     * Inclusive end-date convention:
     * If duration is 30 days and start date is Oct 1:
     * Oct 1 is day 1, Oct 30 is day 30.
     * end_date = start_date.plusDays(durationDays - 1).
     */
    public static LocalDate calculateEndDate(LocalDate startDate, int durationDays) {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (durationDays <= 0) {
            throw new IllegalArgumentException("Duration days must be strictly greater than 0.");
        }
        return startDate.plusDays(durationDays - 1);
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE) : "-";
    }

    public static String formatDateIso(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_DATETIME) : "-";
    }

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    public static boolean isExpired(LocalDate endDate) {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    public static boolean isExpiringSoon(LocalDate endDate, int withinDays) {
        if (endDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !endDate.isBefore(today) && !endDate.isAfter(today.plusDays(withinDays));
    }
}
