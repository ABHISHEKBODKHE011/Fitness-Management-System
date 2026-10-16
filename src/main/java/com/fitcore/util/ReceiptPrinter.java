package com.fitcore.util;

import com.fitcore.config.AppConfig;
import com.fitcore.model.Member;
import com.fitcore.model.Membership;
import com.fitcore.model.Payment;
import com.fitcore.security.SecurityContext;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;

/**
 * Utility to generate and print formatted gym payment receipts.
 */
public final class ReceiptPrinter {

    private ReceiptPrinter() {
    }

    public static String buildReceiptHtml(Payment payment, Member member, Membership membership) {
        String gymName = AppConfig.getGymName();
        String gymTagline = AppConfig.getGymTagline();
        String gymAddress = AppConfig.getGymAddress();
        String gymPhone = AppConfig.getGymPhone();
        String gymEmail = AppConfig.getGymEmail();

        String memberCode = member != null ? member.getMemberCode() : (payment != null ? payment.getMemberCode() : "-");
        String memberName = member != null ? member.getFullName() : (payment != null ? payment.getMemberName() : "-");
        String memberPhone = member != null ? member.getPhone() : "-";

        String planName = membership != null ? membership.getPlanName() : (payment != null && payment.getPlanName() != null ? payment.getPlanName() : "General Membership/Service");
        String validDates = membership != null ? (DateTimeUtil.formatDate(membership.getStartDate()) + " to " + DateTimeUtil.formatDate(membership.getEndDate())) : "N/A";

        String paymentCode = payment != null ? payment.getPaymentCode() : "RCP-0000";
        String paymentDate = payment != null ? DateTimeUtil.formatDateTime(payment.getPaymentDate()) : DateTimeUtil.formatDateTime(java.time.LocalDateTime.now());
        String paymentMethod = payment != null ? payment.getPaymentMethod() : "CASH";
        String txnRef = payment != null && payment.getTransactionReference() != null ? payment.getTransactionReference() : "N/A";
        String amountFormatted = payment != null ? CurrencyUtil.format(payment.getAmount()) : "$0.00";
        String cashier = SecurityContext.getCurrentUser() != null ? SecurityContext.getCurrentUser().getFullName() : "Front Desk Desk-1";

        return """
            <html>
            <head>
            <style>
                body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; color: #1e293b; font-size: 13px; line-height: 1.4; }
                .header { text-align: center; border-bottom: 2px solid #0f172a; padding-bottom: 12px; margin-bottom: 15px; }
                .title { font-size: 20px; font-weight: bold; color: #0f172a; margin: 0; }
                .tagline { font-size: 11px; color: #64748b; margin-top: 3px; font-style: italic; }
                .contact { font-size: 11px; color: #475569; margin-top: 4px; }
                .badge { display: inline-block; background-color: #0f172a; color: white; padding: 4px 12px; font-weight: bold; font-size: 12px; border-radius: 4px; margin-top: 8px; }
                .meta-table, .item-table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
                .meta-table td { padding: 4px 6px; font-size: 12px; vertical-align: top; }
                .item-table th { background-color: #f1f5f9; color: #334155; border: 1px solid #cbd5e1; padding: 8px; text-align: left; font-size: 12px; }
                .item-table td { border: 1px solid #e2e8f0; padding: 8px; font-size: 12px; }
                .total-row td { font-weight: bold; background-color: #f8fafc; font-size: 13px; }
                .amount { text-align: right; }
                .footer { text-align: center; font-size: 11px; color: #64748b; border-top: 1px dashed #cbd5e1; padding-top: 12px; margin-top: 20px; }
            </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">""" + gymName + """
</div>
                    <div class="tagline">""" + gymTagline + """
</div>
                    <div class="contact">""" + gymAddress + " | Tel: " + gymPhone + " | " + gymEmail + """
</div>
                    <div class="badge">OFFICIAL PAYMENT RECEIPT</div>
                </div>

                <table class="meta-table">
                    <tr>
                        <td width="55%"><strong>Receipt No:</strong> """ + paymentCode + """
<br>
                            <strong>Date & Time:</strong> """ + paymentDate + """
<br>
                            <strong>Cashier / Staff:</strong> """ + cashier + """
</td>
                        <td width="45%"><strong>Member Code:</strong> """ + memberCode + """
<br>
                            <strong>Member Name:</strong> """ + memberName + """
<br>
                            <strong>Contact:</strong> """ + memberPhone + """
</td>
                    </tr>
                </table>

                <table class="item-table">
                    <thead>
                        <tr>
                            <th width="50%">Description / Membership Plan</th>
                            <th width="30%">Validity Period</th>
                            <th width="20%" class="amount">Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><strong>""" + planName + """
</strong><br><small>Fitness facility access & gym amenities</small></td>
                            <td>""" + validDates + """
</td>
                            <td class="amount">""" + amountFormatted + """
</td>
                        </tr>
                        <tr class="total-row">
                            <td colspan="2" style="text-align: right;">Total Amount Paid:</td>
                            <td class="amount">""" + amountFormatted + """
</td>
                        </tr>
                    </tbody>
                </table>

                <table class="meta-table">
                    <tr>
                        <td><strong>Payment Mode:</strong> """ + paymentMethod + """
</td>
                        <td class="amount"><strong>Reference / Auth:</strong> """ + txnRef + """
</td>
                    </tr>
                    <tr>
                        <td colspan="2"><strong>Status:</strong> <span style="color: #16a34a; font-weight: bold;">PAID IN FULL</span></td>
                    </tr>
                </table>

                <div class="footer">
                    <p>Thank you for choosing """ + gymName + """
! Work hard, stay consistent, and achieve greatness.</p>
                    <p style="font-size: 10px; color: #94a3b8;">* All memberships are non-transferable and subject to gym code of conduct.</p>
                </div>
            </body>
            </html>
            """;
    }

    public static void showReceiptDialog(Component parent, Payment payment, Member member, Membership membership) {
        String html = buildReceiptHtml(payment, member, membership);
        JEditorPane editorPane = new JEditorPane("text/html", html);
        editorPane.setEditable(false);
        editorPane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(540, 520));

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton printButton = new JButton("Print Receipt");
        printButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        printButton.setBackground(new Color(15, 23, 42));
        printButton.setForeground(Color.WHITE);
        printButton.setFocusPainted(false);
        printButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        printButton.addActionListener(e -> {
            try {
                boolean complete = editorPane.print();
                if (complete) {
                    JOptionPane.showMessageDialog(parent, "Receipt printed successfully.", "Print Completed", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(parent, "Printing failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(printButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Receipt - " + (payment != null ? payment.getPaymentCode() : "Print"), true);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
