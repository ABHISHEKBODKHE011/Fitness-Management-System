package com.fitcore;

import com.fitcore.config.DatabaseConfig;
import com.fitcore.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point for FitCore Pro - Advanced Fitness Management System.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Set system properties for high-DPI scaling and font smoothing
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        logger.info("Starting FitCore Pro Desktop Application...");

        // Setup FlatLaf modern UI theme (Trendy macOS-styled Dark Theme)
        try {
            com.formdev.flatlaf.themes.FlatMacDarkLaf.setup();
            configureCustomUiProperties();
            logger.info("FlatMacDarkLaf modern Look & Feel successfully initialized.");
        } catch (Exception e) {
            logger.warn("Could not load FlatMacDarkLaf, trying FlatDarkLaf: {}", e.getMessage());
            try {
                com.formdev.flatlaf.FlatDarkLaf.setup();
                configureCustomUiProperties();
            } catch (Exception ex) {
                logger.warn("Falling back to system Look & Feel: {}", ex.getMessage());
            }
        }

        // Verify database connectivity
        SwingUtilities.invokeLater(() -> {
            try {
                if (!DatabaseConfig.testConnection()) {
                    logger.warn("Database connection could not be established immediately. Check database configuration.");
                    int option = JOptionPane.showConfirmDialog(
                            null,
                            "Unable to connect to the MySQL database at localhost:3306.\n\n" +
                            "Please ensure MySQL / MariaDB service is running and database 'fitcore_pro' is imported.\n" +
                            "Would you like to continue launching the application anyway?",
                            "Database Connection Warning",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );
                    if (option != JOptionPane.YES_OPTION) {
                        System.exit(1);
                    }
                }

                MainFrame mainFrame = new MainFrame();
                // mainFrame visibility is handled inside MainFrame after login dialog
            } catch (Exception e) {
                logger.error("Fatal error during application launch", e);
                JOptionPane.showMessageDialog(
                        null,
                        "Fatal initialization error: " + e.getMessage(),
                        "Launch Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }

    private static void configureCustomUiProperties() {
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("ProgressBar.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumbArc", 10);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("Table.rowHeight", 42);
        UIManager.put("TableHeader.height", 42);
        UIManager.put("TableHeader.separatorColor", new Color(33, 45, 68));
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.tabHeight", 38);
        UIManager.put("TabbedPane.selectedBackground", new Color(30, 41, 59));
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("TitlePane.centerTitle", true);
    }
}
