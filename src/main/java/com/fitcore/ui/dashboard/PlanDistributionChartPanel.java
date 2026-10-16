package com.fitcore.ui.dashboard;

import com.fitcore.model.PlanDistribution;
import com.fitcore.ui.theme.Theme;
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
            Theme.ACCENT_PRIMARY,   // Electric Indigo (#6366F1)
            Theme.ACCENT_EMERALD,   // Neon Emerald (#10B981)
            Theme.ACCENT_CYAN,      // Cyber Cyan (#06B6D4)
            Theme.ACCENT_AMBER,     // Amber Gold (#F59E0B)
            Theme.ACCENT_PURPLE,    // Neon Purple (#A855F7)
            Theme.ACCENT_ROSE       // Vibrant Rose (#F43F5E)
    };

    public PlanDistributionChartPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JFreeChart chart = ChartFactory.createPieChart(
                "Membership Plan Distribution",
                dataset,
                true, // legend
                true, // tooltips
                false // urls
        );

        chart.setBackgroundPaint(Theme.BG_CARD);
        chart.getTitle().setFont(Theme.bold(14));
        chart.getTitle().setPaint(Theme.TEXT_PRIMARY);

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(Theme.BG_CARD);
            chart.getLegend().setItemFont(Theme.font(11));
            chart.getLegend().setItemPaint(Theme.TEXT_SECONDARY);
        }

        plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Theme.BG_CARD);
        plot.setOutlineVisible(false);
        plot.setLabelFont(Theme.font(11));
        plot.setLabelPaint(Theme.TEXT_PRIMARY);
        plot.setLabelBackgroundPaint(new Color(24, 33, 53));
        plot.setLabelOutlinePaint(Theme.BORDER_SUBTLE);
        plot.setShadowPaint(null);

        chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(Theme.BG_CARD);
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
            plot.setSectionPaint("No Subscriptions Recorded", Theme.BORDER_SUBTLE);
        }
    }
}
