package com.fitcore.ui.members;

import com.fitcore.model.*;
import com.fitcore.service.AttendanceService;
import com.fitcore.service.MembershipService;
import com.fitcore.service.PaymentService;
import com.fitcore.ui.components.ModernTable;
import com.fitcore.ui.components.StatusBadge;
import com.fitcore.util.CurrencyUtil;
import com.fitcore.util.DateTimeUtil;
import com.fitcore.util.ReceiptPrinter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 360-degree comprehensive member profile dialog with tabbed membership, attendance, and billing history.
 */
public class MemberDetailDialog extends JDialog {

    private final Member member;
    private final MembershipService membershipService;
    private final AttendanceService attendanceService;
    private final PaymentService paymentService;

    public MemberDetailDialog(Frame parent, Member member,
                              MembershipService membershipService,
                              AttendanceService attendanceService,
                              PaymentService paymentService) {
        super(parent, "Member Profile: " + member.getFullName() + " (" + member.getMemberCode() + ")", true);
        this.member = member;
        this.membershipService = membershipService;
        this.attendanceService = attendanceService;
        this.paymentService = paymentService;

        setSize(800, 580);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(createProfileHeader(), BorderLayout.NORTH);
        add(createTabs(), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        footer.setBackground(com.fitcore.ui.theme.Theme.BG_CARD);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, com.fitcore.ui.theme.Theme.BORDER_SUBTLE));
        JButton closeBtn = com.fitcore.ui.theme.Theme.createGhostButton("Close Profile");
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createProfileHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(com.fitcore.ui.theme.Theme.BG_HEADER);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, com.fitcore.ui.theme.Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(18, 24, 18, 24)
        ));

        JPanel left = new JPanel(new GridLayout(2, 2, 16, 6));
        left.setOpaque(false);

        JLabel nameLbl = new JLabel(member.getFullName());
        nameLbl.setFont(com.fitcore.ui.theme.Theme.bold(20));
        nameLbl.setForeground(com.fitcore.ui.theme.Theme.TEXT_PRIMARY);

        JLabel codeLbl = new JLabel("ID: " + member.getMemberCode() + " • Joined: " + DateTimeUtil.formatDate(member.getJoiningDate()));
        codeLbl.setFont(com.fitcore.ui.theme.Theme.font(12));
        codeLbl.setForeground(com.fitcore.ui.theme.Theme.TEXT_SECONDARY);
        codeLbl.setForeground(new Color(100, 116, 139));

        JLabel contactLbl = new JLabel("📞 " + member.getPhone() + "  |  ✉ " + member.getEmail());
        contactLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contactLbl.setForeground(new Color(71, 85, 105));

        String trainerStr = member.getAssignedTrainerName() != null ? member.getAssignedTrainerName() : "None";
        JLabel trainerLbl = new JLabel("🏋 Trainer: " + trainerStr + "  |  🚑 Emergency: " + (member.getEmergencyContact() != null ? member.getEmergencyContact() : "N/A"));
        trainerLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        trainerLbl.setForeground(new Color(71, 85, 105));

        left.add(nameLbl);
        left.add(contactLbl);
        left.add(codeLbl);
        left.add(trainerLbl);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        right.setOpaque(false);
        StatusBadge statusBadge = new StatusBadge(member.getStatus());
        right.add(statusBadge);

        panel.add(left, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabs.addTab("Membership History", createMembershipsTab());
        tabs.addTab("Attendance Logs", createAttendanceTab());
        tabs.addTab("Payment History", createPaymentsTab());

        return tabs;
    }

    private JPanel createMembershipsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Code", "Plan", "Start Date", "End Date", "Fee", "Status", "Notes"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Membership> list = membershipService.getMembershipsForMember(member.getId());
        for (Membership ms : list) {
            model.addRow(new Object[]{
                    ms.getMembershipCode(),
                    ms.getPlanName(),
                    DateTimeUtil.formatDate(ms.getStartDate()),
                    DateTimeUtil.formatDate(ms.getEndDate()),
                    CurrencyUtil.format(ms.getAgreedFee()),
                    ms.getStatus(),
                    ms.getNotes() != null ? ms.getNotes() : ""
            });
        }

        ModernTable table = new ModernTable(model);
        p.add(table.createScrollPane(), BorderLayout.CENTER);
        return p;
    }

    private JPanel createAttendanceTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Check-in Date", "Check-in Time", "Membership Status", "Notes"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Attendance> list = attendanceService.getMemberAttendanceHistory(member.getId());
        for (Attendance a : list) {
            model.addRow(new Object[]{
                    DateTimeUtil.formatDate(a.getCheckInDate()),
                    a.getCheckInTime().format(DateTimeUtil.TIME_FORMATTER),
                    a.getMembershipStatus(),
                    a.getNotes() != null ? a.getNotes() : ""
            });
        }

        ModernTable table = new ModernTable(model);
        p.add(table.createScrollPane(), BorderLayout.CENTER);
        return p;
    }

    private JPanel createPaymentsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Receipt Code", "Amount", "Method", "Date", "Status", "Reference"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Payment> list = paymentService.getPaymentsForMember(member.getId());
        for (Payment pay : list) {
            model.addRow(new Object[]{
                    pay.getPaymentCode(),
                    CurrencyUtil.format(pay.getAmount()),
                    pay.getPaymentMethod(),
                    DateTimeUtil.formatDateTime(pay.getPaymentDate()),
                    pay.getPaymentStatus(),
                    pay.getTransactionReference() != null ? pay.getTransactionReference() : "-"
            });
        }

        ModernTable table = new ModernTable(model);
        p.add(table.createScrollPane(), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton receiptBtn = new JButton("View Receipt Details");
        receiptBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        receiptBtn.setBackground(new Color(15, 23, 42));
        receiptBtn.setForeground(Color.WHITE);
        receiptBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a payment record from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Payment selectedPayment = list.get(row);
            Membership ms = null;
            if (selectedPayment.getMembershipId() != null) {
                List<Membership> msList = membershipService.getMembershipsForMember(member.getId());
                for (Membership m : msList) {
                    if (m.getId().equals(selectedPayment.getMembershipId())) {
                        ms = m;
                        break;
                    }
                }
            }
            ReceiptPrinter.showReceiptDialog(this, selectedPayment, member, ms);
        });

        bottom.add(receiptBtn);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }
}
