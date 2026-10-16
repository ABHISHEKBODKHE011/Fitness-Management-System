package com.fitcore.ui.trainers;

import com.fitcore.model.Member;
import com.fitcore.model.Trainer;
import com.fitcore.service.TrainerService;
import com.fitcore.ui.components.ModernTable;
import com.fitcore.ui.components.StatusBadge;
import com.fitcore.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Trainer management panel providing directory views, profile edits, and member roster inspection.
 */
public class TrainerManagementPanel extends JPanel {

    private final TrainerService trainerService;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private DefaultTableModel tableModel;
    private ModernTable table;
    private List<Trainer> currentTrainerList = new ArrayList<>();

    public TrainerManagementPanel(TrainerService trainerService) {
        this.trainerService = trainerService;

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

        JLabel titleLbl = new JLabel("Certified Fitness Trainers");
        titleLbl.setFont(com.fitcore.ui.theme.Theme.bold(22));
        titleLbl.setForeground(com.fitcore.ui.theme.Theme.TEXT_PRIMARY);
        titleRow.add(titleLbl, BorderLayout.WEST);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBox.setOpaque(false);

        searchField = new JTextField(18);
        searchField.putClientProperty("JTextField.placeholderText", "🔍  Search trainer code, name...");
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.setFont(com.fitcore.ui.theme.Theme.font(12));
        searchField.setBackground(com.fitcore.ui.theme.Theme.BG_INPUT);
        searchField.setForeground(com.fitcore.ui.theme.Theme.TEXT_PRIMARY);
        searchField.setCaretColor(com.fitcore.ui.theme.Theme.ACCENT_CYAN);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(com.fitcore.ui.theme.Theme.BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        searchField.addActionListener(e -> refreshData());

        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "ACTIVE", "INACTIVE"});
        statusFilterCombo.setFont(com.fitcore.ui.theme.Theme.font(12));
        statusFilterCombo.setBackground(com.fitcore.ui.theme.Theme.BG_INPUT);
        statusFilterCombo.setForeground(com.fitcore.ui.theme.Theme.TEXT_PRIMARY);
        statusFilterCombo.addActionListener(e -> refreshData());

        JLabel filterLabel = new JLabel("Status:");
        filterLabel.setFont(com.fitcore.ui.theme.Theme.bold(11));
        filterLabel.setForeground(com.fitcore.ui.theme.Theme.TEXT_SECONDARY);

        searchBox.add(filterLabel);
        searchBox.add(statusFilterCombo);
        searchBox.add(searchField);

        JButton searchBtn = com.fitcore.ui.theme.Theme.createGhostButton("Search");
        searchBtn.addActionListener(e -> refreshData());
        searchBox.add(searchBtn);
        titleRow.add(searchBox, BorderLayout.EAST);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);

        JButton addBtn = com.fitcore.ui.theme.Theme.createPrimaryButton("➕  Add Trainer");
        addBtn.addActionListener(e -> onAddTrainer());

        JButton editBtn = com.fitcore.ui.theme.Theme.createGhostButton("✏  Edit Details");
        editBtn.addActionListener(e -> onEditTrainer());

        JButton viewMembersBtn = com.fitcore.ui.theme.Theme.createGhostButton("🏋  Assigned Athletes");
        viewMembersBtn.addActionListener(e -> onViewAssignedMembers());

        JButton toggleBtn = com.fitcore.ui.theme.Theme.createGhostButton("🔄  Toggle Status");
        toggleBtn.addActionListener(e -> onToggleStatus());

        JButton exportBtn = com.fitcore.ui.theme.Theme.createGhostButton("📥  Export CSV");
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCsv(this, table, "FitCore_Trainers_Roster"));

        actionRow.add(addBtn);
        actionRow.add(editBtn);
        actionRow.add(viewMembersBtn);
        actionRow.add(toggleBtn);
        actionRow.add(exportBtn);

        panel.add(titleRow, BorderLayout.NORTH);
        panel.add(actionRow, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(com.fitcore.ui.theme.Theme.BG_CARD);

        tableModel = new DefaultTableModel(
                new String[]{"Trainer Code", "Trainer Name", "Phone", "Email", "Specialization", "Assigned Members", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new ModernTable(tableModel);

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
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
        String filter = (String) statusFilterCombo.getSelectedItem();
        String query = searchField.getText().trim();

        SwingWorker<List<Trainer>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Trainer> doInBackground() {
                return trainerService.searchTrainers(filter, query);
            }

            @Override
            protected void done() {
                try {
                    currentTrainerList = get();
                    tableModel.setRowCount(0);
                    for (Trainer t : currentTrainerList) {
                        tableModel.addRow(new Object[]{
                                t.getTrainerCode(),
                                t.getName(),
                                t.getPhone(),
                                t.getEmail(),
                                t.getSpecialization(),
                                t.getAssignedMemberCount() + " active members",
                                t.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TrainerManagementPanel.this,
                            "Error retrieving trainers: " + ex.getMessage(),
                            "Query Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private Trainer getSelectedTrainer() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentTrainerList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a trainer from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return currentTrainerList.get(row);
    }

    private void onAddTrainer() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        TrainerFormDialog dialog = new TrainerFormDialog(parent, trainerService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void onEditTrainer() {
        Trainer selected = getSelectedTrainer();
        if (selected == null) return;

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        TrainerFormDialog dialog = new TrainerFormDialog(parent, trainerService, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void onViewAssignedMembers() {
        Trainer selected = getSelectedTrainer();
        if (selected == null) return;

        List<Member> assigned = trainerService.getAssignedMembers(selected.getId());
        DefaultTableModel mModel = new DefaultTableModel(new String[]{"Code", "Name", "Phone", "Email", "Plan"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Member m : assigned) {
            mModel.addRow(new Object[]{
                    m.getMemberCode(),
                    m.getFullName(),
                    m.getPhone(),
                    m.getEmail(),
                    m.getCurrentPlanName() != null ? m.getCurrentPlanName() : "-"
            });
        }

        ModernTable mTable = new ModernTable(mModel);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Assigned Members: " + selected.getName(), true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.add(mTable.createScrollPane(), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void onToggleStatus() {
        Trainer selected = getSelectedTrainer();
        if (selected == null) return;

        if (selected.isActive()) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Deactivate trainer '" + selected.getName() + "'?\nTheir assigned members will remain preserved.",
                    "Confirm Deactivation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                trainerService.deactivateTrainer(selected.getId());
                refreshData();
            }
        } else {
            selected.setStatus("ACTIVE");
            trainerService.updateTrainer(selected);
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
