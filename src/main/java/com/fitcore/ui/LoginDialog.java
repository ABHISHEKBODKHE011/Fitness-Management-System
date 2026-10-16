package com.fitcore.ui;

import com.fitcore.config.AppConfig;
import com.fitcore.model.User;
import com.fitcore.service.AuthService;

import com.fitcore.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Modern login dialog with branding banner, quick demo role presets, and error handling.
 */
public class LoginDialog extends JDialog {

    private final AuthService authService;
    private boolean authenticated = false;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private JButton loginButton;

    public LoginDialog(Frame parent, AuthService authService) {
        super(parent, "FitCore Pro • System Authentication", true);
        this.authService = authService;

        setSize(760, 470);
        setResizable(false);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel container = new JPanel(new GridLayout(1, 2));
        container.setBackground(Theme.BG_APP);

        // Left Branding Panel with subtle gradient
        JPanel leftBanner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(15, 23, 42),
                        getWidth(), getHeight(), new Color(11, 15, 25)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Subtle glowing line
                g2.setColor(new Color(99, 102, 241, 60));
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        leftBanner.setLayout(new BoxLayout(leftBanner, BoxLayout.Y_AXIS));
        leftBanner.setBorder(BorderFactory.createEmptyBorder(44, 34, 44, 34));

        JLabel badgePill = new JLabel("  ENTERPRISE FITNESS v1.0  ");
        badgePill.setFont(Theme.bold(10));
        badgePill.setForeground(Theme.ACCENT_CYAN);
        badgePill.setOpaque(true);
        badgePill.setBackground(new Color(6, 182, 212, 25));
        badgePill.setBorder(BorderFactory.createLineBorder(new Color(6, 182, 212, 60), 1, true));

        JLabel logoLbl = new JLabel("⚡ FITCORE PRO");
        logoLbl.setFont(Theme.bold(26));
        logoLbl.setForeground(Theme.TEXT_PRIMARY);

        JLabel tagLbl = new JLabel("Next-Gen Gym & Athletic Operations");
        tagLbl.setFont(Theme.font(12));
        tagLbl.setForeground(Theme.ACCENT_CYAN);

        JLabel descLbl = new JLabel("<html><br>" +
                "<div style='line-height:1.7; color:#94A3B8; font-size:11px;'>" +
                "✓ &nbsp;<b style='color:#F8FAFC;'>Role-Based Access Control</b> (Admin, Reception, Trainer)<br>" +
                "✓ &nbsp;<b style='color:#F8FAFC;'>Live MySQL 8.x</b> Analytics & Trend Engine<br>" +
                "✓ &nbsp;<b style='color:#F8FAFC;'>Member 360° Profiles</b> & Active Subscriptions<br>" +
                "✓ &nbsp;<b style='color:#F8FAFC;'>Strict Attendance Guard</b> & QR/Key Check-in<br>" +
                "✓ &nbsp;<b style='color:#F8FAFC;'>Automated Billing Ledger</b> & Thermal Receipts" +
                "</div></html>");

        JLabel statusDot = new JLabel("● MySQL 8.x Database Online");
        statusDot.setFont(Theme.bold(11));
        statusDot.setForeground(Theme.ACCENT_EMERALD);
        statusDot.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        leftBanner.add(badgePill);
        leftBanner.add(Box.createVerticalStrut(12));
        leftBanner.add(logoLbl);
        leftBanner.add(Box.createVerticalStrut(4));
        leftBanner.add(tagLbl);
        leftBanner.add(descLbl);
        leftBanner.add(Box.createVerticalGlue());
        leftBanner.add(statusDot);

        // Right Form Panel
        JPanel rightForm = new JPanel();
        rightForm.setBackground(Theme.BG_CARD);
        rightForm.setLayout(new BoxLayout(rightForm, BoxLayout.Y_AXIS));
        rightForm.setBorder(BorderFactory.createEmptyBorder(40, 36, 40, 36));

        JLabel loginHeader = new JLabel("Sign In");
        loginHeader.setFont(Theme.bold(22));
        loginHeader.setForeground(Theme.TEXT_PRIMARY);

        JLabel loginSub = new JLabel("Enter your staff credentials to access console");
        loginSub.setFont(Theme.font(12));
        loginSub.setForeground(Theme.TEXT_SECONDARY);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.bold(11));
        errorLabel.setForeground(Theme.ACCENT_ROSE);

