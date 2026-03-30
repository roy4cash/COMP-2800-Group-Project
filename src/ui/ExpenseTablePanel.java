/**
 * File: ExpenseTablePanel.java
 * Purpose: Displays expense history with search, filtering, sorting, and delete support.
 *
 * It is both a detailed review screen and part of the observer refresh flow,
 * which is why it reloads itself whenever shared expense data changes.
 */
package ui;

import model.Expense;
import observer.ExpenseManager;
import observer.Observer;
import util.DbErrorFormatter;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Displays all expenses in a styled, scrollable table.
 * Implements Observer — refreshes automatically whenever data changes.
 * The hidden ID column is used to identify which row to delete.
 */
public class ExpenseTablePanel extends JPanel implements Observer {

    private static final Color SUCCESS    = new Color(16, 185, 129);
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
    private final TableRowSorter<DefaultTableModel> rowSorter;
    private       JLabel            countLabel;
    private       JLabel            statusLabel;
    private       JButton           deleteButton;
    private       JTextField        searchField;
    private       JComboBox<String> categoryFilterBox;
    private       JButton           clearFiltersButton;
    private final CardLayout        contentLayout = new CardLayout();
    private final JPanel            contentPanel  = new JPanel(contentLayout);
    private       JLabel            stateTitleLabel;
    private       JLabel            stateBodyLabel;
    private       List<Expense>     currentExpenses = new ArrayList<>();
    private       boolean           syncingFilterOptions;

    /**
     * Builds the transactions page and subscribes it to shared expense updates.
     *
     * The table is wired once here, then repopulated inside update() whenever
     * the observer flow reports that shared data has changed.
     */
    public ExpenseTablePanel(ExpenseManager manager) {
        this.manager = manager;
        manager.addObserver(this);

        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        tableModel = buildTableModel();
        table      = buildTable();
        rowSorter  = buildRowSorter();
        table.setRowSorter(rowSorter);
        table.getSelectionModel().addListSelectionListener(e -> updateDeleteButtonState());

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        update();
    }

    // ----------------------------------------------------------------
    // Build helpers
    // ----------------------------------------------------------------

