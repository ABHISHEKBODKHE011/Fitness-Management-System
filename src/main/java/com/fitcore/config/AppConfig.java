package com.fitcore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Global application configuration holder.
 */
public final class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private AppConfig() {
    }

    private static void loadProperties() {
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                properties.load(is);
                logger.info("Loaded application configuration from application.properties");
            } else {
                logger.warn("application.properties not found on classpath, relying on defaults and environment variables.");
            }
        } catch (Exception e) {
            logger.error("Error loading application.properties", e);
        }
    }

    public static String get(String key, String defaultValue) {
        // 1. Environment variable override (e.g. GYM_NAME -> gym.name)
        String envKey = key.toUpperCase().replace('.', '_');
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }

        // 2. System property override (-Dgym.name=...)
        String sysVal = System.getProperty(key);
        if (sysVal != null && !sysVal.trim().isEmpty()) {
            return sysVal.trim();
        }

        // 3. Properties file
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(val.trim());
    }

    public static String getGymName() {
        return get("gym.name", "FitCore Pro Health & Fitness Club");
    }

    public static String getGymTagline() {
        return get("gym.tagline", "Elevate Your Strength, Fuel Your Life");
    }

    public static String getGymAddress() {
        return get("gym.address", "108 Fitness Boulevard, Suite 400, Metro City");
    }

    public static String getGymPhone() {
        return get("gym.phone", "+1 (800) 555-CORE");
    }

    public static String getGymEmail() {
        return get("gym.email", "contact@fitcorepro.com");
    }

    public static String getCurrencySymbol() {
        return get("gym.currencySymbol", "$");
    }

    public static boolean isSingleCheckInPerDay() {
        return getBoolean("attendance.allowSingleCheckInPerDay", true);
    }

    public static boolean isRequireActiveMembershipForAttendance() {
        return getBoolean("attendance.requireActiveMembership", true);
    }
}
