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

        // Setup FlatLaf modern UI theme
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            configureCustomUiProperties();
            logger.info("FlatLaf modern Look & Feel successfully initialized.");
        } catch (Exception e) {
            logger.warn("Could not load FlatLaf, falling back to system Look & Feel: {}", e.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
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
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("ProgressBar.arc", 8);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TableHeader.separatorColor", new Color(226, 232, 240));
    }
}
