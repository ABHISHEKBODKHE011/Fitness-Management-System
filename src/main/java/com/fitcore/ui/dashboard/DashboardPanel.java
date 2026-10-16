package com.fitcore.ui.dashboard;

import com.fitcore.model.*;
import com.fitcore.service.DashboardService;
import com.fitcore.ui.components.ModernTable;
import com.fitcore.ui.components.StatCard;
import com.fitcore.util.CurrencyUtil;
import com.fitcore.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main visual dashboard displaying aggregated KPI cards, JFreeChart trends, and recent logs.
 */
public class DashboardPanel extends JPanel {

    private final DashboardService dashboardService;
    private final Consumer<String> navigationHandler;
    private final Runnable openCheckInAction;
    private final Runnable openMemberFormAction;
    private final Runnable openPaymentFormAction;

    // Stat Cards
    private StatCard totalMembersCard;
    private StatCard activeMembersCard;
    private StatCard todayAttendanceCard;
    private StatCard expiringSoonCard;
    private StatCard monthRevenueCard;
    private StatCard totalRevenueCard;

    // Charts
    private RevenueChartPanel revenueChartPanel;
    private PlanDistributionChartPanel planChartPanel;

    // Tables
    private DefaultTableModel recentMembersModel;
    private DefaultTableModel recentPaymentsModel;

