package ui;

import observer.ExpenseManager;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

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
        setSize(1240, 820);
        setMinimumSize(new Dimension(1000, 680));
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

        // Tabbed pane
        JTabbedPane tabs = buildTabs();
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

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BUTTON);
        tabs.setBackground(BG);
        tabs.setOpaque(false);
        tabs.setFocusable(false);
        tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setBorder(new EmptyBorder(10, 14, 14, 14));
        tabs.setUI(new AppTabbedPaneUI());
        return tabs;
    }

    /** Creates the dark-blue title bar at the top of the window. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("Financial Activity Tracker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 23));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Track expenses | investments | insights | budgets");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(191, 219, 254));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(title);
        text.add(sub);

        JLabel badge = new JLabel(currentMonthLabel() + "  |  Desktop Edition");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setBorder(new EmptyBorder(8, 14, 8, 14));
        badge.setOpaque(true);
        badge.setBackground(new Color(59, 130, 246));

        header.add(text, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    private String currentMonthLabel() {
        LocalDate now = LocalDate.now();
        return now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + now.getYear();
    }

    private static final class AppTabbedPaneUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(10, 18, 10, 18);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            contentBorderInsets = new Insets(12, 0, 0, 0);
        }

        @Override
        protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
            return 40;
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isSelected ? Color.WHITE : new Color(219, 234, 254));
            g2.fillRoundRect(x + 2, y + 3, w - 4, h - 4, 14, 14);
            g2.dispose();
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                      int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isSelected ? new Color(191, 219, 254) : new Color(147, 197, 253));
            g2.drawRoundRect(x + 2, y + 3, w - 5, h - 5, 14, 14);
            g2.dispose();
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                                 int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            g.setFont(font);
            g.setColor(isSelected ? UITheme.TEXT : UITheme.SLATE);
            super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
        }

        @Override protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                                     int tabIndex, Rectangle iconRect, Rectangle textRect,
                                                     boolean isSelected) {}
        @Override protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex,
                                                           int x, int y, int w, int h) {}
        @Override protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex,
                                                            int x, int y, int w, int h) {}
        @Override protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex,
                                                             int x, int y, int w, int h) {}
        @Override protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex,
                                                              int x, int y, int w, int h) {}
    }
}
