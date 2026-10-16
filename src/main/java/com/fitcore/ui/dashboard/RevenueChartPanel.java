package com.fitcore.ui.dashboard;

import com.fitcore.model.MonthlyRevenue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Chart component rendering monthly revenue collections using JFreeChart.
 */
public class RevenueChartPanel extends JPanel {

    private final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    private final ChartPanel chartPanel;

    public RevenueChartPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JFreeChart chart = ChartFactory.createBarChart(
                "Monthly Revenue Trends ($)",
                "Month",
                "Total Collected ($)",
                dataset,
                PlotOrientation.VERTICAL,
                false, // legend
                true,  // tooltips
                false  // urls
        );

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
        chart.getTitle().setPaint(new Color(15, 23, 42));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 250, 252));
        plot.setDomainGridlinePaint(new Color(226, 232, 240));
        plot.setRangeGridlinePaint(new Color(226, 232, 240));
        plot.setOutlineVisible(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        domainAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 12));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        rangeAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 12));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new Color(37, 99, 235)); // electric blue
        renderer.setMaximumBarWidth(0.08);

        chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setDisplayToolTips(true);
        add(chartPanel, BorderLayout.CENTER);
    }

    public void updateData(List<MonthlyRevenue> data) {
        dataset.clear();
        if (data != null && !data.isEmpty()) {
            for (MonthlyRevenue rev : data) {
                dataset.addValue(
                        rev.getTotalCollected() != null ? rev.getTotalCollected().doubleValue() : 0.0,
                        "Revenue",
                        rev.getMonthLabel() != null ? rev.getMonthLabel() : rev.getMonthPeriod()
                );
            }
        } else {
            dataset.addValue(0, "Revenue", "No Data");
        }
    }
}
