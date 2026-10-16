package com.fitcore.ui.reports;

import com.fitcore.dao.PaymentDao;
import com.fitcore.dao.TrainerDao;
import com.fitcore.model.Membership;
import com.fitcore.model.Payment;
import com.fitcore.model.Trainer;
import com.fitcore.service.AttendanceService;
import com.fitcore.service.MembershipPlanService;
import com.fitcore.service.MembershipService;
import com.fitcore.ui.components.ModernTable;
import com.fitcore.ui.components.StatusBadge;
import com.fitcore.ui.memberships.MembershipRenewalDialog;
import com.fitcore.util.CurrencyUtil;
import com.fitcore.util.DateTimeUtil;
import com.fitcore.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Enterprise reporting center providing tabular reports, expiration tracking, and CSV exports.
 */
public class ReportPanel extends JPanel {

    private final MembershipService membershipService;
    private final MembershipPlanService planService;
    private final AttendanceService attendanceService;
    private final TrainerDao trainerDao = new TrainerDao();
    private final PaymentDao paymentDao = new PaymentDao();

    public ReportPanel(MembershipService membershipService,
                       MembershipPlanService planService,
                       AttendanceService attendanceService) {
        this.membershipService = membershipService;
        this.planService = planService;
        this.attendanceService = attendanceService;

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JLabel title = new JLabel("Reports & Analytics Center");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabs.addTab("Expiring Memberships", createExpiringSoonTab());
        tabs.addTab("Active vs Expired Subscriptions", createActiveExpiredTab());
        tabs.addTab("Trainer Allocation Roster", createTrainerAllocationTab());
        tabs.addTab("Revenue Breakdown by Method", createRevenueBreakdownTab());
        tabs.addTab("Daily Attendance Frequency", createAttendanceFrequencyTab());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createExpiringSoonTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JComboBox<String> daysCombo = new JComboBox<>(new String[]{"7 Days", "14 Days", "30 Days"});
        JButton refreshBtn = new JButton("Run Report");
        JButton renewBtn = new JButton("Renew Selected Member");
        renewBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        renewBtn.setBackground(new Color(16, 185, 129));
        renewBtn.setForeground(Color.WHITE);

        JButton exportBtn = new JButton("Export CSV");

        top.add(new JLabel("Expiring Within:"));
        top.add(daysCombo);
        top.add(refreshBtn);
        top.add(renewBtn);
        top.add(exportBtn);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Member Code", "Member Name", "Plan Name", "End Date", "Days Remaining", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        ModernTable table = new ModernTable(model);
        p.add(top, BorderLayout.NORTH);
        p.add(table.createScrollPane(), BorderLayout.CENTER);

        Runnable load = () -> {
            int days = daysCombo.getSelectedIndex() == 0 ? 7 : (daysCombo.getSelectedIndex() == 1 ? 14 : 30);
            List<Membership> list = membershipService.getExpiringSoon(days);
            model.setRowCount(0);
            for (Membership ms : list) {
                long remaining = DateTimeUtil.daysBetween(LocalDate.now(), ms.getEndDate());
                model.addRow(new Object[]{
                        ms.getMemberCode(),
                        ms.getMemberName(),
                        ms.getPlanName(),
                        DateTimeUtil.formatDate(ms.getEndDate()),
                        remaining + " days",
                        ms.getStatus()
                });
            }
        };

        refreshBtn.addActionListener(e -> load.run());
        daysCombo.addActionListener(e -> load.run());
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCsv(this, table, "FitCore_Expiring_Memberships_Report"));

        renewBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an expiring member to renew.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int days = daysCombo.getSelectedIndex() == 0 ? 7 : (daysCombo.getSelectedIndex() == 1 ? 14 : 30);
            List<Membership> list = membershipService.getExpiringSoon(days);
            if (row < list.size()) {
                Membership ms = list.get(row);
                Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
                MembershipRenewalDialog dialog = new MembershipRenewalDialog(parent, membershipService, planService, ms);
                dialog.setVisible(true);
                if (dialog.isRenewed()) {
                    load.run();
                }
            }
        });

        load.run();
        return p;
    }

    private JPanel createActiveExpiredTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"ALL", "ACTIVE", "EXPIRED", "UPCOMING", "CANCELLED"});
        JButton refreshBtn = new JButton("Filter");
        JButton exportBtn = new JButton("Export CSV");

        top.add(new JLabel("Status Filter:"));
        top.add(statusFilter);
        top.add(refreshBtn);
        top.add(exportBtn);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Membership Code", "Member Code", "Member Name", "Plan", "Start Date", "End Date", "Agreed Fee", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        ModernTable table = new ModernTable(model);
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasF, int r, int c) {
                StatusBadge badge = new StatusBadge(val != null ? val.toString() : "ACTIVE");
                JPanel wrap = new JPanel(new GridBagLayout());
                wrap.setOpaque(true);
                wrap.setBackground(isSel ? table.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
                wrap.add(badge);
                return wrap;
            }
        });

        p.add(top, BorderLayout.NORTH);
        p.add(table.createScrollPane(), BorderLayout.CENTER);

        Runnable load = () -> {
            String filter = (String) statusFilter.getSelectedItem();
            List<Membership> list = membershipService.searchMemberships(filter, null);
            model.setRowCount(0);
            for (Membership ms : list) {
                model.addRow(new Object[]{
                        ms.getMembershipCode(),
                        ms.getMemberCode(),
                        ms.getMemberName(),
                        ms.getPlanName(),
                        DateTimeUtil.formatDate(ms.getStartDate()),
                        DateTimeUtil.formatDate(ms.getEndDate()),
                        CurrencyUtil.format(ms.getAgreedFee()),
                        ms.getStatus()
                });
            }
        };

        refreshBtn.addActionListener(e -> load.run());
        statusFilter.addActionListener(e -> load.run());
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCsv(this, table, "FitCore_Memberships_Active_Expired_Report"));

        load.run();
        return p;
    }

    private JPanel createTrainerAllocationTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        top.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh Roster");
        JButton exportBtn = new JButton("Export CSV");
        top.add(refreshBtn);
        top.add(exportBtn);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Trainer Code", "Trainer Name", "Specialization", "Contact Phone", "Assigned Active Members", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        ModernTable table = new ModernTable(model);
        p.add(top, BorderLayout.NORTH);
        p.add(table.createScrollPane(), BorderLayout.CENTER);

        Runnable load = () -> {
            List<Trainer> list = trainerDao.findAll("ALL", null);
            model.setRowCount(0);
            for (Trainer t : list) {
                model.addRow(new Object[]{
                        t.getTrainerCode(),
                        t.getName(),
                        t.getSpecialization(),
                        t.getPhone(),
                        t.getAssignedMemberCount() + " members",
                        t.getStatus()
                });
            }
        };

        refreshBtn.addActionListener(e -> load.run());
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCsv(this, table, "FitCore_Trainer_Allocation_Report"));

        load.run();
        return p;
    }

    private JPanel createRevenueBreakdownTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        top.setOpaque(false);
        JButton exportBtn = new JButton("Export CSV");
        top.add(exportBtn);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Payment Method", "Successful Transaction Count", "Total Collected ($)"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        ModernTable table = new ModernTable(model);
        p.add(top, BorderLayout.NORTH);
        p.add(table.createScrollPane(), BorderLayout.CENTER);

        exportBtn.addActionListener(e -> ExportUtil.exportTableToCsv(this, table, "FitCore_Revenue_Breakdown_Report"));

        // Query payment method statistics
        String[] methods = {"CASH", "UPI", "CARD", "BANK_TRANSFER"};
        for (String m : methods) {
            List<Payment> list = paymentDao.findAll(null, null, m, "SUCCESS", null);
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            for (Payment pmt : list) {
                total = total.add(pmt.getAmount());
            }
            model.addRow(new Object[]{
                    m,
                    list.size() + " transactions",
                    CurrencyUtil.format(total)
            });
        }

        return p;
    }

    private JPanel createAttendanceFrequencyTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        top.setOpaque(false);
        JButton exportBtn = new JButton("Export CSV");
        top.add(exportBtn);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Date", "Check-in Visits Recorded"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        ModernTable table = new ModernTable(model);
        p.add(top, BorderLayout.NORTH);
        p.add(table.createScrollPane(), BorderLayout.CENTER);

        LocalDate now = LocalDate.now();
        Map<LocalDate, Integer> map = attendanceService.getMonthlyAttendanceSummary(now.getYear(), now.getMonthValue());
        for (Map.Entry<LocalDate, Integer> entry : map.entrySet()) {
            model.addRow(new Object[]{
                    DateTimeUtil.formatDate(entry.getKey()),
                    entry.getValue() + " visits"
            });
        }

        exportBtn.addActionListener(e -> ExportUtil.exportTableToCsv(this, table, "FitCore_Attendance_Frequency_Report"));

        return p;
    }
}
