package com.fitcore.ui.dashboard;

import com.fitcore.model.MonthlyRevenue;
import com.fitcore.ui.theme.Theme;
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
import java.util.List;

/**
 * Chart component rendering monthly revenue collections using dark theme JFreeChart.
 */
public class RevenueChartPanel extends JPanel {

    private final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    private final ChartPanel chartPanel;

    public RevenueChartPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JFreeChart chart = ChartFactory.createBarChart(
                "Monthly Revenue Trends ($)",
                "Billing Period",
                "Total Collections ($)",
                dataset,
                PlotOrientation.VERTICAL,
                false, // legend
                true,  // tooltips
                false  // urls
        );

        chart.setBackgroundPaint(Theme.BG_CARD);
        chart.getTitle().setFont(Theme.bold(14));
        chart.getTitle().setPaint(Theme.TEXT_PRIMARY);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(15, 23, 42)); // #0F172A
        plot.setDomainGridlinePaint(Theme.BORDER_SUBTLE);
        plot.setRangeGridlinePaint(Theme.BORDER_SUBTLE);
        plot.setOutlineVisible(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(Theme.font(11));
        domainAxis.setTickLabelPaint(Theme.TEXT_SECONDARY);
        domainAxis.setLabelFont(Theme.bold(11));
        domainAxis.setLabelPaint(Theme.TEXT_SECONDARY);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(Theme.font(11));
        rangeAxis.setTickLabelPaint(Theme.TEXT_SECONDARY);
        rangeAxis.setLabelFont(Theme.bold(11));
        rangeAxis.setLabelPaint(Theme.TEXT_SECONDARY);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, Theme.ACCENT_PRIMARY); // Electric Indigo
        renderer.setMaximumBarWidth(0.08);

        chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(Theme.BG_CARD);
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
