package com.fitcore.ui.members;

import com.fitcore.model.Member;
import com.fitcore.service.AttendanceService;
import com.fitcore.service.MemberService;
import com.fitcore.service.MembershipPlanService;
import com.fitcore.service.MembershipService;
import com.fitcore.service.PaymentService;
import com.fitcore.service.TrainerService;
import com.fitcore.ui.components.ModernTable;
import com.fitcore.ui.components.StatusBadge;
import com.fitcore.ui.memberships.MembershipAssignmentDialog;
import com.fitcore.util.DateTimeUtil;
import com.fitcore.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Member management panel providing full CRUD, searching, filtering, and 360° profile inspection.
 */
public class MemberManagementPanel extends JPanel {

    private final MemberService memberService;
    private final TrainerService trainerService;
    private final MembershipService membershipService;
    private final MembershipPlanService planService;
    private final AttendanceService attendanceService;
    private final PaymentService paymentService;

    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private DefaultTableModel tableModel;
    private ModernTable table;
    private List<Member> currentMemberList = new ArrayList<>();

    public MemberManagementPanel(MemberService memberService,
                                 TrainerService trainerService,
                                 MembershipService membershipService,
                                 MembershipPlanService planService,
                                 AttendanceService attendanceService,
                                 PaymentService paymentService) {
        this.memberService = memberService;
        this.trainerService = trainerService;
        this.membershipService = membershipService;
        this.planService = planService;
        this.attendanceService = attendanceService;
        this.paymentService = paymentService;

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        add(createTopControlPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createTopControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        // Title & Search Filter Row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel titleLbl = new JLabel("Member Management Directory");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(new Color(15, 23, 42));
        titleRow.add(titleLbl, BorderLayout.WEST);

        // Search + Status Filter
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBox.setOpaque(false);

        searchField = new JTextField(18);
        searchField.putClientProperty("JTextField.placeholderText", "Search by code, name, phone, email...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.addActionListener(e -> refreshData());

        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchBtn.addActionListener(e -> refreshData());

        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "ACTIVE", "INACTIVE"});
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilterCombo.addActionListener(e -> refreshData());

        searchBox.add(new JLabel("Status:"));
        searchBox.add(statusFilterCombo);
        searchBox.add(searchField);
        searchBox.add(searchBtn);
        titleRow.add(searchBox, BorderLayout.EAST);

        // Action Toolbar
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);

        JButton addBtn = createActionButton("+ Register Member", new Color(37, 99, 235), Color.WHITE);
        addBtn.addActionListener(e -> onAddMember());

        JButton editBtn = createActionButton("✏ Edit Details", new Color(241, 245, 249), new Color(30, 41, 59));
        editBtn.addActionListener(e -> onEditMember());

        JButton viewProfileBtn = createActionButton("🔍 View 360° Profile", new Color(15, 23, 42), Color.WHITE);
        viewProfileBtn.addActionListener(e -> onViewProfile());

        JButton assignPlanBtn = createActionButton("📋 Assign Plan", new Color(16, 185, 129), Color.WHITE);
        assignPlanBtn.addActionListener(e -> onAssignPlan());

        JButton toggleStatusBtn = createActionButton("🔄 Toggle Active/Inactive", new Color(241, 245, 249), new Color(30, 41, 59));
        toggleStatusBtn.addActionListener(e -> onToggleStatus());

        JButton exportBtn = createActionButton("📥 Export CSV", new Color(241, 245, 249), new Color(30, 41, 59));
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCsv(this, table, "FitCore_Members_Directory"));

        actionRow.add(addBtn);
        actionRow.add(editBtn);
        actionRow.add(viewProfileBtn);
        actionRow.add(assignPlanBtn);
        actionRow.add(toggleStatusBtn);
        actionRow.add(exportBtn);

        panel.add(titleRow, BorderLayout.NORTH);
        panel.add(actionRow, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        tableModel = new DefaultTableModel(
                new String[]{"Member Code", "Full Name", "Phone", "Email", "Gender", "Trainer", "Active Plan", "Valid Until", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new ModernTable(tableModel);

        // Custom status badge renderer
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                StatusBadge badge = new StatusBadge(value != null ? value.toString() : "ACTIVE");
                JPanel p = new JPanel(new GridBagLayout());
                p.setOpaque(true);
                p.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
                p.add(badge);
                return p;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onViewProfile();
                }
            }
        });

        panel.add(table.createScrollPane(), BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        String filter = (String) statusFilterCombo.getSelectedItem();
        String query = searchField.getText().trim();

        SwingWorker<List<Member>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Member> doInBackground() {
                return memberService.searchMembers(filter, query, null);
            }

            @Override
            protected void done() {
                try {
                    currentMemberList = get();
                    tableModel.setRowCount(0);
                    for (Member m : currentMemberList) {
                        tableModel.addRow(new Object[]{
                                m.getMemberCode(),
                                m.getFullName(),
                                m.getPhone(),
                                m.getEmail(),
                                m.getGender(),
                                m.getAssignedTrainerName() != null ? m.getAssignedTrainerName() : "None",
                                m.getCurrentPlanName() != null ? m.getCurrentPlanName() : "-",
                                m.getCurrentMembershipEndDate() != null ? DateTimeUtil.formatDate(m.getCurrentMembershipEndDate()) : "-",
                                m.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MemberManagementPanel.this,
                            "Error retrieving members: " + ex.getMessage(),
                            "Query Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private Member getSelectedMember() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentMemberList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a member from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return currentMemberList.get(row);
    }

    public void onAddMember() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        MemberFormDialog dialog = new MemberFormDialog(parent, memberService, trainerService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void onEditMember() {
        Member selected = getSelectedMember();
        if (selected == null) return;

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        MemberFormDialog dialog = new MemberFormDialog(parent, memberService, trainerService, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void onViewProfile() {
        Member selected = getSelectedMember();
        if (selected == null) return;

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        MemberDetailDialog dialog = new MemberDetailDialog(parent, selected, membershipService, attendanceService, paymentService);
        dialog.setVisible(true);
    }

    private void onAssignPlan() {
        Member selected = getSelectedMember();
        if (selected == null) return;

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        MembershipAssignmentDialog dialog = new MembershipAssignmentDialog(parent, membershipService, planService, memberService, selected.getId());
        dialog.setVisible(true);
        if (dialog.isAssigned()) {
            refreshData();
        }
    }

    private void onToggleStatus() {
        Member selected = getSelectedMember();
        if (selected == null) return;

        if (selected.isActive()) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Deactivating member '" + selected.getFullName() + "' will preserve all financial & attendance history.\nProceed with deactivation?",
                    "Confirm Deactivation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                memberService.deactivateMember(selected.getId());
                refreshData();
            }
        } else {
            memberService.activateMember(selected.getId());
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
