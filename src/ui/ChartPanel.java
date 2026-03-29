package ui;

import observer.ExpenseManager;
import observer.Observer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Map;

/**
 * Renders a styled pie chart showing spending by category for the current month.
 * Uses JFreeChart. Refreshes automatically via the Observer pattern.
 */
public class ChartPanel extends JPanel implements Observer {

    private static final Color BG         = new Color(248, 250, 252);
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color BORDER_CLR = new Color(226, 232, 240);
    private static final Color TEXT_CLR   = new Color(30,  41,  59);
    private static final Color MUTED      = new Color(100, 116, 139);

    // Palette for pie slices
    private static final Color[] PALETTE = {
        new Color(99,  102, 241), new Color(16,  185, 129),
        new Color(245, 158, 11),  new Color(239, 68,  68),
        new Color(139, 92,  246), new Color(6,   182, 212),
        new Color(249, 115, 22),  new Color(236, 72,  153)
    };

    private final ExpenseManager manager;

    public ChartPanel(ExpenseManager manager) {
        this.manager = manager;
        manager.addObserver(this);
        setLayout(new BorderLayout());
        setOpaque(false);
        update();
    }

    @Override
    public void update() {
        removeAll();

        Map<String, Double> spending = manager.getSpendingByCategory();

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_CLR, 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));

        // Card title bar
        JLabel titleLabel = new JLabel("  Spending by Category — This Month");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_CLR);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(239, 246, 255));
        titleLabel.setBorder(new EmptyBorder(12, 16, 12, 16));
        titleLabel.setPreferredSize(new Dimension(0, 46));
        card.add(titleLabel, BorderLayout.NORTH);

        if (spending.isEmpty()) {
            JLabel empty = new JLabel("No expenses recorded this month", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(MUTED);
            card.add(empty, BorderLayout.CENTER);
        } else {
            JFreeChart chart = buildChart(spending);
            org.jfree.chart.ChartPanel jfreePanel = new org.jfree.chart.ChartPanel(chart);
            jfreePanel.setBackground(CARD_BG);
            jfreePanel.setPopupMenu(null);
            jfreePanel.setPreferredSize(new Dimension(0, 340));
            card.add(jfreePanel, BorderLayout.CENTER);
        }

        add(card, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JFreeChart buildChart(Map<String, Double> spending) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Double> entry : spending.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
            null, dataset, true, true, false
        );

        chart.setBackgroundPaint(CARD_BG);
        chart.getLegend().setBackgroundPaint(CARD_BG);
        chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 12));

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(CARD_BG);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setInteriorGap(0.04);

        // Apply colour palette
        int i = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable<?>) key, PALETTE[i % PALETTE.length]);
            i++;
        }

        return chart;
    }
}
