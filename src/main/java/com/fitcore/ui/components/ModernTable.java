package com.fitcore.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Polished modern JTable with generous row padding, zebra striping, and custom headers.
 */
public class ModernTable extends JTable {

    private static final Color HEADER_BG = new Color(241, 245, 249); // slate-100
    private static final Color HEADER_FG = new Color(30, 41, 59);    // slate-800
    private static final Color ROW_ALT_BG = new Color(248, 250, 252); // slate-50
    private static final Color ROW_HOVER_BG = new Color(241, 245, 249);
    private static final Color SELECTION_BG = new Color(224, 231, 255); // indigo-100
    private static final Color SELECTION_FG = new Color(15, 23, 42);    // navy-900

    public ModernTable(TableModel model) {
        super(model);
        initStyle();
    }

    public ModernTable() {
        super();
        initStyle();
    }

    private void initStyle() {
        setRowHeight(38);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 1));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setSelectionBackground(SELECTION_BG);
        setSelectionForeground(SELECTION_FG);
        setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Configure header
        JTableHeader header = getTableHeader();
        header.setPreferredSize(new Dimension(0, 42));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(HEADER_BG);
        header.setForeground(HEADER_FG);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setBackground(HEADER_BG);
                l.setForeground(HEADER_FG);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return l;
            }
        };
        header.setDefaultRenderer(headerRenderer);

        // Configure default cell padding
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT_BG);
                    c.setForeground(new Color(30, 41, 59));
                }
                return c;
            }
        });
    }

    /**
     * Helper to wrap this table in a standard modern styled scroll pane.
     */
    public JScrollPane createScrollPane() {
        JScrollPane scroll = new JScrollPane(this);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }
}
