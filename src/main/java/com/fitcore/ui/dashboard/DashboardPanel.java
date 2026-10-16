package com.fitcore.ui.dashboard;

import com.fitcore.model.*;
import com.fitcore.service.DashboardService;
import com.fitcore.ui.components.ModernTable;
import com.fitcore.ui.components.StatCard;
import com.fitcore.ui.theme.Theme;
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
        setBackground(Theme.BG_APP);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));

        // 1. Header with title and quick action buttons
        mainContent.add(createHeaderPanel());
        mainContent.add(Box.createVerticalStrut(20));

        // 2. Summary KPI Cards Grid
        mainContent.add(createKpiCardsPanel());
        mainContent.add(Box.createVerticalStrut(20));

        // 3. Analytics Charts Grid
        mainContent.add(createChartsPanel());
        mainContent.add(Box.createVerticalStrut(20));

        // 4. Recent Activity Tables Grid
        mainContent.add(createRecentActivityPanel());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_APP);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Executive Analytics Console");
        title.setFont(Theme.bold(22));
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Real-time operational metrics, member trends, and revenue telemetry");
        sub.setFont(Theme.font(12));
        sub.setForeground(Theme.TEXT_SECONDARY);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(sub);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        JButton checkInBtn = Theme.createSuccessButton("⏱  Quick Check-in");
        checkInBtn.addActionListener(e -> {
            if (openCheckInAction != null) openCheckInAction.run();
        });

        JButton newMemberBtn = Theme.createPrimaryButton("➕  New Athlete");
        newMemberBtn.addActionListener(e -> {
            if (openMemberFormAction != null) openMemberFormAction.run();
        });

        JButton payBtn = Theme.createGhostButton("💳  Record Payment");
        payBtn.addActionListener(e -> {
            if (openPaymentFormAction != null) openPaymentFormAction.run();
        });

        JButton refreshBtn = Theme.createGhostButton("🔄  Sync Data");
        refreshBtn.addActionListener(e -> refreshData());

        buttonsPanel.add(checkInBtn);
        buttonsPanel.add(newMemberBtn);
        buttonsPanel.add(payBtn);
        buttonsPanel.add(refreshBtn);

        panel.add(titleBlock, BorderLayout.WEST);
        panel.add(buttonsPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createKpiCardsPanel() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setOpaque(false);

        totalMembersCard = new StatCard("Total Members", "0", "Registered athlete profiles", Theme.ACCENT_PRIMARY);
        activeMembersCard = new StatCard("Active Members", "0", "Currently eligible subscribers", Theme.ACCENT_EMERALD);
        todayAttendanceCard = new StatCard("Today's Check-ins", "0", "Athletes checked in today", Theme.ACCENT_CYAN);
        expiringSoonCard = new StatCard("Expiring in 7 Days", "0", "Requires renewal follow-up", Theme.ACCENT_ROSE);
        monthRevenueCard = new StatCard("Current Month Revenue", "$0.00", "Billed in active cycle", Theme.ACCENT_PURPLE);
        totalRevenueCard = new StatCard("Total Revenue Collected", "$0.00", "All-time financial intake", Theme.ACCENT_AMBER);

        grid.add(totalMembersCard);
        grid.add(activeMembersCard);
        grid.add(todayAttendanceCard);
        grid.add(expiringSoonCard);
        grid.add(monthRevenueCard);
        grid.add(totalRevenueCard);

        return grid;
    }

    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 16));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 310));

        revenueChartPanel = new RevenueChartPanel();
        planChartPanel = new PlanDistributionChartPanel();

        panel.add(revenueChartPanel);
        panel.add(planChartPanel);
        return panel;
    }

    private JPanel createRecentActivityPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 16));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 280));

        // Left Table: Recent Registrations
        JPanel leftContainer = createSectionContainer("Recent Athlete Registrations", "MEMBERS");
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
        container.setBackground(Theme.BG_CARD);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lbl = new JLabel(title);
        lbl.setFont(Theme.bold(14));
        lbl.setForeground(Theme.TEXT_PRIMARY);

        JButton viewAll = new JButton("Explore all →");
        viewAll.setFont(Theme.bold(11));
        viewAll.setForeground(Theme.ACCENT_CYAN);
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
