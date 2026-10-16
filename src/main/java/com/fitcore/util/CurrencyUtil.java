package com.fitcore.util;

import com.fitcore.config.AppConfig;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Currency formatting and monetary calculations.
 */
public final class CurrencyUtil {

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");

    private CurrencyUtil() {
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        return AppConfig.getCurrencySymbol() + CURRENCY_FORMAT.format(amount);
    }

    public static String format(double amount) {
        return format(BigDecimal.valueOf(amount));
    }

    public static BigDecimal parse(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        String clean = amountStr.replace(AppConfig.getCurrencySymbol(), "").replace(",", "").trim();
        return new BigDecimal(clean);
    }
}
