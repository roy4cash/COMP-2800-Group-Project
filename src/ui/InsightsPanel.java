package ui;

import observer.ExpenseManager;
import observer.Observer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * InsightsPanel — shows spending insights for the current month.
 *
 * Implements Observer so it refreshes automatically whenever
 * an expense is added or deleted via ExpenseManager.
 *
 * Layout (top to bottom):
 *   1. Page title
 *   2. Row of 4 stat cards: Top Category, Avg Daily Spend, Total This Month, Transactions
 *   3. Bar chart of top 5 spending categories (JFreeChart)
 *   4. Personalized financial tips section
 */
public class InsightsPanel extends JPanel implements Observer {

    // ---- Colour scheme ----
    private static final Color PRIMARY    = new Color(37,  99,  235);
    private static final Color SUCCESS    = new Color(16,  185, 129);
    private static final Color DANGER     = new Color(239, 68,  68);
    private static final Color WARNING    = new Color(245, 158, 11);
    private static final Color BG         = new Color(248, 250, 252);
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color BORDER_CLR = new Color(226, 232, 240);
    private static final Color LABEL_CLR  = new Color(100, 116, 139);
    private static final Color TEXT_CLR   = new Color(30,  41,  59);

    private final ExpenseManager manager;

    // ---- Dynamic regions that need refreshing ----
    private JPanel statsRow;
    private JPanel chartHolder;
    private JPanel tipsPanel;

