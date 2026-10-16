package com.fitcore.ui.components;

import com.fitcore.model.Role;
import com.fitcore.security.SecurityContext;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Modern deep-navy sidebar navigation with active indicator and role filtering.
 */
public class Sidebar extends JPanel {

    private static final Color BG_COLOR = new Color(15, 23, 42);       // slate-900 (deep navy)
    private static final Color ACTIVE_BG = new Color(30, 41, 59);     // slate-800
    private static final Color HOVER_BG = new Color(30, 41, 59, 150);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);  // blue-500
    private static final Color TEXT_MUTED = new Color(148, 163, 184); // slate-400
    private static final Color TEXT_ACTIVE = Color.WHITE;

    private final Consumer<String> navigationHandler;
    private final Map<String, JButton> navButtons = new HashMap<>();
    private String currentSelectedTab = "DASHBOARD";
    private final JPanel menuPanel;

    public Sidebar(Consumer<String> navigationHandler) {
        this.navigationHandler = navigationHandler;
        setPreferredSize(new Dimension(240, 0));
        setBackground(BG_COLOR);
        setLayout(new BorderLayout());

        // Header: Brand logo and title
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        JLabel brandIcon = new JLabel("⚡ FITCORE PRO");
        brandIcon.setFont(new Font("Segoe UI", Font.BOLD, 19));
        brandIcon.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("Fitness Management System");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        brandSub.setForeground(ACCENT_BLUE);

        headerPanel.add(brandIcon);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(brandSub);

        // Menu Container
        menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        rebuildMenu();

        // Footer: Version info
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 16, 0));
        JLabel versionLabel = new JLabel("FitCore Pro v1.0.0 LTS • Java 21");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        versionLabel.setForeground(new Color(100, 116, 139));
        footerPanel.add(versionLabel);

        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(menuPanel) {
            {
                setBorder(null);
                setOpaque(false);
                getViewport().setOpaque(false);
                setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            }
        }, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    public void rebuildMenu() {
        menuPanel.removeAll();
        navButtons.clear();

        Role role = SecurityContext.getCurrentRole();

        addNavButton("DASHBOARD", "📊  Dashboard");
        addNavButton("MEMBERS", "🏋  Member Management");

        if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
            addNavButton("TRAINERS", "💪  Trainer Roster");
            addNavButton("PLANS", "📋  Membership Plans");
        }

        addNavButton("ATTENDANCE", "⏱  Attendance & Check-in");

        if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
            addNavButton("PAYMENTS", "💳  Billing & Payments");
            addNavButton("REPORTS", "📈  Reports & Analytics");
        }

        if (role == Role.ADMIN) {
            addNavButton("AUDIT", "🛡  System Audit Logs");
        }

        menuPanel.revalidate();
        menuPanel.repaint();

        setActiveTab(currentSelectedTab);
    }

    private void addNavButton(String key, String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(BG_COLOR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        btn.addActionListener(e -> selectTab(key));

        navButtons.put(key, btn);
        menuPanel.add(btn);
        menuPanel.add(Box.createVerticalStrut(4));
    }

    public void setActiveTab(String key) {
        if (!navButtons.containsKey(key)) {
            key = "DASHBOARD";
        }
        currentSelectedTab = key;

        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            JButton b = entry.getValue();
            b.setBackground(active ? ACTIVE_BG : BG_COLOR);
            b.setForeground(active ? TEXT_ACTIVE : TEXT_MUTED);
            if (active) {
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, ACCENT_BLUE),
                        BorderFactory.createEmptyBorder(12, 12, 12, 16)
                ));
            } else {
                b.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            }
        }
    }

    public void selectTab(String key) {
        setActiveTab(key);
        if (navigationHandler != null) {
            navigationHandler.accept(key);
        }
    }
}
