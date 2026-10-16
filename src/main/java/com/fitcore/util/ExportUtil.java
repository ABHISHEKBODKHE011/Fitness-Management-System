package com.fitcore.util;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * Utility for exporting table data and reports to CSV and file generation.
 */
public final class ExportUtil {

    private ExportUtil() {
    }

    /**
     * Exports a Swing JTable directly to a CSV file selected by the user.
     */
    public static void exportTableToCsv(Component parent, JTable table, String defaultFileName) {
        if (table == null || table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parent, "No data available to export.", "Export Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Data to CSV");
        fileChooser.setSelectedFile(new File(defaultFileName.endsWith(".csv") ? defaultFileName : defaultFileName + ".csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(parent);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                // Write column headers
                int columnCount = table.getColumnCount();
                for (int col = 0; col < columnCount; col++) {
                    writer.print(escapeCsv(table.getColumnName(col)));
                    if (col < columnCount - 1) {
                        writer.print(",");
                    }
                }
                writer.println();

                // Write rows
                int rowCount = table.getRowCount();
                for (int row = 0; row < rowCount; row++) {
                    for (int col = 0; col < columnCount; col++) {
                        Object val = table.getValueAt(row, col);
                        writer.print(escapeCsv(val != null ? val.toString() : ""));
                        if (col < columnCount - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                }

                JOptionPane.showMessageDialog(parent,
                        "Successfully exported " + rowCount + " records to:\n" + fileToSave.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent,
                        "Error saving CSV export: " + e.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Exports custom headers and data rows to CSV.
     */
    public static void exportDataToCsv(Component parent, String[] headers, List<String[]> rows, String defaultFileName) {
        if (rows == null || rows.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No report records to export.", "Export Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report to CSV");
        fileChooser.setSelectedFile(new File(defaultFileName.endsWith(".csv") ? defaultFileName : defaultFileName + ".csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(parent);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                // Headers
                for (int i = 0; i < headers.length; i++) {
                    writer.print(escapeCsv(headers[i]));
                    if (i < headers.length - 1) {
                        writer.print(",");
                    }
                }
                writer.println();

                // Rows
                for (String[] row : rows) {
                    for (int i = 0; i < row.length; i++) {
                        writer.print(escapeCsv(row[i]));
                        if (i < row.length - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                }

                JOptionPane.showMessageDialog(parent,
                        "Report exported successfully (" + rows.size() + " rows):\n" + fileToSave.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent,
                        "Failed to export report: " + e.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String escapeCsv(String val) {
        if (val == null) {
            return "\"\"";
        }
        val = val.replace("\"", "\"\"");
        return "\"" + val + "\"";
    }
}
