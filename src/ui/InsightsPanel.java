package ui;

import observer.ExpenseManager;
import observer.Observer;
import util.DbErrorFormatter;
import util.UITheme;

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
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
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
     *   2. Two rows of stat cards for current-month metrics
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
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(20, 20, 24, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // 1. Page title
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(buildHeader(), gbc);

        // 2. Stat cards section (mutable — kept as field for refresh)
        statsRow = new JPanel();
        statsRow.setOpaque(false);
        statsRow.setLayout(new BoxLayout(statsRow, BoxLayout.Y_AXIS));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        populateStats(statsRow);
        gbc.gridy = 1;
        gbc.insets = new Insets(16, 0, 0, 0);
        content.add(statsRow, gbc);

        // 3. Bar chart (mutable — kept as field for refresh)
        chartHolder = new JPanel(new BorderLayout());
        chartHolder.setOpaque(false);
        chartHolder.setPreferredSize(new Dimension(0, 340));
        populateChart(chartHolder);
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        content.add(chartHolder, gbc);

        // 4. Tips section (mutable — kept as field for refresh)
        tipsPanel = new JPanel();
        tipsPanel.setOpaque(false);
        populateTips(tipsPanel);
        gbc.gridy = 3;
        content.add(tipsPanel, gbc);

        JPanel bottomSpacer = new JPanel();
        bottomSpacer.setOpaque(false);
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        content.add(bottomSpacer, gbc);

        JScrollPane scroll = new JScrollPane(content);
        UITheme.styleScrollPane(scroll);
        add(scroll, BorderLayout.CENTER);
    }

    // ----------------------------------------------------------------
    // Section builders
    // ----------------------------------------------------------------

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        header.add(UITheme.pageTitle("Spending Insights"));
        header.add(Box.createVerticalStrut(4));
        header.add(UITheme.pageSubtitle("Current-month trends, category breakdown, and simple spending guidance."));
        return header;
    }

    // ---- Stat cards ----

    /** Populates the 4-card statistics row with live data. */
    private void populateStats(JPanel row) {
        row.removeAll();

        JPanel topRow = buildStatsGridRow();
        JPanel bottomRow = buildStatsGridRow();

        List<model.Expense> currentMonthExpenses = manager.getCurrentMonthExpenses();
        String topCat   = manager.getTopCategory();
        double avgDaily = manager.getAverageDailySpend();
        double total    = manager.getTotalSpentThisMonth();
        int    count    = manager.getExpenseCount();
        double avgTransaction = count == 0 ? 0 : total / count;
        double projected = getProjectedMonthEndSpending(avgDaily);
        model.Expense largestExpense = getLargestExpense(currentMonthExpenses);
        String riskLevel = getBudgetRiskLevel(projected, total);
        Color riskColor = getBudgetRiskColor(riskLevel);

        if (hasAnalyticsLoadIssue()) {
            topRow.add(UITheme.statCard("Top Category", createStatValue("Unavailable"),
                "Database connection required", PRIMARY));
            topRow.add(UITheme.statCard("Avg Daily Spend", createStatValue("Unavailable"),
                "Database connection required", SUCCESS));
            topRow.add(UITheme.statCard("Total This Month", createStatValue("Unavailable"),
                "Database connection required", WARNING));
            topRow.add(UITheme.statCard("Transactions", createStatValue("Unavailable"),
                "Database connection required", new Color(139, 92, 246)));
            bottomRow.add(UITheme.statCard("Avg Transaction", createStatValue("Unavailable"),
                "Database connection required", PRIMARY));
            bottomRow.add(UITheme.statCard("Largest Expense", createStatValue("Unavailable"),
                "Database connection required", DANGER));
            bottomRow.add(UITheme.statCard("Projected Month-End", createStatValue("Unavailable"),
                "Database connection required", new Color(14, 165, 233)));
            bottomRow.add(UITheme.statCard("Budget Risk", createStatValue("Unavailable"),
                "Database connection required", WARNING));
            row.add(topRow);
            row.add(Box.createVerticalStrut(16));
            row.add(bottomRow);
            return;
        }

        topRow.add(UITheme.statCard("Top Category", createStatValue(topCat),
            String.format("%.0f%% share of monthly spend", topCategoryPercent(topCat, total)), PRIMARY));
        topRow.add(UITheme.statCard("Avg Daily Spend", createStatValue(String.format("$%.2f", avgDaily)),
            "average per day this month", SUCCESS));
        topRow.add(UITheme.statCard("Total This Month", createStatValue(String.format("$%.2f", total)),
            "current-month spending", WARNING));
        topRow.add(UITheme.statCard("Transactions", createStatValue(String.valueOf(count)),
            count == 1 ? "expense recorded" : "expense records", new Color(139, 92, 246)));

        bottomRow.add(UITheme.statCard("Avg Transaction", createStatValue(String.format("$%.2f", avgTransaction)),
            count == 0 ? "No current-month transactions" : "average transaction size", PRIMARY));

        JLabel largestLabel = createStatValue(largestExpense == null ? "None" : String.format("$%.2f", largestExpense.getAmount()));
        if (largestExpense != null) {
            largestLabel.setForeground(DANGER);
        }
        bottomRow.add(UITheme.statCard("Largest Expense", largestLabel,
            largestExpense == null ? "No current-month expense yet" : largestExpense.getCategoryName() + " | " + largestExpense.getDate(),
            DANGER));

        bottomRow.add(UITheme.statCard("Projected Month-End", createStatValue(String.format("$%.2f", projected)),
            "based on current daily pace", new Color(14, 165, 233)));

        JLabel riskLabel = createStatValue(riskLevel);
        riskLabel.setForeground(riskColor);
        bottomRow.add(UITheme.statCard("Budget Risk", riskLabel,
            getBudgetRiskSubtext(projected, total), riskColor));

        row.add(topRow);
        row.add(Box.createVerticalStrut(16));
        row.add(bottomRow);
    }

    private JLabel createStatValue(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_CLR);
        return label;
    }

    private JPanel buildStatsGridRow() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 16, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        return panel;
    }

    // ---- Bar chart ----

    /** Populates the chart container with a JFreeChart bar chart. */
    private void populateChart(JPanel holder) {
        holder.removeAll();

        Map<String, Double> spending = manager.getSpendingByCategory();

        // Title card wrapper
        JPanel wrapper = createSectionCard("Top 5 Categories This Month");
        wrapper.setLayout(new BorderLayout());

        if (hasAnalyticsLoadIssue()) {
            JLabel empty = new JLabel(DbErrorFormatter.format(getAnalyticsError()), SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            empty.setForeground(LABEL_CLR);
            wrapper.add(empty, BorderLayout.CENTER);
        } else if (spending.isEmpty()) {
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
                null,              // x-axis label
                "Amount ($)",      // y-axis label
                dataset,
                PlotOrientation.HORIZONTAL,
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
            plot.setDomainGridlinesVisible(false);
            plot.setInsets(new RectangleInsets(8, 12, 8, 16));

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, PRIMARY);
            renderer.setDrawBarOutline(false);
            renderer.setShadowVisible(false);
            renderer.setBarPainter(new StandardBarPainter());
            renderer.setMaximumBarWidth(0.18);
            renderer.setItemMargin(0.18);

            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setLabel(null);
            domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
            domainAxis.setAxisLinePaint(BORDER_CLR);
            domainAxis.setTickMarksVisible(false);

            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
            rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
            rangeAxis.setNumberFormatOverride(new java.text.DecimalFormat("$#,##0.00"));
            rangeAxis.setAxisLinePaint(BORDER_CLR);

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(600, 280));
            chartPanel.setBorder(new EmptyBorder(8, 12, 2, 12));
            chartPanel.setBackground(CARD_BG);
            wrapper.add(chartPanel, BorderLayout.CENTER);

            JLabel footnote = new JLabel("Showing up to 5 categories for the current month.");
            footnote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            footnote.setForeground(LABEL_CLR);
            footnote.setBorder(new EmptyBorder(0, 16, 12, 16));
            wrapper.add(footnote, BorderLayout.SOUTH);
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
        for (int i = 0; i < tips.size(); i++) {
            JPanel tipRow = new JPanel(new BorderLayout());
            tipRow.setOpaque(true);
            tipRow.setBackground(i % 2 == 0 ? CARD_BG : new Color(248, 250, 252));
            tipRow.setBorder(BorderFactory.createCompoundBorder(
                i < tips.size() - 1 ? new MatteBorder(0, 0, 1, 0, BORDER_CLR) : new EmptyBorder(0, 0, 0, 0),
                new EmptyBorder(10, 14, 10, 14)));
            JLabel tipLabel = new JLabel("<html><div style='width:720px'><b>Tip " + (i + 1) + ":</b> " + tips.get(i) + "</div></html>");
            tipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tipLabel.setForeground(TEXT_CLR);
            tipRow.add(tipLabel, BorderLayout.CENTER);
            wrapper.add(tipRow);
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
        if (hasAnalyticsLoadIssue()) {
            tips.add(DbErrorFormatter.format(getAnalyticsError()));
            tips.add("Once MySQL is connected, this page will show current-month trends and category insights.");
            return tips;
        }

        List<model.Expense> currentMonthExpenses = manager.getCurrentMonthExpenses();
        double total  = manager.getTotalSpentThisMonth();
        String topCat = manager.getTopCategory();
        double avg    = manager.getAverageDailySpend();
        int count     = manager.getExpenseCount();
        double projected = getProjectedMonthEndSpending(avg);
        model.Expense largestExpense = getLargestExpense(currentMonthExpenses);
        String riskLevel = getBudgetRiskLevel(projected, total);
        double averageTransaction = count == 0 ? 0 : total / count;

        if (!"None".equals(topCat)) {
            tips.add("Your highest spending category this month is <b>" + topCat +
                     "</b>. It currently accounts for <b>" +
                     String.format("%.0f%%</b> of your monthly spend.", topCategoryPercent(topCat, total)));
        } else {
            tips.add("Start tracking your expenses to unlock personalized insights " +
                     "about where your money is going.");
        }

        if (count > 0) {
            tips.add(String.format(
                "Your average transaction size is <b>$%.2f</b> across <b>%d</b> current-month transactions.",
                averageTransaction, count
            ));
        }

        if (avg > 0) {
            tips.add(String.format(
                "At your current daily spend of <b>$%.2f</b>, you are on track to spend " +
                "<b>$%.2f</b> this month. Budget ahead to avoid surprises.", avg, projected));
        }

        if (largestExpense != null) {
            tips.add(String.format(
                "Your largest expense this month was <b>$%.2f</b> on <b>%s</b> (%s).",
                largestExpense.getAmount(),
                largestExpense.getDate(),
                largestExpense.getCategoryName()
            ));
        }

        tips.add(getBudgetRiskMessage(riskLevel, projected, total));

        return tips;
    }

    private double getProjectedMonthEndSpending(double averageDailySpend) {
        LocalDate now = LocalDate.now();
        return averageDailySpend * now.lengthOfMonth();
    }

    private model.Expense getLargestExpense(List<model.Expense> expenses) {
        model.Expense largest = null;
        for (model.Expense expense : expenses) {
            if (largest == null || expense.getAmount() > largest.getAmount()) {
                largest = expense;
            }
        }
        return largest;
    }

    private String getBudgetRiskLevel(double projected, double total) {
        double budget = manager.getCurrentBudget().getAmount();
        if (budget <= 0) {
            return "No Budget";
        }
        if (total >= budget) {
            return "Over Budget";
        }
        if (projected >= budget) {
            return "High Risk";
        }
        if (projected >= budget * 0.9) {
            return "Watch";
        }
        return "On Track";
    }

    private String getBudgetRiskSubtext(double projected, double total) {
        double budget = manager.getCurrentBudget().getAmount();
        if (budget <= 0) {
            return "Set a monthly budget to unlock risk tracking";
        }
        return String.format("Projected $%.2f vs budget $%.2f", projected, budget);
    }

    private String getBudgetRiskMessage(String riskLevel, double projected, double total) {
        double budget = manager.getCurrentBudget().getAmount();
        if (budget <= 0) {
            return "Set a monthly budget to compare your current pace against a spending target.";
        }
        if ("Over Budget".equals(riskLevel)) {
            return String.format("You are already over budget. Spending is <b>$%.2f</b> against a budget of <b>$%.2f</b>.", total, budget);
        }
        if ("High Risk".equals(riskLevel)) {
            return String.format("Your current pace puts you at <b>$%.2f</b> by month-end, which is above your <b>$%.2f</b> budget.", projected, budget);
        }
        if ("Watch".equals(riskLevel)) {
            return String.format("You are close to your spending limit. Projected month-end spend is <b>$%.2f</b> versus a budget of <b>$%.2f</b>.", projected, budget);
        }
        return String.format("You are currently on track to stay below budget. Projected month-end spend is <b>$%.2f</b> versus a budget of <b>$%.2f</b>.", projected, budget);
    }

    private Color getBudgetRiskColor(String riskLevel) {
        if ("Over Budget".equals(riskLevel)) {
            return DANGER;
        }
        if ("High Risk".equals(riskLevel) || "Watch".equals(riskLevel)) {
            return WARNING;
        }
        if ("No Budget".equals(riskLevel)) {
            return LABEL_CLR;
        }
        return SUCCESS;
    }

    private boolean hasAnalyticsLoadIssue() {
        return getAnalyticsError() != null;
    }

    private String getAnalyticsError() {
        String expenseError = manager.getLastExpenseLoadError();
        if (expenseError != null && !expenseError.trim().isEmpty()) {
            return expenseError;
        }
        String aggregateError = manager.getLastAggregateLoadError();
        if (aggregateError != null && !aggregateError.trim().isEmpty()) {
            return aggregateError;
        }
        return null;
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
