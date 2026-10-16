package com.fitcore.ui.dashboard;

import com.fitcore.model.PlanDistribution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Chart component rendering membership plan popularity breakdown with JFreeChart pie plot.
 */
public class PlanDistributionChartPanel extends JPanel {

    private final DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
    private final ChartPanel chartPanel;
    private final PiePlot<String> plot;

    private static final Color[] PALETTE = {
            new Color(16, 185, 129), // emerald-500
            new Color(59, 130, 246), // blue-500
            new Color(245, 158, 11), // amber-500
            new Color(139, 92, 246), // violet-500
            new Color(236, 72, 153), // pink-500
            new Color(20, 184, 166)  // teal-500
    };

    public PlanDistributionChartPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JFreeChart chart = ChartFactory.createPieChart(
                "Membership Plan Distribution",
                dataset,
                true, // legend
                true, // tooltips
                false // urls
        );

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
        chart.getTitle().setPaint(new Color(15, 23, 42));

        plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setLabelBackgroundPaint(new Color(248, 250, 252));
        plot.setLabelOutlinePaint(new Color(203, 213, 225));
        plot.setShadowPaint(null);

        chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setDisplayToolTips(true);
        add(chartPanel, BorderLayout.CENTER);
    }

    public void updateData(List<PlanDistribution> data) {
        dataset.clear();
        if (data != null && !data.isEmpty()) {
            int colorIdx = 0;
            for (PlanDistribution dist : data) {
                if (dist.getTotalSubscriptions() > 0) {
                    dataset.setValue(dist.getPlanName(), dist.getTotalSubscriptions());
                    plot.setSectionPaint(dist.getPlanName(), PALETTE[colorIdx % PALETTE.length]);
                    colorIdx++;
                }
            }
        }
        if (dataset.getItemCount() == 0) {
            dataset.setValue("No Subscriptions Recorded", 1);
            plot.setSectionPaint("No Subscriptions Recorded", new Color(203, 213, 225));
        }
    }
}
