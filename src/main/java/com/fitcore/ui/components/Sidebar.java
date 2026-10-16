package com.fitcore.ui.components;

import com.fitcore.model.Role;
import com.fitcore.security.SecurityContext;
import com.fitcore.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Modern deep-navy sidebar navigation with active indicator, categorized menus, and role filtering.
 */
public class Sidebar extends JPanel {

    private final Consumer<String> navigationHandler;
    private final Map<String, NavButton> navButtons = new HashMap<>();
    private String currentSelectedTab = "DASHBOARD";
    private final JPanel menuPanel;

    public Sidebar(Consumer<String> navigationHandler) {
        this.navigationHandler = navigationHandler;
        setPreferredSize(new Dimension(250, 0));
        setBackground(Theme.BG_SIDEBAR);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_SUBTLE));

        // Header: Brand logo and title
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 20, 20));

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brandRow.setOpaque(false);

        JLabel brandIcon = new JLabel("⚡");
        brandIcon.setFont(Theme.bold(20));
        brandIcon.setForeground(Theme.ACCENT_CYAN);

        JLabel brandTitle = new JLabel("FITCORE PRO");
        brandTitle.setFont(Theme.bold(18));
        brandTitle.setForeground(Theme.TEXT_PRIMARY);

        brandRow.add(brandIcon);
        brandRow.add(brandTitle);

        JLabel brandSub = new JLabel("Enterprise Fitness Engine");
        brandSub.setFont(Theme.font(11));
        brandSub.setForeground(Theme.ACCENT_CYAN);
        brandSub.setBorder(BorderFactory.createEmptyBorder(4, 30, 0, 0));

        headerPanel.add(brandRow);
        headerPanel.add(brandSub);

        // Menu Container
        menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 10, 12));

        rebuildMenu();

        // Footer: System telemetry pill
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 16, 14));

        JPanel footerCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        footerCard.setOpaque(true);
        footerCard.setBackground(Theme.BG_CARD);
        footerCard.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true));

        JLabel statusDot = new JLabel("●");
        statusDot.setFont(Theme.bold(10));
        statusDot.setForeground(Theme.ACCENT_EMERALD);

        JLabel versionLabel = new JLabel("v1.0.0 LTS • Java 21");
        versionLabel.setFont(Theme.font(10));
        versionLabel.setForeground(Theme.TEXT_MUTED);

        footerCard.add(statusDot);
        footerCard.add(versionLabel);
        footerPanel.add(footerCard, BorderLayout.CENTER);

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

        addCategoryHeader("OVERVIEW");
        addNavButton("DASHBOARD", "📊  Dashboard");

        addCategoryHeader("ATHLETES & STAFF");
        addNavButton("MEMBERS", "🏋  Member Hub");

        if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
            addNavButton("TRAINERS", "💪  Trainer Roster");
        }

        addCategoryHeader("OPERATIONS");
        if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
            addNavButton("PLANS", "📋  Membership Plans");
        }
        addNavButton("ATTENDANCE", "⏱  Check-in Desk");

        if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
            addCategoryHeader("FINANCIALS & AUDIT");
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

    private void addCategoryHeader(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(Theme.bold(10));
        lbl.setForeground(Theme.TEXT_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuPanel.add(lbl);
    }

    private void addNavButton(String key, String label) {
        NavButton btn = new NavButton(label);
        btn.addActionListener(e -> selectTab(key));
        navButtons.put(key, btn);
        menuPanel.add(btn);
        menuPanel.add(Box.createVerticalStrut(3));
    }

    public void setActiveTab(String key) {
        if (!navButtons.containsKey(key)) {
            key = "DASHBOARD";
        }
        currentSelectedTab = key;

        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            entry.getValue().setActive(active);
        }
    }

    public void selectTab(String key) {
        setActiveTab(key);
        if (navigationHandler != null) {
            navigationHandler.accept(key);
        }
    }

    /**
     * Modern rounded navigation button with active glow indicator.
     */
    private static class NavButton extends JButton {
        private boolean active = false;
        private boolean hover = false;

        public NavButton(String text) {
            super(text);
            setFont(Theme.bold(12));
            setForeground(Theme.TEXT_SECONDARY);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                // Active pill background
                g2.setColor(new Color(99, 102, 241, 40));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                // Glowing border
                g2.setColor(Theme.ACCENT_PRIMARY);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
                // Active indicator pill on left edge
                g2.setColor(Theme.ACCENT_PRIMARY);
                g2.fill(new RoundRectangle2D.Float(3, 8, 4, getHeight() - 16, 4, 4));
            } else if (hover) {
                // Subtle hover
                g2.setColor(Theme.BG_HOVER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
