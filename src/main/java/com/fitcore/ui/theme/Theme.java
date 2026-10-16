package com.fitcore.ui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Enterprise Design System and theme token provider for FitCore Pro.
 * Provides a sleek, trendy cyber-SaaS dark aesthetic inspired by modern fitness & developer platforms.
 */
public final class Theme {

    private Theme() {}

    // Core Canvas & Surface Colors
    public static final Color BG_APP = new Color(11, 15, 25);            // Deep Obsidian (#0B0F19)
    public static final Color BG_HEADER = new Color(15, 23, 42);         // Slate-900 (#0F172A)
    public static final Color BG_SIDEBAR = new Color(11, 15, 25);        // Deep Obsidian (#0B0F19)
    public static final Color BG_CARD = new Color(18, 25, 41);           // Deep Card Surface (#121929)
    public static final Color BG_CARD_LIGHT = new Color(24, 33, 53);     // Elevated Surface (#182135)
    public static final Color BG_INPUT = new Color(15, 23, 42);          // Input Field Bg (#0F172A)
    public static final Color BG_HOVER = new Color(30, 41, 59);          // Slate-800 (#1E293B)

    // Border Colors
    public static final Color BORDER_SUBTLE = new Color(33, 45, 68);     // Subtle Border (#212D44)
    public static final Color BORDER_LIGHT = new Color(51, 65, 85);      // Slate-700 (#334155)
    public static final Color BORDER_FOCUS = new Color(99, 102, 241);    // Electric Indigo (#6366F1)

    // Neon Accent Palette
    public static final Color ACCENT_PRIMARY = new Color(99, 102, 241);  // Electric Indigo (#6366F1)
    public static final Color ACCENT_SECONDARY = new Color(59, 130, 246);// Electric Blue (#3B82F6)
    public static final Color ACCENT_CYAN = new Color(6, 182, 212);      // Cyber Cyan (#06B6D4)
    public static final Color ACCENT_EMERALD = new Color(16, 185, 129);  // Neon Emerald (#10B981)
    public static final Color ACCENT_AMBER = new Color(245, 158, 11);    // Amber Gold (#F59E0B)
    public static final Color ACCENT_ROSE = new Color(244, 63, 94);      // Vibrant Crimson (#F43F5E)
    public static final Color ACCENT_PURPLE = new Color(168, 85, 247);   // Neon Purple (#A855F7)

    // Typography Colors
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);   // Crisp Off-White (#F8FAFC)
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184); // Slate-400 (#94A3B8)
    public static final Color TEXT_MUTED = new Color(100, 116, 139);     // Slate-500 (#64748B)

    // Fonts
    private static final String PREFERRED_FONT_FAMILY;

    static {
        String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String chosen = "Segoe UI";
        for (String f : families) {
            if (f.equalsIgnoreCase("Segoe UI Variable Display")) {
                chosen = "Segoe UI Variable Display";
                break;
            } else if (f.equalsIgnoreCase("Inter")) {
                chosen = "Inter";
                break;
            }
        }
        PREFERRED_FONT_FAMILY = chosen;
    }

    public static Font font(int size, int style) {
        return new Font(PREFERRED_FONT_FAMILY, style, size);
    }

    public static Font font(int size) {
        return font(size, Font.PLAIN);
    }

    public static Font bold(int size) {
        return font(size, Font.BOLD);
    }

    /**
     * Creates a modern rounded card panel with subtle 1px border.
     */
    public static JPanel createCard() {
        return new ModernCardPanel(BG_CARD, BORDER_SUBTLE, 16);
    }

    public static JPanel createCard(Color bg, Color border, int radius) {
        return new ModernCardPanel(bg, border, radius);
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        );
    }

    /**
     * Primary gradient action button (Electric Indigo/Blue).
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, ACCENT_PRIMARY, new Color(79, 70, 229), TEXT_PRIMARY);
    }

    /**
     * Success button (Neon Emerald).
     */
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, ACCENT_EMERALD, new Color(5, 150, 105), TEXT_PRIMARY);
    }

    /**
     * Danger button (Rose).
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, ACCENT_ROSE, new Color(225, 29, 72), TEXT_PRIMARY);
    }

    /**
     * Ghost / Secondary subtle button.
     */
    public static JButton createGhostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(bold(12));
        btn.setForeground(TEXT_SECONDARY);
        btn.setBackground(BG_CARD_LIGHT);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BG_HOVER);
                btn.setForeground(TEXT_PRIMARY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(BG_CARD_LIGHT);
                btn.setForeground(TEXT_SECONDARY);
            }
        });
        return btn;
    }

    private static JButton createStyledButton(String text, Color baseColor, Color hoverColor, Color fgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color current = getModel().isRollover() ? hoverColor : baseColor;
                g2.setColor(current);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(bold(12));
        btn.setForeground(fgColor);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Search input field with placeholder and rounded styling.
     */
    public static JTextField createSearchField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(font(13));
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_CYAN);
        field.putClientProperty("JTextField.placeholderText", "🔍  " + placeholder);
        field.putClientProperty("JTextField.showClearButton", true);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        return field;
    }

    /**
     * Custom panel with smooth rounded corners and subtle border.
     */
    private static class ModernCardPanel extends JPanel {
        private final Color bgColor;
        private final Color borderColor;
        private final int cornerRadius;

        public ModernCardPanel(Color bgColor, Color borderColor, int cornerRadius) {
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            this.cornerRadius = cornerRadius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Fill background
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
            // Draw 1px border
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
