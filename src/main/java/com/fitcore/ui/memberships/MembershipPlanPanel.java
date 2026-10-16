package com.fitcore.ui.memberships;

import com.fitcore.model.MembershipPlan;
import com.fitcore.service.MemberService;
import com.fitcore.service.MembershipPlanService;
import com.fitcore.service.MembershipService;
import com.fitcore.ui.components.ModernTable;
import com.fitcore.ui.components.StatusBadge;
import com.fitcore.util.CurrencyUtil;
import com.fitcore.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel managing the catalog of membership plans, pricing, durations, and deactivations.
 */
public class MembershipPlanPanel extends JPanel {

    private final MembershipPlanService planService;
    private final MembershipService membershipService;
    private final MemberService memberService;

    private DefaultTableModel tableModel;
    private ModernTable table;
    private List<MembershipPlan> currentPlanList = new ArrayList<>();

    public MembershipPlanPanel(MembershipPlanService planService,
                               MembershipService membershipService,
                               MemberService memberService) {
        this.planService = planService;
        this.membershipService = membershipService;
        this.memberService = memberService;

        setLayout(new BorderLayout());
        setBackground(com.fitcore.ui.theme.Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));

        add(createTopControlPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createTopControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel titleLbl = new JLabel("Membership Plans & Subscription Tiers");
        titleLbl.setFont(com.fitcore.ui.theme.Theme.bold(22));
        titleLbl.setForeground(com.fitcore.ui.theme.Theme.TEXT_PRIMARY);
        titleRow.add(titleLbl, BorderLayout.WEST);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);

        JButton addBtn = com.fitcore.ui.theme.Theme.createPrimaryButton("➕  Create Tier");
        addBtn.addActionListener(e -> onAddPlan());

        JButton editBtn = com.fitcore.ui.theme.Theme.createGhostButton("✏  Edit Tier");
        editBtn.addActionListener(e -> onEditPlan());

        JButton assignBtn = com.fitcore.ui.theme.Theme.createSuccessButton("📋  Assign to Athlete");
        assignBtn.addActionListener(e -> onAssignToMember());

        JButton deactivateBtn = com.fitcore.ui.theme.Theme.createGhostButton("🚫  Toggle Status");
        deactivateBtn.addActionListener(e -> onDeactivatePlan());

        JButton exportBtn = com.fitcore.ui.theme.Theme.createGhostButton("📥  Export CSV");
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCsv(this, table, "FitCore_Plans_Catalog"));

        actionRow.add(addBtn);
        actionRow.add(editBtn);
        actionRow.add(assignBtn);
        actionRow.add(deactivateBtn);
        actionRow.add(exportBtn);

        panel.add(titleRow, BorderLayout.NORTH);
        panel.add(actionRow, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(com.fitcore.ui.theme.Theme.BG_CARD);

        tableModel = new DefaultTableModel(
                new String[]{"Plan Code", "Plan Name", "Duration", "Standard Fee", "Description", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new ModernTable(tableModel);

        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                StatusBadge badge = new StatusBadge(value != null ? value.toString() : "ACTIVE");
                JPanel p = new JPanel(new GridBagLayout());
                p.setOpaque(true);
                p.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? table.getBackground() : new Color(14, 20, 33)));
                p.add(badge);
                return p;
            }
        });

        panel.add(table.createScrollPane(), BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        SwingWorker<List<MembershipPlan>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MembershipPlan> doInBackground() {
                return planService.getAllPlans(false, null);
            }

            @Override
            protected void done() {
                try {
                    currentPlanList = get();
                    tableModel.setRowCount(0);
                    for (MembershipPlan p : currentPlanList) {
                        tableModel.addRow(new Object[]{
                                p.getPlanCode(),
                                p.getName(),
                                p.getDurationDays() + " days",
                                CurrencyUtil.format(p.getFee()),
                                p.getDescription() != null ? p.getDescription() : "",
                                p.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MembershipPlanPanel.this,
                            "Error retrieving plans: " + ex.getMessage(),
                            "Query Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private MembershipPlan getSelectedPlan() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentPlanList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a plan from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return currentPlanList.get(row);
    }

    private void onAddPlan() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        PlanFormDialog dialog = new PlanFormDialog(parent, planService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void onEditPlan() {
        MembershipPlan selected = getSelectedPlan();
        if (selected == null) return;

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        PlanFormDialog dialog = new PlanFormDialog(parent, planService, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void onAssignToMember() {
        MembershipPlan selected = getSelectedPlan();
        if (selected == null) return;

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        MembershipAssignmentDialog dialog = new MembershipAssignmentDialog(parent, membershipService, planService, memberService, null);
        dialog.setVisible(true);
    }

    private void onDeactivatePlan() {
        MembershipPlan selected = getSelectedPlan();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Deactivate plan '" + selected.getName() + "'?\nExisting member subscriptions referencing this plan will remain intact.",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            planService.deactivatePlan(selected.getId());
            refreshData();
        }
    }

    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
