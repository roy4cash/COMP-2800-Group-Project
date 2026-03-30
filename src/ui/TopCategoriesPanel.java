package ui;

import observer.ExpenseManager;
import observer.Observer;
import util.DbErrorFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Map;

/**
 * Dashboard card that highlights the top spending categories this month.
 */
public class TopCategoriesPanel extends JPanel implements Observer {

    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_CLR = new Color(226, 232, 240);
    private static final Color LABEL_CLR = new Color(100, 116, 139);
    private static final Color TEXT_CLR = new Color(30, 41, 59);
    private static final Color PRIMARY = new Color(37, 99, 235);

    private final ExpenseManager manager;
    private final JPanel bodyPanel = new JPanel();

    public TopCategoriesPanel(ExpenseManager manager) {
        this.manager = manager;
        manager.addObserver(this);

        setOpaque(false);
        setLayout(new BorderLayout());
        add(buildCard(), BorderLayout.CENTER);
        update();
    }

    private JPanel buildCard() {
        JLabel titleLabel = new JLabel("  Top Categories");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(TEXT_CLR);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(239, 246, 255));
        titleLabel.setBorder(new EmptyBorder(10, 14, 10, 14));

        bodyPanel.setBackground(CARD_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(10, 14, 12, 14));

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

        String loadError = manager.getLastAggregateLoadError();
        Map<String, Double> spending = manager.getSpendingByCategory();
        double total = manager.getTotalSpentThisMonth();

        if (loadError != null && !loadError.trim().isEmpty()) {
            bodyPanel.add(createMessageLabel(DbErrorFormatter.format(loadError)));
        } else if (spending.isEmpty() || total <= 0) {
            bodyPanel.add(createMessageLabel("Spend in a few categories to unlock your monthly category summary."));
        } else {
            int shown = 0;
            for (Map.Entry<String, Double> entry : spending.entrySet()) {
                if (shown == 4) {
                    break;
                }
                bodyPanel.add(createCategoryRow(entry.getKey(), entry.getValue(), total));
                bodyPanel.add(Box.createVerticalStrut(10));
                shown++;
            }

            JLabel footer = new JLabel(String.format("Current month total: $%.2f", total));
            footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            footer.setForeground(LABEL_CLR);
            bodyPanel.add(footer);
        }

        revalidate();
        repaint();
    }

    private JPanel createCategoryRow(String name, double amount, double total) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        double percent = total <= 0 ? 0 : (amount / total) * 100.0;

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(TEXT_CLR);

        JLabel amountLabel = new JLabel(String.format("$%.2f (%.0f%%)", amount, percent));
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        amountLabel.setForeground(LABEL_CLR);

        top.add(nameLabel, BorderLayout.WEST);
        top.add(amountLabel, BorderLayout.EAST);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) Math.min(Math.round(percent), 100));
        bar.setForeground(PRIMARY);
        bar.setBackground(new Color(226, 232, 240));
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(0, 9));

        row.add(top);
        row.add(Box.createVerticalStrut(4));
        row.add(bar);
        return row;
    }

    private JComponent createMessageLabel(String text) {
        JLabel label = new JLabel("<html><div style='width:250px; line-height:1.5'>" + text + "</div></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(LABEL_CLR);
        return label;
    }
}
