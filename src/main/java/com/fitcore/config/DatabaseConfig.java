package com.fitcore.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Enterprise database configuration and HikariCP connection pool manager.
 */
public final class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static volatile HikariDataSource dataSource = null;
    private static final Object lock = new Object();

    private DatabaseConfig() {
    }

    /**
     * Initializes the HikariCP DataSource.
     */
    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (lock) {
                if (dataSource == null) {
                    initDataSource();
                }
            }
        }
        return dataSource;
    }

    /**
     * Custom initialization for testing or custom DataSource injection.
     */
    public static void setCustomDataSource(HikariDataSource customDs) {
        synchronized (lock) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
            dataSource = customDs;
        }
    }

    private static void initDataSource() {
        try {
            String host = AppConfig.get("db.host", "localhost");
            int port = AppConfig.getInt("db.port", 3306);
            String dbName = AppConfig.get("db.name", "fitcore_pro");
            String username = AppConfig.get("db.username", "root");
            String password = AppConfig.get("db.password", "");
            boolean useSSL = AppConfig.getBoolean("db.useSSL", false);
            boolean allowPublicKey = AppConfig.getBoolean("db.allowPublicKeyRetrieval", true);
            String serverTimezone = AppConfig.get("db.serverTimezone", "UTC");

            String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%d/%s?useSSL=%b&allowPublicKeyRetrieval=%b&serverTimezone=%s&characterEncoding=UTF-8",
                    host, port, dbName, useSSL, allowPublicKey, serverTimezone
            );

            logger.info("Initializing HikariCP connection pool with URL: jdbc:mysql://{}:{}/{}", host, port, dbName);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            // Pool settings
            config.setMaximumPoolSize(AppConfig.getInt("db.pool.maximumSize", 10));
            config.setMinimumIdle(AppConfig.getInt("db.pool.minimumIdle", 2));
            config.setConnectionTimeout(AppConfig.getInt("db.pool.connectionTimeout", 30000));
            config.setIdleTimeout(AppConfig.getInt("db.pool.idleTimeout", 600000));
            config.setMaxLifetime(AppConfig.getInt("db.pool.maxLifetime", 1800000));
            config.setPoolName("FitCore-HikariPool");

            // Recommended MySQL performance optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            logger.info("HikariCP connection pool successfully established.");

            // Register JVM shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConfig::closePool, "FitCore-DB-Shutdown"));

        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool: {}", e.getMessage(), e);
            throw new RuntimeException("Database initialization failure: " + e.getMessage(), e);
        }
    }

    /**
     * Obtains an active connection from the pool.
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Checks whether the database connection is live.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Database connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Closes the connection pool gracefully.
     */
    public static void closePool() {
        synchronized (lock) {
            if (dataSource != null && !dataSource.isClosed()) {
                logger.info("Shutting down HikariCP database pool...");
                dataSource.close();
                dataSource = null;
            }
        }
    }
}
