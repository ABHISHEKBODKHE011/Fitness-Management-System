package com.fitcore.ui.memberships;

import com.fitcore.model.Membership;
import com.fitcore.model.MembershipPlan;
import com.fitcore.service.MembershipPlanService;
import com.fitcore.service.MembershipService;
import com.fitcore.util.CurrencyUtil;
import com.fitcore.util.DateTimeUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Dialog for renewing an expiring or expired membership while preserving the previous historical record.
 */
public class MembershipRenewalDialog extends JDialog {

    private final MembershipService membershipService;
    private final MembershipPlanService planService;
    private final Membership previousMembership;
    private boolean renewed = false;

    private JComboBox<PlanItem> planCombo;
    private JTextField startDateField;
    private JLabel calculatedEndDateLabel;
    private JTextField agreedFeeField;
    private JTextArea notesArea;

    private static class PlanItem {
        final MembershipPlan plan;

        PlanItem(MembershipPlan plan) {
            this.plan = plan;
        }

        @Override
        public String toString() {
            return plan.getName() + " (" + plan.getDurationDays() + " days - " + CurrencyUtil.format(plan.getFee()) + ")";
        }
    }

    public MembershipRenewalDialog(Frame parent,
                                   MembershipService membershipService,
                                   MembershipPlanService planService,
                                   Membership previousMembership) {
        super(parent, "Renew Membership: " + previousMembership.getMemberName() + " (" + previousMembership.getMembershipCode() + ")", true);
        this.membershipService = membershipService;
        this.planService = planService;
        this.previousMembership = previousMembership;

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

        // Calculate default renewal start date
        LocalDate defaultStart;
        LocalDate today = LocalDate.now();
        if (previousMembership.getEndDate() != null && !previousMembership.getEndDate().isBefore(today)) {
            defaultStart = previousMembership.getEndDate().plusDays(1);
        } else {
            defaultStart = today;
        }

        planCombo = new JComboBox<>();
        List<MembershipPlan> plans = planService.getAllPlans(true, null);
        for (MembershipPlan p : plans) {
            PlanItem item = new PlanItem(p);
            planCombo.addItem(item);
            if (p.getId().equals(previousMembership.getPlanId())) {
                planCombo.setSelectedItem(item);
            }
        }

        startDateField = new JTextField(DateTimeUtil.formatDateIso(defaultStart), 15);
        calculatedEndDateLabel = new JLabel("-");
        calculatedEndDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        calculatedEndDateLabel.setForeground(new Color(16, 185, 129));

        agreedFeeField = new JTextField(15);
        notesArea = new JTextArea(3, 20);
        notesArea.setText("Renewed from " + previousMembership.getMembershipCode());
        notesArea.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        Runnable updateCalculations = () -> {
            PlanItem selected = (PlanItem) planCombo.getSelectedItem();
            if (selected != null) {
                try {
                    LocalDate start = DateTimeUtil.parseDate(startDateField.getText().trim());
                    if (start != null) {
                        LocalDate end = DateTimeUtil.calculateEndDate(start, selected.plan.getDurationDays());
                        calculatedEndDateLabel.setText(DateTimeUtil.formatDate(end) + " (Inclusive)");
                    }
                } catch (Exception ex) {
                    calculatedEndDateLabel.setText("Invalid date");
                }
                if (agreedFeeField.getText().trim().isEmpty()) {
                    agreedFeeField.setText(selected.plan.getFee().toPlainString());
                }
            }
        };

        planCombo.addActionListener(e -> {
            PlanItem selected = (PlanItem) planCombo.getSelectedItem();
            if (selected != null) {
                agreedFeeField.setText(selected.plan.getFee().toPlainString());
            }
            updateCalculations.run();
        });

        startDateField.addActionListener(e -> updateCalculations.run());
        updateCalculations.run();

        int row = 0;
        addRow(content, gbc, row++, "Member:", new JLabel(previousMembership.getMemberName() + " (" + previousMembership.getMemberCode() + ")"));
        addRow(content, gbc, row++, "Prior Membership:", new JLabel(previousMembership.getMembershipCode() + " (Ended: " + DateTimeUtil.formatDate(previousMembership.getEndDate()) + ")"));
        addRow(content, gbc, row++, "Select Plan *:", planCombo);
        addRow(content, gbc, row++, "Renewal Start Date *:", startDateField);
        addRow(content, gbc, row++, "Calculated End Date:", calculatedEndDateLabel);
        addRow(content, gbc, row++, "Agreed Fee ($) *:", agreedFeeField);
        addRow(content, gbc, row++, "Renewal Notes:", new JScrollPane(notesArea));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        buttons.setBackground(com.fitcore.ui.theme.Theme.BG_CARD);
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, com.fitcore.ui.theme.Theme.BORDER_SUBTLE));

        JButton cancelBtn = com.fitcore.ui.theme.Theme.createGhostButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton renewBtn = com.fitcore.ui.theme.Theme.createSuccessButton("⚡  Confirm Renewal");
        renewBtn.addActionListener(e -> onRenew());

        buttons.add(cancelBtn);
        buttons.add(renewBtn);

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

    private void onRenew() {
        try {
            PlanItem planItem = (PlanItem) planCombo.getSelectedItem();
            if (planItem == null) throw new IllegalArgumentException("Please select a plan.");

            LocalDate start = DateTimeUtil.parseDate(startDateField.getText().trim());
            if (start == null) throw new IllegalArgumentException("Please enter a valid start date (YYYY-MM-DD).");

            BigDecimal fee = new BigDecimal(agreedFeeField.getText().trim());
            String notes = notesArea.getText().trim();

            membershipService.renewMembership(previousMembership.getId(), planItem.plan.getId(), start, fee, notes);
            renewed = true;
            JOptionPane.showMessageDialog(this, "Membership renewed successfully!", "Renewal Completed", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Renewal Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isRenewed() {
        return renewed;
    }
}
