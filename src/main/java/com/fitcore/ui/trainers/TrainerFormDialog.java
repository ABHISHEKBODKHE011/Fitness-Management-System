package com.fitcore.ui.trainers;

import com.fitcore.model.Trainer;
import com.fitcore.service.TrainerService;

import javax.swing.*;
import java.awt.*;

/**
 * Modal form for registering or updating certified fitness trainers.
 */
public class TrainerFormDialog extends JDialog {

    private final TrainerService trainerService;
    private final Trainer existingTrainer;
    private boolean saved = false;

    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JComboBox<String> specializationCombo;
    private JComboBox<String> statusCombo;

    public TrainerFormDialog(Frame parent, TrainerService trainerService, Trainer existingTrainer) {
        super(parent, existingTrainer == null ? "Add Certified Trainer" : "Edit Trainer: " + existingTrainer.getName(), true);
        this.trainerService = trainerService;
        this.existingTrainer = existingTrainer;

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        nameField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);

        String[] specs = {
                "CrossFit & Functional Strength",
                "HIIT & Cardiovascular Conditioning",
                "Powerlifting & Heavy Compound Movement",
                "Bodybuilding & Hypertrophy",
                "Yoga, Mobility & Core Flexibility",
                "Physical Rehabilitation & Posture Corrective",
                "General Fitness & Lifestyle Coaching"
        };
        specializationCombo = new JComboBox<>(specs);
        specializationCombo.setEditable(true);

        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        if (existingTrainer != null) {
            nameField.setText(existingTrainer.getName());
            phoneField.setText(existingTrainer.getPhone());
            emailField.setText(existingTrainer.getEmail());
            specializationCombo.setSelectedItem(existingTrainer.getSpecialization());
            statusCombo.setSelectedItem(existingTrainer.getStatus());
        }

        int row = 0;
        addRow(content, gbc, row++, "Trainer Full Name *:", nameField);
        addRow(content, gbc, row++, "Phone Number *:", phoneField);
        addRow(content, gbc, row++, "Email Address *:", emailField);
        addRow(content, gbc, row++, "Specialization *:", specializationCombo);
        addRow(content, gbc, row++, "Employment Status:", statusCombo);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        buttons.setBackground(new Color(248, 250, 252));
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingTrainer == null ? "Register Trainer" : "Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setBackground(new Color(37, 99, 235));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> onSave());

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(51, 65, 85));
        p.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(comp, gbc);
    }

    private void onSave() {
        try {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String spec = (String) specializationCombo.getSelectedItem();
            String status = (String) statusCombo.getSelectedItem();

            Trainer t = existingTrainer != null ? existingTrainer : new Trainer();
            t.setName(name);
            t.setPhone(phone);
            t.setEmail(email);
            t.setSpecialization(spec);
            t.setStatus(status);

            if (existingTrainer == null) {
                trainerService.registerTrainer(t);
            } else {
                trainerService.updateTrainer(t);
            }

            saved = true;
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
