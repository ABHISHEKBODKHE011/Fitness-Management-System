package com.fitcore.ui.attendance;

import com.fitcore.model.Attendance;
import com.fitcore.service.AttendanceService;
import com.fitcore.service.MemberService;
import com.fitcore.ui.components.ModernTable;
import com.fitcore.ui.components.StatusBadge;
import com.fitcore.util.DateTimeUtil;
import com.fitcore.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Attendance panel managing daily check-ins, history queries, date filters, and export.
 */
public class AttendancePanel extends JPanel {

    private final AttendanceService attendanceService;
    private final MemberService memberService;

    private JTextField fromDateField;
    private JTextField toDateField;
    private JTextField searchField;
    private DefaultTableModel tableModel;
    private ModernTable table;
    private List<Attendance> currentList = new ArrayList<>();

    public AttendancePanel(AttendanceService attendanceService, MemberService memberService) {
        this.attendanceService = attendanceService;
        this.memberService = memberService;

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        add(createTopControlPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadTodayData();
    }

    private JPanel createTopControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel titleLbl = new JLabel("Daily Attendance & Check-in Roster");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(new Color(15, 23, 42));
        titleRow.add(titleLbl, BorderLayout.WEST);

        // Date filter controls
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterRow.setOpaque(false);

        fromDateField = new JTextField(DateTimeUtil.formatDateIso(LocalDate.now().minusDays(7)), 9);
        toDateField = new JTextField(DateTimeUtil.formatDateIso(LocalDate.now()), 9);
        searchField = new JTextField(14);
        searchField.putClientProperty("JTextField.placeholderText", "Search member...");
        searchField.addActionListener(e -> refreshData());

        JButton filterBtn = new JButton("Filter");
        filterBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterBtn.addActionListener(e -> refreshData());

        JButton todayBtn = new JButton("Today Only");
        todayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        todayBtn.addActionListener(e -> loadTodayData());

        filterRow.add(new JLabel("From:"));
        filterRow.add(fromDateField);
        filterRow.add(new JLabel("To:"));
        filterRow.add(toDateField);
        filterRow.add(searchField);
        filterRow.add(filterBtn);
        filterRow.add(todayBtn);

        titleRow.add(filterRow, BorderLayout.EAST);

        // Action Buttons Row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);

        JButton checkInBtn = createActionButton("⏱ Quick Check-in", new Color(16, 185, 129), Color.WHITE);
        checkInBtn.addActionListener(e -> onCheckIn());

        JButton exportBtn = createActionButton("📥 Export Attendance CSV", new Color(241, 245, 249), new Color(30, 41, 59));
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCsv(this, table, "FitCore_Attendance_Logs"));

        actionRow.add(checkInBtn);
        actionRow.add(exportBtn);

        panel.add(titleRow, BorderLayout.NORTH);
        panel.add(actionRow, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        tableModel = new DefaultTableModel(
                new String[]{"Check-in Time", "Date", "Member Code", "Member Name", "Phone", "Membership Status", "Session Notes"}, 0
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
                p.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
                p.add(badge);
                return p;
            }
        });

        panel.add(table.createScrollPane(), BorderLayout.CENTER);
        return panel;
    }

    public void loadTodayData() {
        fromDateField.setText(DateTimeUtil.formatDateIso(LocalDate.now()));
        toDateField.setText(DateTimeUtil.formatDateIso(LocalDate.now()));
        refreshData();
    }

    public void refreshData() {
        LocalDate start = DateTimeUtil.parseDate(fromDateField.getText().trim());
        LocalDate end = DateTimeUtil.parseDate(toDateField.getText().trim());
        String q = searchField.getText().trim();

        SwingWorker<List<Attendance>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Attendance> doInBackground() {
                return attendanceService.getAttendanceHistory(start, end, q);
            }

            @Override
            protected void done() {
                try {
                    currentList = get();
                    tableModel.setRowCount(0);
                    for (Attendance a : currentList) {
                        tableModel.addRow(new Object[]{
                                a.getCheckInTime().format(DateTimeUtil.TIME_FORMATTER),
                                DateTimeUtil.formatDate(a.getCheckInDate()),
                                a.getMemberCode(),
                                a.getMemberName(),
                                a.getMemberPhone(),
                                a.getMembershipStatus(),
                                a.getNotes() != null ? a.getNotes() : ""
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AttendancePanel.this,
                            "Error retrieving attendance logs: " + ex.getMessage(),
                            "Query Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void onCheckIn() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        CheckInDialog dialog = new CheckInDialog(parent, attendanceService, memberService);
        dialog.setVisible(true);
        if (dialog.isCheckedIn()) {
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