    public InsightsPanel(ExpenseManager manager) {
        this.manager = manager;
        setBackground(BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    // ----------------------------------------------------------------
    // Observer callback — called when any expense/budget changes
    // ----------------------------------------------------------------

    @Override
    public void update() {
        // Rebuild the dynamic sections in-place
        refreshStats();
        refreshChart();
        refreshTips();
        revalidate();
        repaint();
    }

    // ----------------------------------------------------------------
    // Initial UI construction
    // ----------------------------------------------------------------

    private void buildUI() {
        // Outer scroll pane so nothing gets clipped on small windows
        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Page title
        content.add(buildTitle());
        content.add(Box.createVerticalStrut(16));

        // 2. Stat cards row (mutable — kept as field for refresh)
        statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setBackground(BG);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        populateStats(statsRow);
        content.add(statsRow);
        content.add(Box.createVerticalStrut(20));

        // 3. Bar chart (mutable — kept as field for refresh)
        chartHolder = new JPanel(new BorderLayout());
        chartHolder.setBackground(BG);
        chartHolder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        chartHolder.setPreferredSize(new Dimension(800, 340));
        populateChart(chartHolder);
        content.add(chartHolder);
        content.add(Box.createVerticalStrut(20));

        // 4. Tips section (mutable — kept as field for refresh)
        tipsPanel = new JPanel();
        tipsPanel.setBackground(BG);
        tipsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        populateTips(tipsPanel);
        content.add(tipsPanel);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ----------------------------------------------------------------
    // Section builders
    // ----------------------------------------------------------------

    private JLabel buildTitle() {
        JLabel title = new JLabel("Spending Insights");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_CLR);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        return title;
    }

    // ---- Stat cards ----

    /** Populates the 4-card statistics row with live data. */
    private void populateStats(JPanel row) {
        row.removeAll();

        String topCat   = manager.getTopCategory();
        double avgDaily = manager.getAverageDailySpend();
        double total    = manager.getTotalSpentThisMonth();
        int    count    = manager.getExpenseCount();

        row.add(buildStatCard("Top Category",     topCat,
                              String.format("%.0f%% of spending", topCategoryPercent(topCat, total)),
                              PRIMARY));
        row.add(buildStatCard("Avg Daily Spend",  String.format("$%.2f", avgDaily),
                              "per day this month", SUCCESS));
        row.add(buildStatCard("Total This Month", String.format("$%.2f", total),
                              "spent so far", WARNING));
        row.add(buildStatCard("Transactions",     String.valueOf(count),
                              "expense records", new Color(139, 92, 246)));
    }

    /**
     * Builds a single stat card with a large value, a label above, and a subtext below.
     *
     * @param label    small label at the top
     * @param value    large center text
     * @param subtext  small text at the bottom
     * @param accent   left-border accent color
     */
    private JPanel buildStatCard(String label, String value, String subtext, Color accent) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_CLR, 1, true),
            new EmptyBorder(14, 16, 14, 16)));

        // Colored left accent bar
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR, 1, false),
                new EmptyBorder(12, 14, 12, 14))));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(LABEL_CLR);
        gbc.gridy = 0;
        card.add(lbl, gbc);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 20));
        val.setForeground(TEXT_CLR);
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 4, 0);
        card.add(val, gbc);

        JLabel sub = new JLabel(subtext);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(LABEL_CLR);
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 0, 0);
        card.add(sub, gbc);

        return card;
    }

    // ---- Bar chart ----

    /** Populates the chart container with a JFreeChart bar chart. */
    private void populateChart(JPanel holder) {
        holder.removeAll();

        Map<String, Double> spending = manager.getSpendingByCategory();

        // Title card wrapper
        JPanel wrapper = createSectionCard("Top 5 Categories This Month");
        wrapper.setLayout(new BorderLayout());

        if (spending.isEmpty()) {
            JLabel empty = new JLabel("No spending data for this month yet.", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            empty.setForeground(LABEL_CLR);
            wrapper.add(empty, BorderLayout.CENTER);
        } else {
            // Take top 5 by spending amount
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            spending.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> dataset.addValue(e.getValue(), "Spending", e.getKey()));

            JFreeChart chart = ChartFactory.createBarChart(
                null,              // chart title (we use card title)
                "Category",        // x-axis label
                "Amount ($)",      // y-axis label
                dataset,
                PlotOrientation.VERTICAL,
                false,             // legend
                true,              // tooltips
                false              // URLs
            );

            // Styling
            chart.setBackgroundPaint(CARD_BG);
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(CARD_BG);
            plot.setOutlineVisible(false);
            plot.setRangeGridlinePaint(BORDER_CLR);

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, PRIMARY);
            renderer.setDrawBarOutline(false);
            renderer.setShadowVisible(false);
            renderer.setMaximumBarWidth(0.15);

            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
            domainAxis.setAxisLinePaint(BORDER_CLR);

            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
            rangeAxis.setNumberFormatOverride(new java.text.DecimalFormat("$#,##0.00"));

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(600, 260));
            chartPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
            chartPanel.setBackground(CARD_BG);
            wrapper.add(chartPanel, BorderLayout.CENTER);
        }

        holder.add(wrapper, BorderLayout.CENTER);
    }

    // ---- Tips section ----

    /** Populates the tips panel with 2-3 personalized financial tips. */
    private void populateTips(JPanel panel) {
        panel.removeAll();
        panel.setLayout(new BorderLayout());

        JPanel wrapper = createSectionCard("Financial Tips");
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        List<String> tips = generateTips();
        for (String tip : tips) {
            JLabel tipLabel = new JLabel("<html><div style='width:700px'>\u2022 " + tip + "</div></html>");
            tipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tipLabel.setForeground(TEXT_CLR);
            tipLabel.setBorder(new EmptyBorder(6, 4, 6, 4));
            wrapper.add(tipLabel);
        }

        panel.add(wrapper, BorderLayout.CENTER);
    }

    // ----------------------------------------------------------------
    // Refresh helpers (called by update())
    // ----------------------------------------------------------------

    private void refreshStats() {
        populateStats(statsRow);
        statsRow.revalidate();
        statsRow.repaint();
    }

    private void refreshChart() {
        populateChart(chartHolder);
        chartHolder.revalidate();
        chartHolder.repaint();
    }

    private void refreshTips() {
        populateTips(tipsPanel);
        tipsPanel.revalidate();
        tipsPanel.repaint();
    }

    // ----------------------------------------------------------------
    // Business logic helpers
    // ----------------------------------------------------------------

    /**
     * Returns what percentage of total spend the top category represents.
     * Returns 0 if total is zero or category is "None".
     */
    private double topCategoryPercent(String topCat, double total) {
        if (total == 0 || "None".equals(topCat)) return 0;
        Map<String, Double> spending = manager.getSpendingByCategory();
        Double topAmt = spending.get(topCat);
        if (topAmt == null) return 0;
        return (topAmt / total) * 100.0;
    }

    /**
     * Generates 2-3 personalized tips based on current month spending data.
     * Tips are contextual — they reference actual top categories and budget usage.
     */
    private List<String> generateTips() {
        List<String> tips = new ArrayList<>();

        double total  = manager.getTotalSpentThisMonth();
        String topCat = manager.getTopCategory();
        double avg    = manager.getAverageDailySpend();

        // Tip 1: based on top category
        if (!"None".equals(topCat)) {
            tips.add("Your highest spending category this month is <b>" + topCat +
                     "</b>. Consider reviewing those transactions to find savings.");
        } else {
            tips.add("Start tracking your expenses to unlock personalized insights " +
                     "about where your money is going.");
        }

        // Tip 2: based on average daily spend
        if (avg > 0) {
            double projected = avg * 30;
            tips.add(String.format(
                "At your current daily spend of <b>$%.2f</b>, you are on track to spend " +
                "<b>$%.2f</b> this month. Budget ahead to avoid surprises.", avg, projected));
        }

        // Tip 3: general saving tip
        if (total > 500) {
            tips.add("You have spent over $500 this month. Consider the 50/30/20 rule: " +
                     "50% needs, 30% wants, 20% savings.");
        } else {
            tips.add("Great job keeping spending low! Consider putting unspent budget " +
                     "into an emergency fund or investment.");
        }

        return tips;
    }

    // ----------------------------------------------------------------
    // Layout helper
    // ----------------------------------------------------------------

    /** Creates a white card panel with a bold section title label inside. */
    private JPanel createSectionCard(String title) {
        JLabel titleLabel = new JLabel("  " + title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(TEXT_CLR);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(239, 246, 255));
        titleLabel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new LineBorder(BORDER_CLR, 1, true));
        card.add(titleLabel, BorderLayout.NORTH);
        return card;
    }
}
