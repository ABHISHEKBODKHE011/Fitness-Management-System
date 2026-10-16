package com.fitcore.ui.members;

import com.fitcore.model.Member;
import com.fitcore.model.Trainer;
import com.fitcore.service.MemberService;
import com.fitcore.service.TrainerService;
import com.fitcore.util.DateTimeUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Modal form for registering or updating gym members.
 */
public class MemberFormDialog extends JDialog {

    private final MemberService memberService;
    private final TrainerService trainerService;
    private final Member existingMember;
    private boolean saved = false;

    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JComboBox<String> genderCombo;
    private JTextField emergencyContactField;
    private JTextField joiningDateField;
    private JComboBox<TrainerItem> trainerCombo;
    private JComboBox<String> statusCombo;

    private static class TrainerItem {
        final Integer id;
        final String displayName;

        TrainerItem(Integer id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public MemberFormDialog(Frame parent, MemberService memberService, TrainerService trainerService, Member existingMember) {
        super(parent, existingMember == null ? "Register New Member" : "Edit Member: " + existingMember.getFullName(), true);
        this.memberService = memberService;
        this.trainerService = trainerService;
        this.existingMember = existingMember;

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
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Non-binary / Other", "Prefer not to say"});
        emergencyContactField = new JTextField(20);
        joiningDateField = new JTextField(DateTimeUtil.formatDateIso(LocalDate.now()), 20);

        trainerCombo = new JComboBox<>();
        trainerCombo.addItem(new TrainerItem(null, "-- None (Self-Guided) --"));
        List<Trainer> trainers = trainerService.searchTrainers("ACTIVE", null);
        for (Trainer t : trainers) {
            trainerCombo.addItem(new TrainerItem(t.getId(), t.getName() + " (" + t.getSpecialization() + ")"));
        }

        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        // Populate if editing
        if (existingMember != null) {
            nameField.setText(existingMember.getFullName());
            phoneField.setText(existingMember.getPhone());
            emailField.setText(existingMember.getEmail());
            genderCombo.setSelectedItem(existingMember.getGender());
            emergencyContactField.setText(existingMember.getEmergencyContact() != null ? existingMember.getEmergencyContact() : "");
            joiningDateField.setText(DateTimeUtil.formatDateIso(existingMember.getJoiningDate()));
            statusCombo.setSelectedItem(existingMember.getStatus());

            if (existingMember.getAssignedTrainerId() != null) {
                for (int i = 0; i < trainerCombo.getItemCount(); i++) {
                    TrainerItem item = trainerCombo.getItemAt(i);
                    if (item.id != null && item.id.equals(existingMember.getAssignedTrainerId())) {
                        trainerCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        int row = 0;
        addRow(content, gbc, row++, "Full Name *:", nameField);
        addRow(content, gbc, row++, "Phone Number *:", phoneField);
        addRow(content, gbc, row++, "Email Address *:", emailField);
        addRow(content, gbc, row++, "Gender *:", genderCombo);
        addRow(content, gbc, row++, "Emergency Contact:", emergencyContactField);
        addRow(content, gbc, row++, "Joining Date (YYYY-MM-DD) *:", joiningDateField);
        addRow(content, gbc, row++, "Assigned Trainer:", trainerCombo);
        addRow(content, gbc, row++, "Account Status:", statusCombo);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        buttonPanel.setBackground(new Color(248, 250, 252));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingMember == null ? "Register Member" : "Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setBackground(new Color(37, 99, 235));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> onSave());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(content, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(51, 65, 85));
        p.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(comp, gbc);
    }

    private void onSave() {
        try {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String gender = (String) genderCombo.getSelectedItem();
            String emergency = emergencyContactField.getText().trim();
            LocalDate joiningDate = DateTimeUtil.parseDate(joiningDateField.getText().trim());
            TrainerItem selectedTrainer = (TrainerItem) trainerCombo.getSelectedItem();
            String status = (String) statusCombo.getSelectedItem();

            Member m = existingMember != null ? existingMember : new Member();
            m.setFullName(name);
            m.setPhone(phone);
            m.setEmail(email);
            m.setGender(gender);
            m.setEmergencyContact(emergency.isEmpty() ? null : emergency);
            m.setJoiningDate(joiningDate);
            m.setAssignedTrainerId(selectedTrainer != null ? selectedTrainer.id : null);
            m.setStatus(status);

            if (existingMember == null) {
                memberService.registerMember(m);
            } else {
                memberService.updateMember(m);
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
