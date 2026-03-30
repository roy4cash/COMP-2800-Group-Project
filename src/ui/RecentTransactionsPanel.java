package ui;

import model.Expense;
import observer.ExpenseManager;
import observer.Observer;
import util.DbErrorFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * Dashboard card that shows the most recent transactions.
 */
public class RecentTransactionsPanel extends JPanel implements Observer {

    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_CLR = new Color(226, 232, 240);
    private static final Color LABEL_CLR = new Color(100, 116, 139);
    private static final Color TEXT_CLR = new Color(30, 41, 59);
    private static final Color ALT_BG = new Color(248, 250, 252);
    private static final Color SUCCESS = new Color(16, 185, 129);

    private final ExpenseManager manager;
    private final JPanel bodyPanel = new JPanel();

    public RecentTransactionsPanel(ExpenseManager manager) {
        this.manager = manager;
        manager.addObserver(this);

        setOpaque(false);
        setLayout(new BorderLayout());
        add(buildCard(), BorderLayout.CENTER);
        update();
    }

    private JPanel buildCard() {
        JLabel titleLabel = new JLabel("  Recent Transactions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(TEXT_CLR);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(239, 246, 255));
        titleLabel.setBorder(new EmptyBorder(10, 14, 10, 14));

        bodyPanel.setBackground(CARD_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new LineBorder(BORDER_CLR, 1, true));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(bodyPanel, BorderLayout.CENTER);
        return card;
    }

    @Override
    public void update() {
        bodyPanel.removeAll();

        String loadError = manager.getLastExpenseLoadError();
        if (loadError != null && !loadError.trim().isEmpty()) {
            bodyPanel.add(createMessageRow("Could not load recent transactions.", DbErrorFormatter.format(loadError)));
        } else {
            List<Expense> expenses = manager.getRecentExpenses(5);
            if (expenses.isEmpty()) {
                bodyPanel.add(createMessageRow("No recent transactions yet.", "Add a few expenses to populate the dashboard activity feed."));
            } else {
                int index = 0;
                for (Expense expense : expenses) {
                    bodyPanel.add(createExpenseRow(expense, index % 2 == 1));
                    index++;
                }
            }
        }

        revalidate();
        repaint();
    }

    private JPanel createExpenseRow(Expense expense, boolean alternate) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(true);
        row.setBackground(alternate ? ALT_BG : CARD_BG);
        row.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(0, 0, 0, 0),
            new EmptyBorder(10, 14, 10, 14)
        ));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel description = new JLabel(expense.getDescription());
        description.setFont(new Font("Segoe UI", Font.BOLD, 12));
        description.setForeground(TEXT_CLR);

        JLabel meta = new JLabel(expense.getDate() + " | " + expense.getCategoryName());
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        meta.setForeground(LABEL_CLR);

        left.add(description);
        left.add(Box.createVerticalStrut(2));
        left.add(meta);

        JLabel amount = new JLabel(String.format("$%.2f", expense.getAmount()));
        amount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        amount.setForeground(SUCCESS);

        row.add(left, BorderLayout.CENTER);
        row.add(amount, BorderLayout.EAST);
        return row;
    }

    private JPanel createMessageRow(String title, String body) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(CARD_BG);
        wrapper.setBorder(new EmptyBorder(28, 18, 28, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(3, 0, 3, 0);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(TEXT_CLR);
        gbc.gridy = 0;
        wrapper.add(titleLabel, gbc);

        JLabel bodyLabel = new JLabel("<html><div style='text-align:center; width:250px'>" + body + "</div></html>");
        bodyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bodyLabel.setForeground(LABEL_CLR);
        gbc.gridy = 1;
        wrapper.add(bodyLabel, gbc);

        return wrapper;
    }
}
