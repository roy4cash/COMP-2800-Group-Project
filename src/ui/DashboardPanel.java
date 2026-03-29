package ui;

import observer.ExpenseManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Dashboard tab — combines AlertPanel, SummaryPanel, and ChartPanel.
 *
 * Layout:
 *   [Alert banner — full width]
 *   [3 stat cards — full width]
 *   [Pie chart — full width]
 */
public class DashboardPanel extends JPanel {

    private static final Color BG = new Color(248, 250, 252);

    public DashboardPanel(ExpenseManager manager) {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        AlertPanel   alertPanel   = new AlertPanel(manager);
        SummaryPanel summaryPanel = new SummaryPanel(manager);
        ChartPanel   chartPanel   = new ChartPanel(manager);

        // North section: alert + stat cards stacked
        JPanel northSection = new JPanel();
        northSection.setOpaque(false);
        northSection.setLayout(new BoxLayout(northSection, BoxLayout.Y_AXIS));
        northSection.add(alertPanel);
        northSection.add(summaryPanel);

        add(northSection, BorderLayout.NORTH);
        add(chartPanel,   BorderLayout.CENTER);
    }
}
