package com.fitcore.ui.components;

import com.fitcore.config.AppConfig;
import com.fitcore.model.User;
import com.fitcore.security.SecurityContext;
import com.fitcore.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Top navigation bar showing gym branding, live clock pill, profile avatar, role badge, and logout.
 */
public class TopBar extends JPanel {

    private final JLabel clockLabel;
    private final JLabel userLabel;
    private final StatusBadge roleBadge;
    private final UserAvatarPanel avatarPanel;
    private final JButton logoutButton;
    private final Runnable logoutAction;

    public TopBar(Runnable logoutAction) {
        this.logoutAction = logoutAction;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 64));
        setBackground(Theme.BG_HEADER);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE));

        // Left section: Gym name & Tagline
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 14));
        leftPanel.setOpaque(false);

        JLabel liveDot = new JLabel("●");
        liveDot.setFont(Theme.bold(12));
        liveDot.setForeground(Theme.ACCENT_EMERALD);

        JLabel gymTitle = new JLabel(AppConfig.getGymName());
        gymTitle.setFont(Theme.bold(16));
        gymTitle.setForeground(Theme.TEXT_PRIMARY);

        JLabel gymTagline = new JLabel("• " + AppConfig.getGymTagline());
        gymTagline.setFont(Theme.font(12));
        gymTagline.setForeground(Theme.TEXT_MUTED);

        leftPanel.add(liveDot);
        leftPanel.add(gymTitle);
        leftPanel.add(gymTagline);

        // Right section: Clock, User profile avatar, Role badge, Logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        rightPanel.setOpaque(false);

        // Digital Clock Pill
        JPanel clockPill = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        clockPill.setOpaque(true);
        clockPill.setBackground(Theme.BG_CARD);
        clockPill.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true));

        JLabel clockIcon = new JLabel("⏱");
        clockIcon.setFont(Theme.font(12));
        clockIcon.setForeground(Theme.ACCENT_CYAN);

        clockLabel = new JLabel();
        clockLabel.setFont(Theme.font(11, Font.BOLD));
        clockLabel.setForeground(Theme.TEXT_SECONDARY);
        updateClock();

        clockPill.add(clockIcon);
        clockPill.add(clockLabel);

        // Ticking timer
        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();

        // User Avatar Circle
        avatarPanel = new UserAvatarPanel("FC");

        userLabel = new JLabel("Staff User");
        userLabel.setFont(Theme.bold(13));
        userLabel.setForeground(Theme.TEXT_PRIMARY);

        roleBadge = new StatusBadge("ADMIN");

        logoutButton = new JButton("⏻  Logout");
        logoutButton.setFont(Theme.bold(11));
        logoutButton.setForeground(Theme.ACCENT_ROSE);
        logoutButton.setBackground(new Color(244, 63, 94, 20));
        logoutButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(244, 63, 94, 60), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
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

        rightPanel.add(clockPill);
        rightPanel.add(Box.createHorizontalStrut(6));
        rightPanel.add(avatarPanel);
        rightPanel.add(userLabel);
        rightPanel.add(roleBadge);
        rightPanel.add(Box.createHorizontalStrut(4));
        rightPanel.add(logoutButton);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        refreshUserDisplay();
    }

    private void updateClock() {
        clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, MMM dd • HH:mm:ss")));
    }

    public void refreshUserDisplay() {
        User user = SecurityContext.getCurrentUser();
        if (user != null) {
            userLabel.setText(user.getFullName());
            roleBadge.setStatus(user.getRole() != null ? user.getRole().name() : "GUEST");
            avatarPanel.setInitials(getInitials(user.getFullName()));
        } else {
            userLabel.setText("Not Logged In");
            roleBadge.setStatus("OFFLINE");
            avatarPanel.setInitials("??");
        }
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    /**
     * Circular gradient avatar with user initials.
     */
    private static class UserAvatarPanel extends JPanel {
        private String initials;

        public UserAvatarPanel(String initials) {
            this.initials = initials;
            setPreferredSize(new Dimension(34, 34));
            setOpaque(false);
        }

        public void setInitials(String initials) {
            this.initials = initials;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, Theme.ACCENT_PRIMARY,
                    getWidth(), getHeight(), Theme.ACCENT_CYAN
            );
            g2.setPaint(gp);
            g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));

            g2.setColor(Color.WHITE);
            g2.setFont(Theme.bold(12));
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(initials)) / 2;
            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(initials, x, y);
            g2.dispose();
        }
    }
}