        usernameField = new JTextField();
        usernameField.setFont(Theme.font(13));
        usernameField.setBackground(Theme.BG_INPUT);
        usernameField.setForeground(Theme.TEXT_PRIMARY);
        usernameField.setCaretColor(Theme.ACCENT_CYAN);
        usernameField.putClientProperty("JTextField.placeholderText", "Username (e.g. admin)");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        passwordField = new JPasswordField();
        passwordField.setFont(Theme.font(13));
        passwordField.setBackground(Theme.BG_INPUT);
        passwordField.setForeground(Theme.TEXT_PRIMARY);
        passwordField.setCaretColor(Theme.ACCENT_CYAN);
        passwordField.putClientProperty("JTextField.placeholderText", "Password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        loginButton = Theme.createPrimaryButton("🚀  Authenticate & Enter");
        loginButton.setFont(Theme.bold(13));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.addActionListener(e -> attemptLogin());

        // Enter key submission
        usernameField.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());

        // Demo Quick Credentials helper pills
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        demoPanel.setOpaque(false);
        demoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel demoLbl = new JLabel("Quick Fill:");
        demoLbl.setFont(Theme.font(11));
        demoLbl.setForeground(Theme.TEXT_MUTED);
        demoPanel.add(demoLbl);

        JButton adminPill = createPresetPill("⚡ Admin", "admin", "Admin@123");
        JButton recPill = createPresetPill("📋 Reception", "receptionist", "Staff@123");
        JButton trainerPill = createPresetPill("💪 Trainer", "trainer_alex", "Trainer@123");

        demoPanel.add(adminPill);
        demoPanel.add(recPill);
        demoPanel.add(trainerPill);

        JLabel userLabelPrompt = new JLabel("Staff Username");
        userLabelPrompt.setFont(Theme.bold(11));
        userLabelPrompt.setForeground(Theme.TEXT_SECONDARY);

        JLabel passLabelPrompt = new JLabel("Secure Password");
        passLabelPrompt.setFont(Theme.bold(11));
        passLabelPrompt.setForeground(Theme.TEXT_SECONDARY);

        rightForm.add(loginHeader);
        rightForm.add(Box.createVerticalStrut(4));
        rightForm.add(loginSub);
        rightForm.add(Box.createVerticalStrut(8));
        rightForm.add(errorLabel);
        rightForm.add(Box.createVerticalStrut(6));
        rightForm.add(userLabelPrompt);
        rightForm.add(Box.createVerticalStrut(4));
        rightForm.add(usernameField);
        rightForm.add(Box.createVerticalStrut(12));
        rightForm.add(passLabelPrompt);
        rightForm.add(Box.createVerticalStrut(4));
        rightForm.add(passwordField);
        rightForm.add(Box.createVerticalStrut(18));
        rightForm.add(loginButton);
        rightForm.add(Box.createVerticalStrut(10));
        rightForm.add(demoPanel);

        container.add(leftBanner);
        container.add(rightForm);
        add(container, BorderLayout.CENTER);

        // Pre-fill with admin by default for convenience
        usernameField.setText("admin");
        passwordField.setText("Admin@123");
    }

    private JButton createPresetPill(String title, String user, String pass) {
        JButton b = new JButton(title);
        b.setFont(Theme.bold(10));
        b.setBackground(Theme.BG_INPUT);
        b.setForeground(Theme.TEXT_SECONDARY);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            usernameField.setText(user);
            passwordField.setText(pass);
            errorLabel.setText(" ");
        });
        return b;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            User user = authService.login(username, password);
            if (user != null) {
                authenticated = true;
                dispose();
            }
        } catch (Exception ex) {
            errorLabel.setText(ex.getMessage());
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
