package com.fitcore.ui;

import com.fitcore.config.AppConfig;
import com.fitcore.security.SecurityContext;
import com.fitcore.service.*;
import com.fitcore.ui.attendance.AttendancePanel;
import com.fitcore.ui.attendance.CheckInDialog;
import com.fitcore.ui.components.Sidebar;
import com.fitcore.ui.components.TopBar;
import com.fitcore.ui.dashboard.DashboardPanel;
import com.fitcore.ui.members.MemberFormDialog;
import com.fitcore.ui.members.MemberManagementPanel;
import com.fitcore.ui.memberships.MembershipPlanPanel;
import com.fitcore.ui.payments.PaymentFormDialog;
import com.fitcore.ui.payments.PaymentPanel;
import com.fitcore.ui.reports.AuditLogPanel;
import com.fitcore.ui.reports.ReportPanel;
import com.fitcore.ui.trainers.TrainerManagementPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame integrating sidebar navigation, top bar, and module card panels.
 */
public class MainFrame extends JFrame {

    private final AuthService authService;
    private final MemberService memberService;
    private final TrainerService trainerService;
    private final MembershipPlanService planService;
    private final MembershipService membershipService;
    private final AttendanceService attendanceService;
    private final PaymentService paymentService;
    private final DashboardService dashboardService;
    private final AuditService auditService;

    private Sidebar sidebar;
    private TopBar topBar;
    private JPanel cardsPanel;
    private CardLayout cardLayout;

    // View panels
    private DashboardPanel dashboardPanel;
    private MemberManagementPanel memberPanel;
    private TrainerManagementPanel trainerPanel;
    private MembershipPlanPanel planPanel;
    private AttendancePanel attendancePanel;
    private PaymentPanel paymentPanel;
    private ReportPanel reportPanel;
    private AuditLogPanel auditPanel;

    public MainFrame() {
        super(AppConfig.get("app.title", "FitCore Pro - Fitness Management System"));

        // Initialize Services
        this.authService = new AuthService();
        this.memberService = new MemberService();
        this.trainerService = new TrainerService();
        this.planService = new MembershipPlanService();
        this.membershipService = new MembershipService();
        this.attendanceService = new AttendanceService();
        this.paymentService = new PaymentService();
        this.dashboardService = new DashboardService();
        this.auditService = new AuditService();

        // Safe bootstrap of administrator if empty
        authService.bootstrapAdminIfNeeded();

        initFrame();
    }

    private void initFrame() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "Are you sure you want to exit FitCore Pro?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    if (SecurityContext.isAuthenticated()) {
                        authService.logout();
                    }
                    System.exit(0);
                }
            }
        });

        // Prompt login before showing main application
        showLogin();
    }

    private void showLogin() {
        setVisible(false);
        LoginDialog loginDialog = new LoginDialog(this, authService);
        loginDialog.setVisible(true);

        if (loginDialog.isAuthenticated()) {
            buildMainLayout();
            setVisible(true);
        } else {
            System.exit(0);
        }
    }

    private void buildMainLayout() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        // 1. Top Bar
        topBar = new TopBar(this::handleLogout);

        // 2. Central Card Layout container
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(com.fitcore.ui.theme.Theme.BG_APP);

        // Instantiate view panels
        dashboardPanel = new DashboardPanel(
                dashboardService,
                this::switchTab,
                this::openQuickCheckIn,
                this::openQuickNewMember,
                this::openQuickPayment
        );

        memberPanel = new MemberManagementPanel(
                memberService,
                trainerService,
                membershipService,
                planService,
                attendanceService,
                paymentService
        );

        trainerPanel = new TrainerManagementPanel(trainerService);
        planPanel = new MembershipPlanPanel(planService, membershipService, memberService);
        attendancePanel = new AttendancePanel(attendanceService, memberService);
        paymentPanel = new PaymentPanel(paymentService, memberService, membershipService);
        reportPanel = new ReportPanel(membershipService, planService, attendanceService);
        auditPanel = new AuditLogPanel(auditService);

        cardsPanel.add(dashboardPanel, "DASHBOARD");
        cardsPanel.add(memberPanel, "MEMBERS");
        cardsPanel.add(trainerPanel, "TRAINERS");
        cardsPanel.add(planPanel, "PLANS");
        cardsPanel.add(attendancePanel, "ATTENDANCE");
        cardsPanel.add(paymentPanel, "PAYMENTS");
        cardsPanel.add(reportPanel, "REPORTS");
        cardsPanel.add(auditPanel, "AUDIT");

        // 3. Sidebar Navigation
        sidebar = new Sidebar(this::switchTab);

        add(sidebar, BorderLayout.WEST);

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(com.fitcore.ui.theme.Theme.BG_APP);
        centerContainer.add(topBar, BorderLayout.NORTH);
        centerContainer.add(cardsPanel, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);

        sidebar.rebuildMenu();
        switchTab("DASHBOARD");

        revalidate();
        repaint();
    }

    private void switchTab(String tabKey) {
        if (cardLayout == null || cardsPanel == null) {
            return;
        }
        cardLayout.show(cardsPanel, tabKey);
        if (sidebar != null) {
            sidebar.setActiveTab(tabKey);
        }
        // Refresh data on panel activation
        switch (tabKey) {
            case "DASHBOARD" -> { if (dashboardPanel != null) dashboardPanel.refreshData(); }
            case "MEMBERS" -> { if (memberPanel != null) memberPanel.refreshData(); }
            case "TRAINERS" -> { if (trainerPanel != null) trainerPanel.refreshData(); }
            case "PLANS" -> { if (planPanel != null) planPanel.refreshData(); }
            case "ATTENDANCE" -> { if (attendancePanel != null) attendancePanel.loadTodayData(); }
            case "PAYMENTS" -> { if (paymentPanel != null) paymentPanel.refreshData(); }
            case "AUDIT" -> { if (auditPanel != null) auditPanel.refreshData(); }
        }
    }

    private void handleLogout() {
        authService.logout();
        showLogin();
    }

    private void openQuickCheckIn() {
        CheckInDialog dialog = new CheckInDialog(this, attendanceService, memberService);
        dialog.setVisible(true);
        if (dialog.isCheckedIn()) {
            dashboardPanel.refreshData();
            attendancePanel.refreshData();
        }
    }

    private void openQuickNewMember() {
        MemberFormDialog dialog = new MemberFormDialog(this, memberService, trainerService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            dashboardPanel.refreshData();
            memberPanel.refreshData();
        }
    }

    private void openQuickPayment() {
        PaymentFormDialog dialog = new PaymentFormDialog(this, paymentService, memberService, membershipService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            dashboardPanel.refreshData();
            paymentPanel.refreshData();
        }
    }
}
