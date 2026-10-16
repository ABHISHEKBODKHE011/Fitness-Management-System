package com.fitcore.ui.components;

import com.fitcore.config.AppConfig;
import com.fitcore.model.User;
import com.fitcore.security.SecurityContext;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Top navigation bar showing gym name, live system clock, current user role badge, and logout.
 */
public class TopBar extends JPanel {

    private final JLabel clockLabel;
    private final JLabel userLabel;
    private final StatusBadge roleBadge;
    private final JButton logoutButton;
    private final Runnable logoutAction;

    public TopBar(Runnable logoutAction) {
        this.logoutAction = logoutAction;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 60));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        // Left section: Gym name & Tagline
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        leftPanel.setOpaque(false);

        JLabel gymTitle = new JLabel(AppConfig.getGymName());
        gymTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gymTitle.setForeground(new Color(15, 23, 42)); // navy-900

        JLabel gymTagline = new JLabel("• " + AppConfig.getGymTagline());
        gymTagline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gymTagline.setForeground(new Color(100, 116, 139)); // slate-500

        leftPanel.add(gymTitle);
        leftPanel.add(gymTagline);

        // Right section: Clock, User profile, Role badge, Logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 14));
        rightPanel.setOpaque(false);

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clockLabel.setForeground(new Color(71, 85, 105));
        updateClock();

        // Ticking timer
        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();

        userLabel = new JLabel("Staff User");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(new Color(15, 23, 42));

        roleBadge = new StatusBadge("ADMIN");

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setForeground(new Color(239, 68, 68)); // rose-500
        logoutButton.setBackground(new Color(254, 242, 242)); // rose-50
        logoutButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(254, 202, 202), 1),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out of FitCore Pro?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION && logoutAction != null) {
                logoutAction.run();
            }
        });

        rightPanel.add(clockLabel);
        rightPanel.add(new JSeparator(SwingConstants.VERTICAL));
        rightPanel.add(userLabel);
        rightPanel.add(roleBadge);
        rightPanel.add(logoutButton);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        refreshUserDisplay();
    }

    private void updateClock() {
        clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy • HH:mm:ss")));
    }

    public void refreshUserDisplay() {
        User user = SecurityContext.getCurrentUser();
        if (user != null) {
            userLabel.setText(user.getFullName());
            roleBadge.setStatus(user.getRole() != null ? user.getRole().name() : "GUEST");
        } else {
            userLabel.setText("Not Logged In");
            roleBadge.setStatus("OFFLINE");
        }
    }
}
