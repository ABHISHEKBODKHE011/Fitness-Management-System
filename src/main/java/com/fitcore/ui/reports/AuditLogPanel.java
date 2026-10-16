package com.fitcore.ui.reports;

import com.fitcore.model.AuditLog;
import com.fitcore.service.AuditService;
import com.fitcore.ui.components.ModernTable;
import com.fitcore.util.DateTimeUtil;
import com.fitcore.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Administrative panel for reviewing system audit trails and compliance events.
 */
public class AuditLogPanel extends JPanel {

    private final AuditService auditService;
    private DefaultTableModel tableModel;
    private ModernTable table;

    public AuditLogPanel(AuditService auditService) {
        this.auditService = auditService;

        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("System Security & Administrative Audit Trail");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton refreshBtn = new JButton("🔄 Refresh Logs");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> refreshData());

        JButton exportBtn = new JButton("📥 Export Audit CSV");
        exportBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCsv(this, table, "FitCore_Security_Audit_Logs"));

        actions.add(refreshBtn);
        actions.add(exportBtn);

        top.add(title, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{"Timestamp", "Username", "Action", "Target Entity", "Entity ID", "Details"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new ModernTable(tableModel);
        add(table.createScrollPane(), BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        SwingWorker<List<AuditLog>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AuditLog> doInBackground() {
                return auditService.getRecentLogs(200);
            }

            @Override
            protected void done() {
                try {
                    List<AuditLog> logs = get();
                    tableModel.setRowCount(0);
                    for (AuditLog l : logs) {
                        tableModel.addRow(new Object[]{
                                DateTimeUtil.formatDateTime(l.getCreatedAt()),
                                l.getUsername(),
                                l.getAction(),
                                l.getEntityType(),
                                l.getEntityId() != null ? l.getEntityId() : "-",
                                l.getDetails() != null ? l.getDetails() : ""
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AuditLogPanel.this,
                            "Error loading audit trail: " + ex.getMessage(),
                            "Audit Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
