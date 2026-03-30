/**
 * File: DashboardPanel.java
 * Purpose: Combines high-level budget, chart, and activity widgets into one page.
 *
 * This panel is intentionally compositional: it arranges smaller observer-aware
 * panels rather than re-implementing their logic.
 */
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

    /**
     * Builds the dashboard from smaller reusable panels.
     *
     * This composition keeps each widget focused on one responsibility while
     * still giving the dashboard a richer, product-like layout.
     */
    public DashboardPanel(ExpenseManager manager) {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        AlertPanel   alertPanel   = new AlertPanel(manager);
        SummaryPanel summaryPanel = new SummaryPanel(manager);
        ChartPanel   chartPanel   = new ChartPanel(manager);
        TopCategoriesPanel topCategoriesPanel = new TopCategoriesPanel(manager);
        RecentTransactionsPanel recentTransactionsPanel = new RecentTransactionsPanel(manager);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(buildHeader());
        content.add(Box.createVerticalStrut(16));
        content.add(alertPanel);
        content.add(summaryPanel);
        content.add(Box.createVerticalStrut(16));

        JPanel analyticsRow = new JPanel(new GridBagLayout());
        analyticsRow.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.weightx = 0.62;
        chartPanel.setPreferredSize(new Dimension(0, 420));
        analyticsRow.add(chartPanel, gbc);

        JPanel sideColumn = new JPanel();
        sideColumn.setOpaque(false);
        sideColumn.setLayout(new GridLayout(2, 1, 0, 16));
        sideColumn.add(topCategoriesPanel);
        sideColumn.add(recentTransactionsPanel);

        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 16, 0, 0);
        analyticsRow.add(sideColumn, gbc);

        content.add(analyticsRow);

        add(content, BorderLayout.CENTER);
    }

    /** Builds the title and subtitle shown at the top of the dashboard page. */
    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = util.UITheme.pageTitle("Dashboard");
        JLabel subtitle = util.UITheme.pageSubtitle(
            "Live budget status, category trends, and recent activity in one place."
        );

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        return header;
    }
}
