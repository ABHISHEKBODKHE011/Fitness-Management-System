package com.fitcore.ui.memberships;

import com.fitcore.model.MembershipPlan;
import com.fitcore.service.MembershipPlanService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Modal form for creating and editing gym membership plans.
 */
public class PlanFormDialog extends JDialog {

    private final MembershipPlanService planService;
    private final MembershipPlan existingPlan;
    private boolean saved = false;

    private JTextField nameField;
    private JSpinner durationSpinner;
    private JTextField feeField;
    private JTextArea descriptionArea;
    private JComboBox<String> statusCombo;

    public PlanFormDialog(Frame parent, MembershipPlanService planService, MembershipPlan existingPlan) {
        super(parent, existingPlan == null ? "Create Membership Plan" : "Edit Plan: " + existingPlan.getName(), true);
        this.planService = planService;
        this.existingPlan = existingPlan;

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
        durationSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 3650, 1));
        feeField = new JTextField("49.99", 20);

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        if (existingPlan != null) {
            nameField.setText(existingPlan.getName());
            durationSpinner.setValue(existingPlan.getDurationDays());
            feeField.setText(existingPlan.getFee().toPlainString());
            descriptionArea.setText(existingPlan.getDescription() != null ? existingPlan.getDescription() : "");
            statusCombo.setSelectedItem(existingPlan.getStatus());
        }

        int row = 0;
        addRow(content, gbc, row++, "Plan Name *:", nameField);
        addRow(content, gbc, row++, "Duration (Days) *:", durationSpinner);
        addRow(content, gbc, row++, "Membership Fee ($) *:", feeField);
        addRow(content, gbc, row++, "Plan Description:", new JScrollPane(descriptionArea));
        addRow(content, gbc, row++, "Catalog Status:", statusCombo);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        buttons.setBackground(new Color(248, 250, 252));
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingPlan == null ? "Create Plan" : "Save Plan");
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
            int duration = (Integer) durationSpinner.getValue();
            BigDecimal fee = new BigDecimal(feeField.getText().trim());
            String desc = descriptionArea.getText().trim();
            String status = (String) statusCombo.getSelectedItem();

            MembershipPlan p = existingPlan != null ? existingPlan : new MembershipPlan();
            p.setName(name);
            p.setDurationDays(duration);
            p.setFee(fee);
            p.setDescription(desc.isEmpty() ? null : desc);
            p.setStatus(status);

            if (existingPlan == null) {
                planService.createPlan(p);
            } else {
                planService.updatePlan(p);
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
