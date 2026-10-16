package com.fitcore.ui;

import com.fitcore.config.AppConfig;
import com.fitcore.model.User;
import com.fitcore.service.AuthService;

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
        super(parent, "FitCore Pro - System Authentication", true);
        this.authService = authService;

        setSize(720, 440);
        setResizable(false);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel container = new JPanel(new GridLayout(1, 2));

        // Left Branding Panel
        JPanel leftBanner = new JPanel();
        leftBanner.setBackground(new Color(15, 23, 42)); // deep navy
        leftBanner.setLayout(new BoxLayout(leftBanner, BoxLayout.Y_AXIS));
        leftBanner.setBorder(BorderFactory.createEmptyBorder(40, 32, 40, 32));

        JLabel logoLbl = new JLabel("⚡ FITCORE PRO");
        logoLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoLbl.setForeground(Color.WHITE);

        JLabel tagLbl = new JLabel("Advanced Fitness Management System");
        tagLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tagLbl.setForeground(new Color(96, 165, 250)); // blue-400

        JLabel descLbl = new JLabel("<html><br><br>• Secure Role-Based Access Control<br>• Real-time MySQL 8.x Analytics<br>• Member 360° Profiles & History<br>• Attendance & Check-in Enforcement<br>• Automated Financial Billing Ledger</html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLbl.setForeground(new Color(203, 213, 225));

        leftBanner.add(logoLbl);
        leftBanner.add(Box.createVerticalStrut(6));
        leftBanner.add(tagLbl);
        leftBanner.add(descLbl);

        // Right Form Panel
        JPanel rightForm = new JPanel();
        rightForm.setBackground(Color.WHITE);
        rightForm.setLayout(new BoxLayout(rightForm, BoxLayout.Y_AXIS));
        rightForm.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));

        JLabel loginHeader = new JLabel("Welcome Back");
        loginHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        loginHeader.setForeground(new Color(15, 23, 42));

        JLabel loginSub = new JLabel("Please enter your staff credentials to continue");
        loginSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loginSub.setForeground(new Color(100, 116, 139));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        errorLabel.setForeground(new Color(239, 68, 68)); // red-500

        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameField.putClientProperty("JTextField.placeholderText", "Username (e.g. admin)");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.putClientProperty("JTextField.placeholderText", "Password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        loginButton = new JButton("Sign In to System");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginButton.setBackground(new Color(37, 99, 235));
        loginButton.setForeground(Color.WHITE);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> attemptLogin());

        // Enter key submission
        usernameField.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());

        // Demo Quick Credentials helper pills for college presentation
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        demoPanel.setOpaque(false);
        demoPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JLabel demoLbl = new JLabel("Demo Roles: ");
        demoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        demoLbl.setForeground(new Color(100, 116, 139));
        demoPanel.add(demoLbl);

        JButton adminPill = createPresetPill("Admin", "admin", "Admin@123");
        JButton recPill = createPresetPill("Reception", "reception", "Reception@123");
        JButton trainerPill = createPresetPill("Trainer", "trainer_mike", "Trainer@123");

        demoPanel.add(adminPill);
        demoPanel.add(recPill);
        demoPanel.add(trainerPill);

        rightForm.add(loginHeader);
        rightForm.add(Box.createVerticalStrut(4));
        rightForm.add(loginSub);
        rightForm.add(Box.createVerticalStrut(10));
        rightForm.add(errorLabel);
        rightForm.add(Box.createVerticalStrut(8));
        rightForm.add(new JLabel("Username:"));
        rightForm.add(Box.createVerticalStrut(4));
        rightForm.add(usernameField);
        rightForm.add(Box.createVerticalStrut(12));
        rightForm.add(new JLabel("Password:"));
        rightForm.add(Box.createVerticalStrut(4));
        rightForm.add(passwordField);
        rightForm.add(Box.createVerticalStrut(18));
        rightForm.add(loginButton);
        rightForm.add(Box.createVerticalStrut(10));
        rightForm.add(demoPanel);

        container.add(leftBanner);
        container.add(rightForm);
        add(container, BorderLayout.CENTER);

        // Pre-fill with admin by default for easy local start
        usernameField.setText("admin");
        passwordField.setText("Admin@123");
    }

    private JButton createPresetPill(String title, String user, String pass) {
        JButton b = new JButton(title);
        b.setFont(new Font("Segoe UI", Font.BOLD, 10));
        b.setBackground(new Color(241, 245, 249));
        b.setForeground(new Color(51, 65, 85));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
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
