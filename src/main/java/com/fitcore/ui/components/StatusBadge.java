package com.fitcore.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Modern pill badge for displaying statuses (ACTIVE, EXPIRED, SUCCESS, etc.).
 */
public class StatusBadge extends JLabel {

    private Color backgroundColor;
    private Color textColor;

    public StatusBadge(String status) {
        super(status != null ? status.toUpperCase() : "N/A", SwingConstants.CENTER);
        setFont(new Font("Segoe UI", Font.BOLD, 11));
        setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        setOpaque(false);
        updateColors(status);
    }

    public void setStatus(String status) {
        setText(status != null ? status.toUpperCase() : "N/A");
        updateColors(status);
        repaint();
    }

    private void updateColors(String status) {
        if (status == null) {
            backgroundColor = new Color(226, 232, 240); // slate-200
            textColor = new Color(71, 85, 105);        // slate-600
            return;
        }

        switch (status.toUpperCase()) {
            case "ACTIVE", "SUCCESS", "PAID" -> {
                backgroundColor = new Color(220, 252, 231); // emerald-100
                textColor = new Color(22, 101, 52);         // emerald-800
            }
            case "EXPIRED", "INACTIVE", "FAILED" -> {
                backgroundColor = new Color(254, 226, 226); // rose-100
                textColor = new Color(153, 27, 27);         // rose-800
            }
            case "UPCOMING", "PENDING" -> {
                backgroundColor = new Color(254, 243, 199); // amber-100
                textColor = new Color(146, 64, 14);         // amber-800
            }
            case "CANCELLED", "REFUNDED" -> {
                backgroundColor = new Color(241, 245, 249); // slate-100
                textColor = new Color(100, 116, 139);       // slate-500
            }
            default -> {
                backgroundColor = new Color(226, 232, 240);
                textColor = new Color(51, 65, 85);
            }
        }
        setForeground(textColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}
