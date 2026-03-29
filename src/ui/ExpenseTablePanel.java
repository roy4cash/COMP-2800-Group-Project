package ui;

import model.Expense;
import observer.ExpenseManager;
import observer.Observer;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Displays all expenses in a styled, scrollable table.
 * Implements Observer — refreshes automatically whenever data changes.
 * The hidden ID column is used to identify which row to delete.
 */
public class ExpenseTablePanel extends JPanel implements Observer {

    private static final Color DANGER     = new Color(239, 68,  68);
    private static final Color BG         = new Color(248, 250, 252);
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color HEADER_BG  = new Color(30,  41,  59);
    private static final Color ROW_ALT    = new Color(241, 245, 249);
    private static final Color BORDER_CLR = new Color(226, 232, 240);
    private static final Color TEXT_CLR   = new Color(30,  41,  59);
    private static final Color MUTED      = new Color(100, 116, 139);
    private static final Color SEL_BG     = new Color(219, 234, 254);

    private static final int ID_COLUMN = 0;

    private final ExpenseManager    manager;
    private final DefaultTableModel tableModel;
    private final JTable            table;
    private       JLabel            countLabel;

    public ExpenseTablePanel(ExpenseManager manager) {
        this.manager = manager;
        manager.addObserver(this);

        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        tableModel = buildTableModel();
        table      = buildTable();

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        update();
    }

    // ----------------------------------------------------------------
    // Build helpers
    // ----------------------------------------------------------------

    private DefaultTableModel buildTableModel() {
        String[] columns = {"ID", "Date", "Category", "Description", "Amount ($)"};
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable buildTable() {
        JTable t = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : ROW_ALT);
                    c.setForeground(TEXT_CLR);
                } else {
                    c.setBackground(SEL_BG);
                    c.setForeground(TEXT_CLR);
                }
                return c;
            }
        };

        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(36);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(BORDER_CLR);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setIntercellSpacing(new Dimension(0, 0));

        // Style header
        JTableHeader header = t.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        // Hide ID column
        TableColumn idCol = t.getColumnModel().getColumn(ID_COLUMN);
        idCol.setMinWidth(0); idCol.setMaxWidth(0); idCol.setWidth(0);

        // Column widths
        t.getColumnModel().getColumn(1).setPreferredWidth(110);
        t.getColumnModel().getColumn(2).setPreferredWidth(130);
        t.getColumnModel().getColumn(3).setPreferredWidth(350);
        t.getColumnModel().getColumn(4).setPreferredWidth(110);

        // Right-align amount column
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        t.getColumnModel().getColumn(4).setCellRenderer(rightAlign);

        // Centre-align date column
        DefaultTableCellRenderer centreAlign = new DefaultTableCellRenderer();
        centreAlign.setHorizontalAlignment(SwingConstants.CENTER);
        t.getColumnModel().getColumn(1).setCellRenderer(centreAlign);

        return t;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("Transaction History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_CLR);

        countLabel = new JLabel("");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(MUTED);

        bar.add(title,      BorderLayout.WEST);
        bar.add(countLabel, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTableCard() {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER_CLR, 1, true));
        scroll.getViewport().setBackground(CARD_BG);
        return scroll;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Use UITheme.button() so Delete gets the same hover effect as all other buttons
        JButton deleteBtn = UITheme.button("Delete Selected", UITheme.DANGER);
        deleteBtn.setPreferredSize(new Dimension(160, 36));
        deleteBtn.addActionListener(e -> deleteSelectedExpense());

        bar.add(deleteBtn);
        return bar;
    }

    // ----------------------------------------------------------------
    // Event handling
    // ----------------------------------------------------------------

    private void deleteSelectedExpense() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an expense to delete.", "No Selection",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, "Delete this expense?", "Confirm Delete", JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            int expenseId = (int) tableModel.getValueAt(selectedRow, ID_COLUMN);
            manager.deleteExpense(expenseId);
        }
    }

    // ----------------------------------------------------------------
    // Observer
    // ----------------------------------------------------------------

    @Override
    public void update() {
        tableModel.setRowCount(0);

        List<Expense> expenses = manager.getAllExpenses();
        for (Expense e : expenses) {
            tableModel.addRow(new Object[]{
                e.getId(),
                e.getDate().toString(),
                e.getCategoryName(),
                e.getDescription(),
                String.format("$%.2f", e.getAmount())
            });
        }

        int count = expenses.size();
        countLabel.setText(count + (count == 1 ? " transaction" : " transactions"));
    }
}
