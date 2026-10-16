package com.fitcore.ui.attendance;

import com.fitcore.model.Member;
import com.fitcore.service.AttendanceService;
import com.fitcore.service.MemberService;
import com.fitcore.ui.components.StatusBadge;
import com.fitcore.util.DateTimeUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Quick check-in dialog with instant member search, eligibility checks, and 1-per-day enforcement.
 */
public class CheckInDialog extends JDialog {

    private final AttendanceService attendanceService;
    private final MemberService memberService;
    private boolean checkedIn = false;

    private JTextField searchField;
    private JComboBox<MemberSearchItem> memberResultCombo;
    private JLabel memberDetailsLabel;
    private StatusBadge membershipStatusBadge;
    private JTextField notesField;
    private JButton confirmBtn;

    private static class MemberSearchItem {
        final Member member;

        MemberSearchItem(Member member) {
            this.member = member;
        }

        @Override
        public String toString() {
            return member.getMemberCode() + " - " + member.getFullName() + " (" + member.getPhone() + ")";
        }
    }

    public CheckInDialog(Frame parent, AttendanceService attendanceService, MemberService memberService) {
        super(parent, "Quick Member Check-in", true);
        this.attendanceService = attendanceService;
        this.memberService = memberService;

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

        searchField = new JTextField(22);
        searchField.putClientProperty("JTextField.placeholderText", "Enter Code, Name, or Phone...");

        JButton searchBtn = new JButton("Search Member");
        searchBtn.addActionListener(e -> onSearch());

        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(searchBtn, BorderLayout.EAST);

        memberResultCombo = new JComboBox<>();
        memberResultCombo.addActionListener(e -> onSelectMember());

        memberDetailsLabel = new JLabel("Please search and select a member.");
        memberDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        memberDetailsLabel.setForeground(new Color(71, 85, 105));

        membershipStatusBadge = new StatusBadge("NONE");

        JPanel badgeContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeContainer.setOpaque(false);
        badgeContainer.add(membershipStatusBadge);

        notesField = new JTextField("Gym floor workout session", 22);

        int row = 0;
        addRow(content, gbc, row++, "Search Member:", searchBar);
        addRow(content, gbc, row++, "Matching Members:", memberResultCombo);
        addRow(content, gbc, row++, "Member Overview:", memberDetailsLabel);
        addRow(content, gbc, row++, "Membership Status:", badgeContainer);
        addRow(content, gbc, row++, "Session Notes:", notesField);

        // Prepopulate top 15 active members
        onSearch();

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        buttons.setBackground(new Color(248, 250, 252));
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        confirmBtn = new JButton("Record Check-in");
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        confirmBtn.setBackground(new Color(16, 185, 129)); // emerald-500
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFocusPainted(false);
        confirmBtn.addActionListener(e -> onCheckIn());

        buttons.add(cancelBtn);
        buttons.add(confirmBtn);

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

    private void onSearch() {
        String q = searchField.getText().trim();
        List<Member> list = memberService.searchMembers("ACTIVE", q.isEmpty() ? null : q, null);
        memberResultCombo.removeAllItems();
        for (Member m : list) {
            memberResultCombo.addItem(new MemberSearchItem(m));
        }
        if (memberResultCombo.getItemCount() > 0) {
            memberResultCombo.setSelectedIndex(0);
            onSelectMember();
        } else {
            memberDetailsLabel.setText("No active members found matching query.");
            membershipStatusBadge.setStatus("NONE");
        }
    }

    private void onSelectMember() {
        MemberSearchItem item = (MemberSearchItem) memberResultCombo.getSelectedItem();
        if (item != null) {
            Member m = item.member;
            String plan = m.getCurrentPlanName() != null ? m.getCurrentPlanName() : "No Active Plan";
            String valid = m.getCurrentMembershipEndDate() != null ? DateTimeUtil.formatDate(m.getCurrentMembershipEndDate()) : "N/A";
            memberDetailsLabel.setText(m.getFullName() + " | Plan: " + plan + " (Valid: " + valid + ")");

            String status = m.getCurrentMembershipStatus() != null ? m.getCurrentMembershipStatus() : "NONE";
            membershipStatusBadge.setStatus(status);
        }
    }

    private void onCheckIn() {
        try {
            MemberSearchItem item = (MemberSearchItem) memberResultCombo.getSelectedItem();
            if (item == null) {
                throw new IllegalArgumentException("Please select a member to check in.");
            }

            String notes = notesField.getText().trim();
            attendanceService.checkIn(item.member.getId(), notes);

            checkedIn = true;
            JOptionPane.showMessageDialog(this,
                    "Check-in successfully recorded for " + item.member.getFullName() + "!",
                    "Check-in Complete", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check-in Blocked", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }
}
