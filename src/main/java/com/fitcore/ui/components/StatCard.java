package com.fitcore.ui.components;

import com.fitcore.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Metric summary card displaying KPI values, icons, and trends with modern card styling.
 */
public class StatCard extends JPanel {

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subtitleLabel;
    private final Color accentColor;

    public StatCard(String title, String initialValue, String subtitle, Color accentColor) {
        this.accentColor = accentColor != null ? accentColor : Theme.ACCENT_PRIMARY;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Top Row: Category title and glowing pill indicator
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(Theme.bold(10));
        titleLabel.setForeground(Theme.TEXT_MUTED);

        JLabel glowPill = new JLabel("●");
        glowPill.setFont(Theme.bold(12));
        glowPill.setForeground(this.accentColor);

        topRow.add(titleLabel, BorderLayout.WEST);
        topRow.add(glowPill, BorderLayout.EAST);

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(Theme.bold(28));
        valueLabel.setForeground(Theme.TEXT_PRIMARY);

        subtitleLabel = new JLabel(subtitle != null ? subtitle : "");
        subtitleLabel.setFont(Theme.font(12));
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);

        contentPanel.add(topRow);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(valueLabel);
        contentPanel.add(Box.createVerticalStrut(6));
        contentPanel.add(subtitleLabel);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle != null ? subtitle : "");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dark card background
        g2.setColor(Theme.BG_CARD);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));

        // Subtle 1px border
        g2.setColor(Theme.BORDER_SUBTLE);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));

        // Left accent bar
        g2.setColor(accentColor);
        g2.fill(new RoundRectangle2D.Float(0, 12, 4, getHeight() - 24, 4, 4));

        g2.dispose();
        super.paintComponent(g);
    }
}
