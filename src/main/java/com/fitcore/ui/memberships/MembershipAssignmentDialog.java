package com.fitcore.ui.memberships;

import com.fitcore.model.Member;
import com.fitcore.model.MembershipPlan;
import com.fitcore.service.MemberService;
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
 * Dialog for assigning a membership plan to a member with auto-calculated inclusive validity dates.
 */
public class MembershipAssignmentDialog extends JDialog {

    private final MembershipService membershipService;
    private final MembershipPlanService planService;
    private final MemberService memberService;
    private final Integer preselectedMemberId;
    private boolean assigned = false;

    private JComboBox<MemberItem> memberCombo;
    private JComboBox<PlanItem> planCombo;
    private JTextField startDateField;
    private JLabel calculatedEndDateLabel;
    private JTextField agreedFeeField;
    private JTextArea notesArea;

    private static class MemberItem {
        final Integer id;
        final String name;

        MemberItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

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

    public MembershipAssignmentDialog(Frame parent,
                                      MembershipService membershipService,
                                      MembershipPlanService planService,
                                      MemberService memberService,
                                      Integer preselectedMemberId) {
        super(parent, "Assign Membership Plan", true);
        this.membershipService = membershipService;
        this.planService = planService;
        this.memberService = memberService;
        this.preselectedMemberId = preselectedMemberId;

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

        memberCombo = new JComboBox<>();
        List<Member> members = memberService.searchMembers("ACTIVE", null, null);
        for (Member m : members) {
            MemberItem item = new MemberItem(m.getId(), m.getMemberCode() + " - " + m.getFullName());
            memberCombo.addItem(item);
            if (preselectedMemberId != null && preselectedMemberId.equals(m.getId())) {
                memberCombo.setSelectedItem(item);
            }
        }

        planCombo = new JComboBox<>();
        List<MembershipPlan> plans = planService.getAllPlans(true, null);
        for (MembershipPlan p : plans) {
            planCombo.addItem(new PlanItem(p));
        }

        startDateField = new JTextField(DateTimeUtil.formatDateIso(LocalDate.now()), 15);
        calculatedEndDateLabel = new JLabel("-");
        calculatedEndDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        calculatedEndDateLabel.setForeground(new Color(37, 99, 235));

        agreedFeeField = new JTextField(15);
        notesArea = new JTextArea(3, 20);
        notesArea.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Listener to auto-calculate end date and fee
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
                    calculatedEndDateLabel.setText("Invalid start date");
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
        addRow(content, gbc, row++, "Select Member *:", memberCombo);
        addRow(content, gbc, row++, "Membership Plan *:", planCombo);
        addRow(content, gbc, row++, "Start Date (YYYY-MM-DD) *:", startDateField);
        addRow(content, gbc, row++, "Calculated End Date:", calculatedEndDateLabel);
        addRow(content, gbc, row++, "Agreed Fee ($) *:", agreedFeeField);
        addRow(content, gbc, row++, "Notes:", new JScrollPane(notesArea));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        buttons.setBackground(new Color(248, 250, 252));
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.addActionListener(e -> dispose());

        JButton assignBtn = new JButton("Assign Membership");
        assignBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        assignBtn.setBackground(new Color(37, 99, 235));
        assignBtn.setForeground(Color.WHITE);
        assignBtn.setFocusPainted(false);
        assignBtn.addActionListener(e -> onAssign());

        buttons.add(cancelBtn);
        buttons.add(assignBtn);

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

    private void onAssign() {
        try {
            MemberItem memberItem = (MemberItem) memberCombo.getSelectedItem();
            if (memberItem == null) throw new IllegalArgumentException("Please select a member.");

            PlanItem planItem = (PlanItem) planCombo.getSelectedItem();
            if (planItem == null) throw new IllegalArgumentException("Please select a plan.");

            LocalDate start = DateTimeUtil.parseDate(startDateField.getText().trim());
            if (start == null) throw new IllegalArgumentException("Please enter a valid start date (YYYY-MM-DD).");

            BigDecimal fee = new BigDecimal(agreedFeeField.getText().trim());
            String notes = notesArea.getText().trim();

            membershipService.assignMembership(memberItem.id, planItem.plan.getId(), start, fee, notes);
            assigned = true;
            JOptionPane.showMessageDialog(this, "Membership successfully assigned!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Assignment Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isAssigned() {
        return assigned;
    }
}
