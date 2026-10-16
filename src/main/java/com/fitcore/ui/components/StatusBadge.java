package com.fitcore.ui.components;

import com.fitcore.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern translucent pill badge with status dot indicator and subtle glowing border.
 */
public class StatusBadge extends JLabel {

    private Color backgroundColor;
    private Color textColor;
    private Color borderColor;

    public StatusBadge(String status) {
        super("", SwingConstants.CENTER);
        setFont(Theme.bold(10));
        setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        setOpaque(false);
        setStatus(status);
    }

    public void setStatus(String status) {
        String cleanStatus = status != null ? status.toUpperCase().trim() : "N/A";
        setText("● " + cleanStatus);
        updateColors(cleanStatus);
        repaint();
    }

    private void updateColors(String status) {
        switch (status) {
            case "ACTIVE", "SUCCESS", "PAID" -> {
                backgroundColor = new Color(16, 185, 129, 32);  // translucent emerald
                textColor = new Color(52, 211, 153);            // emerald-400
                borderColor = new Color(16, 185, 129, 80);
            }
            case "EXPIRED", "INACTIVE", "FAILED" -> {
                backgroundColor = new Color(244, 63, 94, 32);   // translucent rose
                textColor = new Color(251, 113, 133);           // rose-400
                borderColor = new Color(244, 63, 94, 80);
            }
            case "UPCOMING", "PENDING" -> {
                backgroundColor = new Color(245, 158, 11, 32);  // translucent amber
                textColor = new Color(251, 191, 36);            // amber-400
                borderColor = new Color(245, 158, 11, 80);
            }
            case "ADMIN" -> {
                backgroundColor = new Color(99, 102, 241, 36);  // translucent indigo
                textColor = new Color(165, 180, 252);           // indigo-300
                borderColor = new Color(99, 102, 241, 90);
            }
            case "RECEPTIONIST" -> {
                backgroundColor = new Color(6, 182, 212, 32);   // translucent cyan
                textColor = new Color(103, 232, 249);           // cyan-300
                borderColor = new Color(6, 182, 212, 80);
            }
            case "TRAINER" -> {
                backgroundColor = new Color(168, 85, 247, 32);  // translucent purple
                textColor = new Color(216, 180, 254);           // purple-300
                borderColor = new Color(168, 85, 247, 80);
            }
            case "CANCELLED", "REFUNDED" -> {
                backgroundColor = new Color(100, 116, 139, 32);
                textColor = new Color(148, 163, 184);
                borderColor = new Color(100, 116, 139, 80);
            }
            default -> {
                backgroundColor = new Color(51, 65, 85, 40);
                textColor = new Color(203, 213, 225);
                borderColor = new Color(71, 85, 105, 80);
            }
        }
        setForeground(textColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill translucent background pill
        g2.setColor(backgroundColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

        // Draw 1px border
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));

        g2.dispose();
        super.paintComponent(g);
    }
}
