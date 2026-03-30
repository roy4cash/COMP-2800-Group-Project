/**
 * File: SummaryPanel.java
 * Purpose: Displays budget, spending, and remaining-balance summary cards.
 *
 * It is responsible for turning raw monthly numbers into visual budget progress
 * indicators that can be understood at a glance.
 */
package ui;

import model.Budget;
import observer.ExpenseManager;
import observer.Observer;

import util.DbErrorFormatter;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Shows three stat cards: Budget, Total Spent, and Remaining for the current month.
 * Cards change colour based on budget status.
 */
public class SummaryPanel extends JPanel implements Observer {

    private static final Color CARD_BG    = Color.WHITE;
    private static final Color BORDER_CLR = new Color(226, 232, 240);
    private static final Color PRIMARY    = new Color(37,  99,  235);
    private static final Color SUCCESS    = new Color(16,  185, 129);
    private static final Color DANGER     = new Color(239, 68,  68);
    private static final Color WARNING    = new Color(245, 158, 11);
    private static final Color MUTED      = new Color(100, 116, 139);
    private static final Color TEXT       = new Color(30,  41,  59);

    private final ExpenseManager manager;

    private final JLabel budgetValue    = new JLabel();
    private final JLabel spentValue     = new JLabel();
    private final JLabel remainingValue = new JLabel();
    private final JLabel progressLabel  = new JLabel();
    private final JProgressBar progressBar = new JProgressBar(0, 100);

    /** Creates the three dashboard summary cards and subscribes the panel to updates. */
    public SummaryPanel(ExpenseManager manager) {
        this.manager = manager;
        manager.addObserver(this);

        setOpaque(false);
        setLayout(new GridLayout(1, 3, 14, 0));
        setBorder(new EmptyBorder(0, 0, 14, 0));

        add(buildCard("Monthly Budget", budgetValue, PRIMARY, "$0.00"));
        add(buildCard("Total Spent",    spentValue,  DANGER,  "$0.00"));
        add(buildRemainingCard());

        update();
    }

    /**
     * Creates a stat card using UITheme.statCard().
     * The accent colour appears as a left border stripe.
     */
    private JPanel buildCard(String title, JLabel valueLabel, Color accent, String defaultVal) {
        valueLabel.setText(defaultVal);
        valueLabel.setForeground(accent);
        return UITheme.statCard(title, valueLabel, "", accent);
    }

    /**
     * The "Remaining" card extends the standard stat card with a thin
     * progress bar so users can instantly see how much budget is left.
     */
    private JPanel buildRemainingCard() {
        // Build the base stat card (accent colour will be overridden in update())
        JPanel card = UITheme.statCard("Remaining", remainingValue, "of monthly budget", SUCCESS);

        // Inject the progress bar + label into the card below the value
        progressBar.setStringPainted(false);
        progressBar.setBorderPainted(false);
        progressBar.setForeground(SUCCESS);
        progressBar.setBackground(UITheme.BORDER);
        progressBar.setPreferredSize(new Dimension(0, 8));

        progressLabel.setFont(UITheme.FONT_SMALL);
        progressLabel.setForeground(UITheme.LABEL);

        JPanel barRow = new JPanel(new BorderLayout(0, 3));
        barRow.setOpaque(false);
        barRow.add(progressBar,   BorderLayout.NORTH);
        barRow.add(progressLabel, BorderLayout.SOUTH);

        // The card uses GridBagLayout; add the bar as a 4th row
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0;
        gbc.gridy   = 3;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets  = new Insets(6, 0, 0, 0);
        card.add(barRow, gbc);

        return card;
    }

    @Override
    /**
     * Recalculates monthly budget, spent, and remaining values.
     *
     * This method also updates the progress bar so the user can understand
     * budget status at a glance without reading every number in detail.
     */
    public void update() {
        Budget budget    = manager.getCurrentBudget();
        double spent     = manager.getTotalSpentThisMonth();
        String budgetError = manager.getLastBudgetLoadError();
        String aggregateError = manager.getLastAggregateLoadError();

        if ((budgetError != null && !budgetError.trim().isEmpty())
                || (aggregateError != null && !aggregateError.trim().isEmpty())) {
            budgetValue.setText("Unavailable");
            spentValue.setText("Unavailable");
            remainingValue.setText("Unavailable");
            budgetValue.setForeground(WARNING);
            spentValue.setForeground(WARNING);
            remainingValue.setForeground(WARNING);
            progressBar.setValue(0);
            progressBar.setForeground(WARNING);
            progressLabel.setText(DbErrorFormatter.format(
                budgetError != null && !budgetError.trim().isEmpty() ? budgetError : aggregateError
            ));
            return;
        }

        double budgetAmt = budget.getAmount();
        double remaining = budgetAmt - spent;
        double percentUsed = budgetAmt > 0 ? (spent / budgetAmt) * 100.0 : 0.0;
        int progressValue = budgetAmt > 0 ? (int) Math.min(percentUsed, 100.0) : 0;

        budgetValue.setText(String.format("$%.2f", budgetAmt));
        spentValue.setText( String.format("$%.2f", spent));
        remainingValue.setText(String.format("$%.2f", remaining));

        // Colour remaining based on status
        if (remaining < 0) {
            remainingValue.setForeground(DANGER);
            progressBar.setForeground(DANGER);
        } else if (budgetAmt > 0 && percentUsed >= 80.0) {
            remainingValue.setForeground(WARNING);
            progressBar.setForeground(WARNING);
        } else {
            remainingValue.setForeground(SUCCESS);
            progressBar.setForeground(SUCCESS);
        }

        progressBar.setValue(progressValue);
        if (budgetAmt <= 0) {
            progressLabel.setText("Set a monthly budget to track progress");
        } else if (remaining < 0) {
            progressLabel.setText(String.format(
                "%.0f%% used | %s over budget | %s spent of %s",
                percentUsed,
                String.format("$%.2f", Math.abs(remaining)),
                String.format("$%.2f", spent),
                String.format("$%.2f", budgetAmt)
            ));
        } else {
            progressLabel.setText(String.format(
                "%.0f%% used | %s spent of %s",
                percentUsed,
                String.format("$%.2f", spent),
                String.format("$%.2f", budgetAmt)
            ));
        }
    }
}