    public DashboardPanel(DashboardService dashboardService,
                          Consumer<String> navigationHandler,
                          Runnable openCheckInAction,
                          Runnable openMemberFormAction,
                          Runnable openPaymentFormAction) {
        this.dashboardService = dashboardService;
        this.navigationHandler = navigationHandler;
        this.openCheckInAction = openCheckInAction;
        this.openMemberFormAction = openMemberFormAction;
        this.openPaymentFormAction = openPaymentFormAction;

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252)); // slate-50

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // 1. Header with title and quick action buttons
        mainContent.add(createHeaderPanel());
        mainContent.add(Box.createVerticalStrut(16));

        // 2. Summary KPI Cards Grid
        mainContent.add(createKpiCardsPanel());
        mainContent.add(Box.createVerticalStrut(16));

        // 3. Analytics Charts Grid
        mainContent.add(createChartsPanel());
        mainContent.add(Box.createVerticalStrut(16));

        // 4. Recent Activity Tables Grid
        mainContent.add(createRecentActivityPanel());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel("Executive Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        JButton checkInBtn = createButton("⏱ Quick Check-in", new Color(16, 185, 129), Color.WHITE);
        checkInBtn.addActionListener(e -> {
            if (openCheckInAction != null) openCheckInAction.run();
        });

        JButton newMemberBtn = createButton("+ Add Member", new Color(37, 99, 235), Color.WHITE);
        newMemberBtn.addActionListener(e -> {
            if (openMemberFormAction != null) openMemberFormAction.run();
        });

        JButton payBtn = createButton("💳 Record Payment", new Color(79, 70, 229), Color.WHITE);
        payBtn.addActionListener(e -> {
            if (openPaymentFormAction != null) openPaymentFormAction.run();
        });

        JButton refreshBtn = createButton("🔄 Refresh", new Color(241, 245, 249), new Color(30, 41, 59));
        refreshBtn.addActionListener(e -> refreshData());

        buttonsPanel.add(checkInBtn);
        buttonsPanel.add(newMemberBtn);
        buttonsPanel.add(payBtn);
        buttonsPanel.add(refreshBtn);

        panel.add(title, BorderLayout.WEST);
        panel.add(buttonsPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createKpiCardsPanel() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14));
        grid.setOpaque(false);

        totalMembersCard = new StatCard("Total Members", "0", "Registered accounts", new Color(37, 99, 235));
        activeMembersCard = new StatCard("Active Members", "0", "Currently eligible", new Color(16, 185, 129));
        todayAttendanceCard = new StatCard("Today's Check-ins", "0", "Member visits today", new Color(245, 158, 11));
        expiringSoonCard = new StatCard("Expiring in 7 Days", "0", "Requires renewal follow-up", new Color(239, 68, 68));
        monthRevenueCard = new StatCard("This Month Revenue", "$0.00", "Current billing cycle", new Color(139, 92, 246));
        totalRevenueCard = new StatCard("Total Revenue Collected", "$0.00", "All-time receipts", new Color(20, 184, 166));

        grid.add(totalMembersCard);
        grid.add(activeMembersCard);
        grid.add(todayAttendanceCard);
        grid.add(expiringSoonCard);
        grid.add(monthRevenueCard);
        grid.add(totalRevenueCard);

        return grid;
    }

    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 14, 14));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 300));

        revenueChartPanel = new RevenueChartPanel();
        planChartPanel = new PlanDistributionChartPanel();

        panel.add(revenueChartPanel);
        panel.add(planChartPanel);
        return panel;
    }

    private JPanel createRecentActivityPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 14, 14));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 260));

        // Left Table: Recent Registrations
        JPanel leftContainer = createSectionContainer("Recent Member Registrations", "MEMBERS");
        recentMembersModel = new DefaultTableModel(new String[]{"Code", "Name", "Phone", "Plan", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        ModernTable membersTable = new ModernTable(recentMembersModel);
        leftContainer.add(membersTable.createScrollPane(), BorderLayout.CENTER);

        // Right Table: Recent Payments
        JPanel rightContainer = createSectionContainer("Recent Payment Receipts", "PAYMENTS");
        recentPaymentsModel = new DefaultTableModel(new String[]{"Receipt No", "Member", "Amount", "Method", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        ModernTable paymentsTable = new ModernTable(recentPaymentsModel);
        rightContainer.add(paymentsTable.createScrollPane(), BorderLayout.CENTER);

        panel.add(leftContainer);
        panel.add(rightContainer);
        return panel;
    }

    private JPanel createSectionContainer(String title, String navKey) {
        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(15, 23, 42));

        JButton viewAll = new JButton("View All →");
        viewAll.setFont(new Font("Segoe UI", Font.BOLD, 11));
        viewAll.setForeground(new Color(37, 99, 235));
        viewAll.setBorder(null);
        viewAll.setContentAreaFilled(false);
        viewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAll.addActionListener(e -> {
            if (navigationHandler != null) navigationHandler.accept(navKey);
        });

        top.add(lbl, BorderLayout.WEST);
        top.add(viewAll, BorderLayout.EAST);
        container.add(top, BorderLayout.NORTH);

        return container;
    }

    public void refreshData() {
        SwingWorker<DashboardMetrics, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardMetrics doInBackground() {
                return dashboardService.getDashboardMetrics();
            }

            @Override
            protected void done() {
                try {
                    DashboardMetrics metrics = get();
                    updateDisplay(metrics);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DashboardPanel.this,
                            "Error loading dashboard metrics: " + e.getMessage(),
                            "Dashboard Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateDisplay(DashboardMetrics m) {
        totalMembersCard.setValue(String.valueOf(m.getTotalMembers()));
        activeMembersCard.setValue(String.valueOf(m.getActiveMembers()));
        todayAttendanceCard.setValue(String.valueOf(m.getTodayAttendanceCount()));
        expiringSoonCard.setValue(String.valueOf(m.getExpiringSoonMemberships()));
        monthRevenueCard.setValue(CurrencyUtil.format(m.getCurrentMonthRevenue()));
        totalRevenueCard.setValue(CurrencyUtil.format(m.getTotalRevenueCollected()));

        revenueChartPanel.updateData(m.getMonthlyRevenues());
        planChartPanel.updateData(m.getPlanDistributions());

        // Update Recent Members
        recentMembersModel.setRowCount(0);
        for (Member mem : m.getRecentMembers()) {
            recentMembersModel.addRow(new Object[]{
                    mem.getMemberCode(),
                    mem.getFullName(),
                    mem.getPhone(),
                    mem.getCurrentPlanName() != null ? mem.getCurrentPlanName() : "None",
                    mem.getStatus()
            });
        }

        // Update Recent Payments
        recentPaymentsModel.setRowCount(0);
        for (Payment pay : m.getRecentPayments()) {
            recentPaymentsModel.addRow(new Object[]{
                    pay.getPaymentCode(),
                    pay.getMemberName(),
                    CurrencyUtil.format(pay.getAmount()),
                    pay.getPaymentMethod(),
                    DateTimeUtil.formatDateTime(pay.getPaymentDate())
            });
        }
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
