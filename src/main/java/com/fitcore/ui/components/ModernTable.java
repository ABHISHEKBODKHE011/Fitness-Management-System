package com.fitcore.ui.components;

import com.fitcore.ui.theme.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Polished modern dark JTable with generous row padding, zebra striping, and custom headers.
 */
public class ModernTable extends JTable {

    private static final Color HEADER_BG = new Color(24, 33, 53);      // slate-850 (#182135)
    private static final Color HEADER_FG = new Color(148, 163, 184);    // slate-400
    private static final Color ROW_BG_EVEN = new Color(18, 25, 41);     // deep card (#121929)
    private static final Color ROW_BG_ODD = new Color(14, 20, 33);      // deep canvas (#0E1421)
    private static final Color SELECTION_BG = new Color(99, 102, 241, 70); // Electric Indigo tint
    private static final Color SELECTION_FG = Color.WHITE;

    public ModernTable(TableModel model) {
        super(model);
        initStyle();
    }

    public ModernTable() {
        super();
        initStyle();
    }

    private void initStyle() {
        setRowHeight(42);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setSelectionBackground(SELECTION_BG);
        setSelectionForeground(SELECTION_FG);
        setFont(Theme.font(13));
        setBackground(ROW_BG_EVEN);
        setForeground(Theme.TEXT_PRIMARY);

        // Configure header
        JTableHeader header = getTableHeader();
        header.setPreferredSize(new Dimension(0, 42));
        header.setFont(Theme.bold(11));
        header.setBackground(HEADER_BG);
        header.setForeground(HEADER_FG);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setBackground(HEADER_BG);
                l.setForeground(HEADER_FG);
                l.setFont(Theme.bold(11));
                l.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                return l;
            }
        };
        header.setDefaultRenderer(headerRenderer);

        // Configure default cell padding and zebra striping
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? ROW_BG_EVEN : ROW_BG_ODD);
                    c.setForeground(Theme.TEXT_PRIMARY);
                } else {
                    c.setBackground(SELECTION_BG);
                    c.setForeground(SELECTION_FG);
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
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1));
        scroll.getViewport().setBackground(ROW_BG_EVEN);
        return scroll;
    }
}
