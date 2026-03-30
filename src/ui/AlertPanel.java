/**
 * File: AlertPanel.java
 * Purpose: Displays a banner summarizing the user's current budget state.
 *
 * The panel reacts to observer notifications so budget warnings stay in sync
 * with expense and budget changes elsewhere in the app.
 */
package ui;

import model.Budget;
import observer.ExpenseManager;
import observer.Observer;
import util.DbErrorFormatter;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Displays a full-width coloured banner based on budget usage:
 *   Green  — under 80%  (on track)
 *   Yellow — 80–99%     (warning)
 *   Red    — 100%+      (over budget)
 */
public class AlertPanel extends JPanel implements Observer {

    private static final Color OK_BG      = new Color(209, 250, 229);
    private static final Color OK_FG      = new Color(6,   95,  70);
    private static final Color WARN_BG    = new Color(254, 243, 199);
    private static final Color WARN_FG    = new Color(146, 64,  14);
    private static final Color DANGER_BG  = new Color(254, 226, 226);
    private static final Color DANGER_FG  = new Color(153, 27,  27);
    private static final Color NONE_BG    = new Color(241, 245, 249);
    private static final Color NONE_FG    = new Color(100, 116, 139);

    private static final double WARNING_THRESHOLD = 0.80;

    private final ExpenseManager manager;
    private final JLabel         icon;
    private final JLabel         message;
    private final JLabel         subMessage;

    /**
     * Creates the budget alert banner and subscribes it to observer updates.
     *
     * The banner is kept as its own panel so budget-status messaging can be
     * reused cleanly inside the dashboard layout.
     */
    public AlertPanel(ExpenseManager manager) {
        this.manager    = manager;
        this.icon       = new JLabel();
        this.message    = new JLabel();
        this.subMessage = new JLabel();
        manager.addObserver(this);

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 12, 0));
        setOpaque(false);

        // Banner card
        JPanel banner = new JPanel(new BorderLayout(12, 0));
        banner.setBorder(new EmptyBorder(14, 18, 14, 18));

        icon.setFont(new Font("Segoe UI", Font.PLAIN, 22));

        message.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subMessage.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);
        textPanel.add(message);
        textPanel.add(subMessage);

        banner.add(icon,      BorderLayout.WEST);
        banner.add(textPanel, BorderLayout.CENTER);

        add(banner, BorderLayout.CENTER);

        // Store reference so update() can restyle the banner background
        putClientProperty("banner", banner);

        update();
    }

    @Override
    /**
     * Recalculates the banner state from the current budget and spending totals.
     *
     * The panel deliberately handles "no budget", "warning", and "over budget"
     * as separate cases so the message can stay clear for demo purposes.
     */
    public void update() {
        Budget budget = manager.getCurrentBudget();
        double spent  = manager.getTotalSpentThisMonth();
        JPanel banner = (JPanel) getClientProperty("banner");
        String budgetError = manager.getLastBudgetLoadError();
        String aggregateError = manager.getLastAggregateLoadError();

        if ((budgetError != null && !budgetError.trim().isEmpty())
                || (aggregateError != null && !aggregateError.trim().isEmpty())) {
            style(banner, NONE_BG, NONE_FG, "i",
                  "Budget data is unavailable.",
                  DbErrorFormatter.format(
                      budgetError != null && !budgetError.trim().isEmpty() ? budgetError : aggregateError
                  ));
            return;
        }

        if (budget.getAmount() <= 0) {
            style(banner, NONE_BG, NONE_FG, "ℹ", "No budget set for this month.",
                  "Go to Add Expense and set a monthly budget to enable tracking.");
            return;
        }

        double ratio = spent / budget.getAmount();
        int    pct   = (int) Math.round(ratio * 100);
        String spent$ = String.format("$%.2f", spent);
        String bdgt$  = String.format("$%.2f", budget.getAmount());

        if (ratio >= 1.0) {
            style(banner, DANGER_BG, DANGER_FG, "⚠",
                  "Over Budget! You have exceeded your monthly limit.",
                  String.format("Spent: %s  |  Budget: %s  |  %d%% used", spent$, bdgt$, pct));
        } else if (ratio >= WARNING_THRESHOLD) {
            style(banner, WARN_BG, WARN_FG, "⚡",
                  String.format("Warning — %d%% of your budget has been used.", pct),
                  String.format("Spent: %s  |  Remaining: $%.2f", spent$, budget.getAmount() - spent));
        } else {
            style(banner, OK_BG, OK_FG, "✓",
                  String.format("On track — %d%% of budget used.", pct),
                  String.format("Spent: %s  |  Remaining: $%.2f  |  Budget: %s", spent$,
                      budget.getAmount() - spent, bdgt$));
        }
    }

    /**
     * Applies the final colours and text for the selected alert state.
     *
     * Centralizing this styling avoids repeating the same UI update code in
     * every branch of update().
     */
    private void style(JPanel banner, Color bg, Color fg,
                       String ico, String msg, String sub) {
        banner.setBackground(bg);
        banner.setOpaque(true);
        icon.setText(ico);
        icon.setForeground(fg);
        message.setText(msg);
        message.setForeground(fg);
        subMessage.setText(sub);
        subMessage.setForeground(fg);
        repaint();
    }
}
