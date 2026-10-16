package com.fitcore.ui.payments;

import com.fitcore.model.Member;
import com.fitcore.model.Membership;
import com.fitcore.model.Payment;
import com.fitcore.service.MemberService;
import com.fitcore.service.MembershipService;
import com.fitcore.service.PaymentService;
import com.fitcore.ui.components.ModernTable;
import com.fitcore.ui.components.StatusBadge;
import com.fitcore.util.CurrencyUtil;
import com.fitcore.util.DateTimeUtil;
import com.fitcore.util.ExportUtil;
import com.fitcore.util.ReceiptPrinter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Payment and billing ledger panel with revenue totals, filters, and receipt generation.
 */
public class PaymentPanel extends JPanel {

    private final PaymentService paymentService;
    private final MemberService memberService;
    private final MembershipService membershipService;

    private JTextField fromDateField;
    private JTextField toDateField;
    private JComboBox<String> methodFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JTextField searchField;
    private JLabel totalRevenueLabel;
    private JLabel txCountLabel;

    private DefaultTableModel tableModel;
    private ModernTable table;
    private List<Payment> currentList = new ArrayList<>();

    public PaymentPanel(PaymentService paymentService, MemberService memberService, MembershipService membershipService) {
        this.paymentService = paymentService;
        this.memberService = memberService;
        this.membershipService = membershipService;

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

        JLabel titleLbl = new JLabel("Billing & Financial Ledger");
        titleLbl.setFont(com.fitcore.ui.theme.Theme.bold(22));
        titleLbl.setForeground(com.fitcore.ui.theme.Theme.TEXT_PRIMARY);
        titleRow.add(titleLbl, BorderLayout.WEST);

        // Filter Bar
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterRow.setOpaque(false);

        fromDateField = new JTextField(DateTimeUtil.formatDateIso(LocalDate.now().minusMonths(3)), 8);
        fromDateField.setFont(com.fitcore.ui.theme.Theme.font(12));
        fromDateField.setBackground(com.fitcore.ui.theme.Theme.BG_INPUT);
        fromDateField.setForeground(com.fitcore.ui.theme.Theme.TEXT_PRIMARY);

        toDateField = new JTextField(DateTimeUtil.formatDateIso(LocalDate.now()), 8);
        toDateField.setFont(com.fitcore.ui.theme.Theme.font(12));
        toDateField.setBackground(com.fitcore.ui.theme.Theme.BG_INPUT);
        toDateField.setForeground(com.fitcore.ui.theme.Theme.TEXT_PRIMARY);

        methodFilterCombo = new JComboBox<>(new String[]{"ALL", "CASH", "UPI", "CARD", "BANK_TRANSFER"});
        methodFilterCombo.setFont(com.fitcore.ui.theme.Theme.font(12));
        methodFilterCombo.setBackground(com.fitcore.ui.theme.Theme.BG_INPUT);
        methodFilterCombo.setForeground(com.fitcore.ui.theme.Theme.TEXT_PRIMARY);

        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "SUCCESS", "PENDING", "FAILED", "REFUNDED"});
        statusFilterCombo.setFont(com.fitcore.ui.theme.Theme.font(12));
        statusFilterCombo.setBackground(com.fitcore.ui.theme.Theme.BG_INPUT);
        statusFilterCombo.setForeground(com.fitcore.ui.theme.Theme.TEXT_PRIMARY);

        searchField = new JTextField(12);
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Search...");
        searchField.setFont(com.fitcore.ui.theme.Theme.font(12));
        searchField.setBackground(com.fitcore.ui.theme.Theme.BG_INPUT);
        searchField.setForeground(com.fitcore.ui.theme.Theme.TEXT_PRIMARY);
        searchField.setCaretColor(com.fitcore.ui.theme.Theme.ACCENT_CYAN);
        searchField.addActionListener(e -> refreshData());

        JButton filterBtn = com.fitcore.ui.theme.Theme.createGhostButton("Filter");
        filterBtn.addActionListener(e -> refreshData());

        filterRow.add(fromDateField);
        filterRow.add(toDateField);
        filterRow.add(methodFilterCombo);
        filterRow.add(statusFilterCombo);
        filterRow.add(searchField);
        filterRow.add(filterBtn);

        titleRow.add(filterRow, BorderLayout.EAST);

        // Action Toolbar & Summary Ribbon
        JPanel actionRow = new JPanel(new BorderLayout());
        actionRow.setOpaque(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);

        JButton recordBtn = com.fitcore.ui.theme.Theme.createPrimaryButton("💳  Record Payment");
        recordBtn.addActionListener(e -> onRecordPayment());

        JButton receiptBtn = com.fitcore.ui.theme.Theme.createGhostButton("📄  Print Receipt");
        receiptBtn.addActionListener(e -> onViewReceipt());

        JButton exportBtn = com.fitcore.ui.theme.Theme.createGhostButton("📥  Export Ledger CSV");
        exportBtn.addActionListener(e -> ExportUtil.exportTableToCsv(this, table, "FitCore_Billing_Ledger"));

        buttons.add(recordBtn);
        buttons.add(receiptBtn);
        buttons.add(exportBtn);

        // Summary labels
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        summary.setOpaque(false);

        totalRevenueLabel = new JLabel("Revenue: $0.00");
        totalRevenueLabel.setFont(com.fitcore.ui.theme.Theme.bold(14));
        totalRevenueLabel.setForeground(com.fitcore.ui.theme.Theme.ACCENT_EMERALD);

        txCountLabel = new JLabel("Transactions: 0");
        txCountLabel.setFont(com.fitcore.ui.theme.Theme.font(13));
        txCountLabel.setForeground(com.fitcore.ui.theme.Theme.TEXT_SECONDARY);

        summary.add(txCountLabel);
        summary.add(totalRevenueLabel);

        actionRow.add(buttons, BorderLayout.WEST);
        actionRow.add(summary, BorderLayout.EAST);

        panel.add(titleRow, BorderLayout.NORTH);
        panel.add(actionRow, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(com.fitcore.ui.theme.Theme.BG_CARD);

        tableModel = new DefaultTableModel(
                new String[]{"Receipt Code", "Date & Time", "Member Code", "Member Name", "Plan", "Amount ($)", "Method", "Auth Reference", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new ModernTable(tableModel);

        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                StatusBadge badge = new StatusBadge(value != null ? value.toString() : "SUCCESS");
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
        LocalDate start = DateTimeUtil.parseDate(fromDateField.getText().trim());
        LocalDate end = DateTimeUtil.parseDate(toDateField.getText().trim());
        String method = (String) methodFilterCombo.getSelectedItem();
        String status = (String) statusFilterCombo.getSelectedItem();
        String search = searchField.getText().trim();

        SwingWorker<List<Payment>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Payment> doInBackground() {
                return paymentService.searchPayments(start, end, method, status, search);
            }

            @Override
            protected void done() {
                try {
                    currentList = get();
                    tableModel.setRowCount(0);
                    BigDecimal totalRev = BigDecimal.ZERO;
                    for (Payment p : currentList) {
                        if (p.isSuccessful()) {
                            totalRev = totalRev.add(p.getAmount());
                        }
                        tableModel.addRow(new Object[]{
                                p.getPaymentCode(),
                                DateTimeUtil.formatDateTime(p.getPaymentDate()),
                                p.getMemberCode(),
                                p.getMemberName(),
                                p.getPlanName() != null ? p.getPlanName() : "-",
                                CurrencyUtil.format(p.getAmount()),
                                p.getPaymentMethod(),
                                p.getTransactionReference() != null ? p.getTransactionReference() : "-",
                                p.getPaymentStatus()
                        });
                    }

                    totalRevenueLabel.setText("Revenue: " + CurrencyUtil.format(totalRev));
                    txCountLabel.setText("Transactions: " + currentList.size());

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PaymentPanel.this,
                            "Error retrieving payments: " + ex.getMessage(),
                            "Query Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void onRecordPayment() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        PaymentFormDialog dialog = new PaymentFormDialog(parent, paymentService, memberService, membershipService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void onViewReceipt() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a payment from the table to view its receipt.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Payment selected = currentList.get(row);
        Member member = memberService.getMemberById(selected.getMemberId());
        Membership membership = null;
        if (selected.getMembershipId() != null) {
            List<Membership> msList = membershipService.getMembershipsForMember(selected.getMemberId());
            for (Membership m : msList) {
                if (m.getId().equals(selected.getMembershipId())) {
                    membership = m;
                    break;
                }
            }
        }

        ReceiptPrinter.showReceiptDialog(this, selected, member, membership);
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
