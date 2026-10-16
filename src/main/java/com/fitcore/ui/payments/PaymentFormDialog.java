package com.fitcore.ui.payments;

import com.fitcore.model.Member;
import com.fitcore.model.Membership;
import com.fitcore.model.Payment;
import com.fitcore.service.MemberService;
import com.fitcore.service.MembershipService;
import com.fitcore.service.PaymentService;
import com.fitcore.util.CurrencyUtil;
import com.fitcore.util.DateTimeUtil;
import com.fitcore.util.ReceiptPrinter;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Modal form for recording payments with mode selection, unique references, and receipt generation.
 */
public class PaymentFormDialog extends JDialog {

    private final PaymentService paymentService;
    private final MemberService memberService;
    private final MembershipService membershipService;
    private final Integer preselectedMemberId;
    private boolean saved = false;

    private JComboBox<MemberItem> memberCombo;
    private JComboBox<MembershipItem> membershipCombo;
    private JTextField amountField;
    private JComboBox<String> methodCombo;
    private JComboBox<String> statusCombo;
    private JTextField referenceField;
    private JTextArea notesArea;
    private JCheckBox printReceiptCheckbox;

    private static class MemberItem {
        final Member member;

        MemberItem(Member member) {
            this.member = member;
        }

        @Override
        public String toString() {
            return member.getMemberCode() + " - " + member.getFullName();
        }
    }

    private static class MembershipItem {
        final Membership membership;

        MembershipItem(Membership membership) {
            this.membership = membership;
        }

        @Override
        public String toString() {
            if (membership == null) return "-- None (General Payment) --";
            return membership.getMembershipCode() + " (" + membership.getPlanName() + " - Fee: " + CurrencyUtil.format(membership.getAgreedFee()) + ")";
        }
    }

    public PaymentFormDialog(Frame parent,
                             PaymentService paymentService,
                             MemberService memberService,
                             MembershipService membershipService,
                             Integer preselectedMemberId) {
        super(parent, "Record Billing Payment", true);
        this.paymentService = paymentService;
        this.memberService = memberService;
        this.membershipService = membershipService;
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
            MemberItem item = new MemberItem(m);
            memberCombo.addItem(item);
            if (preselectedMemberId != null && preselectedMemberId.equals(m.getId())) {
                memberCombo.setSelectedItem(item);
            }
        }

        membershipCombo = new JComboBox<>();
        amountField = new JTextField(15);
        methodCombo = new JComboBox<>(new String[]{"CASH", "UPI", "CARD", "BANK_TRANSFER"});
        statusCombo = new JComboBox<>(new String[]{"SUCCESS", "PENDING", "FAILED"});
        referenceField = new JTextField(20);
        notesArea = new JTextArea(2, 20);
        notesArea.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        printReceiptCheckbox = new JCheckBox("Display and Print Official Receipt Immediately", true);
        printReceiptCheckbox.setOpaque(false);

        memberCombo.addActionListener(e -> updateMembershipsForSelectedMember());
        membershipCombo.addActionListener(e -> {
            MembershipItem item = (MembershipItem) membershipCombo.getSelectedItem();
            if (item != null && item.membership != null) {
                amountField.setText(item.membership.getAgreedFee().toPlainString());
            }
        });

        updateMembershipsForSelectedMember();

        int row = 0;
        addRow(content, gbc, row++, "Select Member *:", memberCombo);
        addRow(content, gbc, row++, "Related Membership:", membershipCombo);
        addRow(content, gbc, row++, "Payment Amount ($) *:", amountField);
        addRow(content, gbc, row++, "Payment Method *:", methodCombo);
        addRow(content, gbc, row++, "Transaction Status *:", statusCombo);
        addRow(content, gbc, row++, "Auth / Reference Code:", referenceField);
        addRow(content, gbc, row++, "Notes / Cashier Memo:", new JScrollPane(notesArea));
        addRow(content, gbc, row++, "", printReceiptCheckbox);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        buttons.setBackground(com.fitcore.ui.theme.Theme.BG_CARD);
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, com.fitcore.ui.theme.Theme.BORDER_SUBTLE));

        JButton cancelBtn = com.fitcore.ui.theme.Theme.createGhostButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = com.fitcore.ui.theme.Theme.createPrimaryButton("💳  Record Payment");
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

    private void updateMembershipsForSelectedMember() {
        MemberItem item = (MemberItem) memberCombo.getSelectedItem();
        membershipCombo.removeAllItems();
        membershipCombo.addItem(new MembershipItem(null));
        if (item != null) {
            List<Membership> list = membershipService.getMembershipsForMember(item.member.getId());
            for (Membership ms : list) {
                membershipCombo.addItem(new MembershipItem(ms));
            }
            if (!list.isEmpty()) {
                membershipCombo.setSelectedIndex(1); // Select most recent
            }
        }
    }

    private void onSave() {
        try {
            MemberItem memberItem = (MemberItem) memberCombo.getSelectedItem();
            if (memberItem == null) throw new IllegalArgumentException("Please select a member.");

            String amtStr = amountField.getText().trim();
            if (amtStr.isEmpty()) throw new IllegalArgumentException("Amount is required.");
            BigDecimal amount = new BigDecimal(amtStr);

            String method = (String) methodCombo.getSelectedItem();
            String status = (String) statusCombo.getSelectedItem();
            String ref = referenceField.getText().trim();
            String notes = notesArea.getText().trim();

            MembershipItem msItem = (MembershipItem) membershipCombo.getSelectedItem();
            Integer msId = (msItem != null && msItem.membership != null) ? msItem.membership.getId() : null;

            Payment p = new Payment();
            p.setMemberId(memberItem.member.getId());
            p.setMembershipId(msId);
            p.setAmount(amount);
            p.setPaymentMethod(method);
            p.setPaymentStatus(status);
            p.setTransactionReference(ref.isEmpty() ? null : ref);
            p.setReceiptNotes(notes.isEmpty() ? null : notes);
            p.setPaymentDate(LocalDateTime.now());

            Payment recorded = paymentService.recordPayment(p);
            saved = true;

            if (printReceiptCheckbox.isSelected()) {
                Membership ms = msItem != null ? msItem.membership : null;
                ReceiptPrinter.showReceiptDialog(this, recorded, memberItem.member, ms);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Payment " + recorded.getPaymentCode() + " recorded successfully!",
                        "Payment Saved", JOptionPane.INFORMATION_MESSAGE);
            }

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Payment Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