    /** Creates the non-editable table model used by the transactions table. */
    private DefaultTableModel buildTableModel() {
        String[] columns = {"ID", "Date", "Category", "Description", "Amount ($)"};
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == ID_COLUMN) return Integer.class;
                if (columnIndex == 4) return Double.class;
                return String.class;
            }
        };
    }

    /** Builds the styled JTable that displays expense history. */
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
        t.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Object display = value instanceof Number
                    ? String.format("$%.2f", ((Number) value).doubleValue())
                    : value;
                return rightAlign.getTableCellRendererComponent(table, display, isSelected, hasFocus, row, column);
            }
        });

        // Centre-align date column
        DefaultTableCellRenderer centreAlign = new DefaultTableCellRenderer();
        centreAlign.setHorizontalAlignment(SwingConstants.CENTER);
        t.getColumnModel().getColumn(1).setCellRenderer(centreAlign);

        return t;
    }

    /** Creates the sorter so numeric amount sorting behaves correctly. */
    private TableRowSorter<DefaultTableModel> buildRowSorter() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setComparator(4, Comparator.comparingDouble(value -> ((Number) value).doubleValue()));
        return sorter;
    }

    /** Builds the page title and filter controls shown above the table. */
    private JPanel buildTopBar() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel title = new JLabel("Transaction History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_CLR);

        countLabel = new JLabel("");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(MUTED);

        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(countLabel, BorderLayout.EAST);

        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setOpaque(false);
        filterRow.setBorder(new EmptyBorder(10, 0, 0, 0));

        searchField = UITheme.field();
        searchField.setToolTipText("Search by description, category, or date");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        categoryFilterBox = new JComboBox<>(new String[] {"All Categories"});
        UITheme.styleComboBox(categoryFilterBox);
        categoryFilterBox.addActionListener(e -> {
            if (!syncingFilterOptions) {
                applyFilters();
            }
        });

        clearFiltersButton = UITheme.button("Clear Filters", UITheme.SLATE);
        clearFiltersButton.setPreferredSize(new Dimension(140, 38));
        clearFiltersButton.addActionListener(e -> {
            searchField.setText("");
            categoryFilterBox.setSelectedIndex(0);
            applyFilters();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        filterRow.add(labeledControl("Search", searchField), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        filterRow.add(labeledControl("Category", categoryFilterBox), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets = new Insets(18, 0, 0, 0);
        filterRow.add(clearFiltersButton, gbc);

        wrapper.add(titleRow);
        wrapper.add(filterRow);
        return wrapper;
    }

    /** Builds the card that switches between the real table and state messages. */
    private JPanel buildTableCard() {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER_CLR, 1, true));
        scroll.getViewport().setBackground(CARD_BG);

        JPanel statePanel = buildStatePanel();

        contentPanel.setOpaque(false);
        contentPanel.add(scroll, "table");
        contentPanel.add(statePanel, "state");
        return contentPanel;
    }

    /** Builds the footer area that shows action feedback and delete controls. */
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(12, 0, 0, 0));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(MUTED);

        deleteButton = UITheme.button("Delete Selected", UITheme.DANGER);
        deleteButton.setPreferredSize(new Dimension(160, 36));
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteSelectedExpense());

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(deleteButton, BorderLayout.EAST);
        return bar;
    }

    /** Builds the empty/unavailable state shown when the table should be hidden. */
    private JPanel buildStatePanel() {
        JPanel statePanel = new JPanel(new GridBagLayout());
        statePanel.setBackground(CARD_BG);
        statePanel.setBorder(new LineBorder(BORDER_CLR, 1, true));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 24, 4, 24);

        stateTitleLabel = new JLabel("No transactions yet");
        stateTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        stateTitleLabel.setForeground(TEXT_CLR);
        gbc.gridy = 0;
        statePanel.add(stateTitleLabel, gbc);

        stateBodyLabel = new JLabel("Add your first expense to start tracking spending.");
        stateBodyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        stateBodyLabel.setForeground(MUTED);
        gbc.gridy = 1;
        statePanel.add(stateBodyLabel, gbc);

        return statePanel;
    }

    // ----------------------------------------------------------------
    // Event handling
    // ----------------------------------------------------------------

    /** Deletes the currently selected expense after user confirmation. */
    private void deleteSelectedExpense() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showStatus("Select a transaction first.", DANGER);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        String date = String.valueOf(tableModel.getValueAt(modelRow, 1));
        String category = String.valueOf(tableModel.getValueAt(modelRow, 2));
        String description = String.valueOf(tableModel.getValueAt(modelRow, 3));
        String amount = String.format("$%.2f", ((Number) tableModel.getValueAt(modelRow, 4)).doubleValue());

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "<html>Delete this transaction?<br><br>" +
                "<b>" + description + "</b><br>" +
                date + " • " + category + " • " + amount +
            "</html>",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            int expenseId = (int) tableModel.getValueAt(modelRow, ID_COLUMN);
            String error = manager.deleteExpense(expenseId);
            if (error == null) {
                table.clearSelection();
                showStatus("Transaction deleted.", SUCCESS);
            } else {
                showStatus("Could not delete transaction: " + DbErrorFormatter.format(error), DANGER);
            }
        }
    }

    private void updateDeleteButtonState() {
        if (deleteButton != null) {
            deleteButton.setEnabled(table.getSelectedRow() != -1 && tableModel.getRowCount() > 0);
        }
    }

    private void showState(String title, String body) {
        stateTitleLabel.setText(title);
        stateBodyLabel.setText(body);
        contentLayout.show(contentPanel, "state");
    }

    private void showTable() {
        contentLayout.show(contentPanel, "table");
    }

    private JPanel labeledControl(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(MUTED);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        return panel;
    }

    /** Rebuilds the category filter choices from the latest loaded expense list. */
    private void populateCategoryFilterOptions(List<Expense> expenses) {
        String selected = categoryFilterBox == null || categoryFilterBox.getSelectedItem() == null
            ? "All Categories"
            : categoryFilterBox.getSelectedItem().toString();

        syncingFilterOptions = true;
        categoryFilterBox.removeAllItems();
        categoryFilterBox.addItem("All Categories");
        expenses.stream()
            .map(Expense::getCategoryName)
            .distinct()
            .sorted()
            .forEach(categoryFilterBox::addItem);

        boolean restored = false;
        for (int i = 0; i < categoryFilterBox.getItemCount(); i++) {
            if (selected.equals(categoryFilterBox.getItemAt(i))) {
                categoryFilterBox.setSelectedIndex(i);
                restored = true;
                break;
            }
        }
        if (!restored) {
            categoryFilterBox.setSelectedIndex(0);
        }
        syncingFilterOptions = false;
    }

    /** Applies the current search text and category filter to the row sorter. */
    private void applyFilters() {
        if (rowSorter == null || searchField == null || categoryFilterBox == null) {
            return;
        }

        String search = searchField.getText().trim().toLowerCase(Locale.ENGLISH);
        String category = String.valueOf(categoryFilterBox.getSelectedItem());

        RowFilter<DefaultTableModel, Integer> filter = new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String rowCategory = String.valueOf(entry.getValue(2));
                String date = String.valueOf(entry.getValue(1));
                String description = String.valueOf(entry.getValue(3));
                String amount = String.format("$%.2f", ((Number) entry.getValue(4)).doubleValue());

                boolean matchesCategory = category == null
                    || "All Categories".equals(category)
                    || rowCategory.equals(category);

                boolean matchesSearch = search.isEmpty()
                    || rowCategory.toLowerCase(Locale.ENGLISH).contains(search)
                    || description.toLowerCase(Locale.ENGLISH).contains(search)
                    || date.toLowerCase(Locale.ENGLISH).contains(search)
                    || amount.toLowerCase(Locale.ENGLISH).contains(search);

                return matchesCategory && matchesSearch;
            }
        };

        rowSorter.setRowFilter(filter);
        refreshVisibleState();
    }

    /** Chooses whether to show the table, an empty state, or a filtered-no-match state. */
    private void refreshVisibleState() {
        int totalCount = currentExpenses.size();
        int visibleCount = rowSorter.getViewRowCount();

        if (totalCount == 0) {
            countLabel.setText("0 transactions");
            showState("No transactions yet", "Add your first expense to start tracking spending.");
        } else if (visibleCount == 0) {
            countLabel.setText("Showing 0 of " + totalCount + " transactions");
            showState("No matching transactions", "Try clearing filters or searching for a different keyword.");
        } else {
            countLabel.setText("Showing " + visibleCount + " of " + totalCount + " transactions");
            showTable();
        }

        updateDeleteButtonState();
    }

    private void showStatus(String message, Color color) {
        Timer timer = (Timer) statusLabel.getClientProperty("statusTimer");
        if (timer != null) {
            timer.stop();
        }

        statusLabel.setForeground(color);
        statusLabel.setText(message);

        Timer clearTimer = new Timer(5000, e -> {
            statusLabel.setText(" ");
            statusLabel.setForeground(MUTED);
        });
        clearTimer.setRepeats(false);
        statusLabel.putClientProperty("statusTimer", clearTimer);
        clearTimer.start();
    }

    // ----------------------------------------------------------------
    // Observer
    // ----------------------------------------------------------------

    @Override
    /** Reloads table data from ExpenseManager after an observer notification. */
    public void update() {
        tableModel.setRowCount(0);

        List<Expense> expenses = manager.getAllExpenses();
        currentExpenses = new ArrayList<>(expenses);
        String loadError = manager.getLastExpenseLoadError();
        for (Expense e : expenses) {
            tableModel.addRow(new Object[]{
                e.getId(),
                e.getDate().toString(),
                e.getCategoryName(),
                e.getDescription(),
                e.getAmount()
            });
        }

        table.clearSelection();
        populateCategoryFilterOptions(expenses);
        updateDeleteButtonState();

        if (loadError != null && !loadError.trim().isEmpty()) {
            countLabel.setText("Unavailable");
            showState("Could not load transactions", DbErrorFormatter.format(loadError));
        } else {
            applyFilters();
        }
    }
}
