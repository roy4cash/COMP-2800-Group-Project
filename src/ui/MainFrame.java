package ui;

import observer.ExpenseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * The main application window.
 *
 * Creates one shared ExpenseManager and passes it to every panel
 * so they all work with the same data source.
 *
 * Tabs (in order):
 *   Add Expense   — expense entry form + budget setter
 *   Insights      — spending stats, bar chart, financial tips (Observer)
 *   Investments   — investment tracker with portfolio summary
 *   Transactions  — full expense table with delete
 *   Dashboard     — pie chart + budget progress bar (Observer)
 */
public class MainFrame extends JFrame {

    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color BG      = new Color(248, 250, 252);

    public MainFrame() {
        ExpenseManager manager = new ExpenseManager();

        setTitle("Financial Activity Tracker (FAT)");
        setSize(1100, 740);
        setMinimumSize(new Dimension(900, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Build panels
        AddExpensePanel   addExpensePanel   = new AddExpensePanel(manager);
        InsightsPanel     insightsPanel     = new InsightsPanel(manager);
        InvestmentPanel   investmentPanel   = new InvestmentPanel();
        ExpenseTablePanel expenseTablePanel = new ExpenseTablePanel(manager);
        DashboardPanel    dashboardPanel    = new DashboardPanel(manager);

        // Register observer panels so they refresh whenever data changes.
        // Note: DashboardPanel is NOT added here — its child panels (AlertPanel,
        // SummaryPanel, ChartPanel) each register themselves as observers in
        // their own constructors.
        manager.addObserver(insightsPanel);
        manager.addObserver(expenseTablePanel);

        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.setBackground(BG);
        tabs.addTab("  Add Expense  ",  addExpensePanel);
        tabs.addTab("  Insights  ",     insightsPanel);
        tabs.addTab("  Investments  ",  investmentPanel);
        tabs.addTab("  Transactions  ", expenseTablePanel);
        tabs.addTab("  Dashboard  ",    dashboardPanel);

        // Root layout: header on top, tabs fill the rest
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(tabs,          BorderLayout.CENTER);

        setContentPane(root);
    }

    /** Creates the dark-blue title bar at the top of the window. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(14, 22, 14, 22));

        JLabel title = new JLabel("\uD83D\uDCB0  Financial Activity Tracker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Track expenses \u00B7 Investments \u00B7 Insights \u00B7 Budgets");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(191, 219, 254));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(title);
        text.add(sub);

        header.add(text, BorderLayout.WEST);
        return header;
    }
}
