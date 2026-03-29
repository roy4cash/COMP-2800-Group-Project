package ui;

import db.InvestmentDAO;
import model.Investment;
import util.DateUtils;
import util.PlaceholderTextField;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * InvestmentPanel — a standalone panel for tracking investment holdings.
 *
 * Does NOT implement Observer because investment data is independent of expenses.
 * A manual "Refresh" button reloads data from the database on demand.
 *
 * Layout (top to bottom):
 *   1. Page title + Refresh button
 *   2. Three portfolio stat cards: Total Invested, Current Value, Total Gain/Loss
 *   3. "Add Investment" form (Name, Ticker, Type, Shares, Buy Price, Current Price, Date)
 *   4. Scrollable table of all investments with a Delete action column
 *
 * The investments table is created automatically on first run via
 * InvestmentDAO.createTableIfNotExists().
 */
public class InvestmentPanel extends JPanel {

    // ---- Colour scheme ----
    private static final Color PRIMARY    = new Color(37,  99,  235);
    private static final Color SUCCESS    = new Color(16,  185, 129);
    private static final Color DANGER     = new Color(239, 68,  68);
    private static final Color BG         = new Color(248, 250, 252);
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color BORDER_CLR = new Color(226, 232, 240);
    private static final Color LABEL_CLR  = new Color(100, 116, 139);
    private static final Color TEXT_CLR   = new Color(30,  41,  59);

    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BTN_FONT   = new Font("Segoe UI", Font.BOLD,  13);

    // Hard-coded single user (same as rest of app)
    private static final int DEFAULT_USER_ID = 1;

    private final InvestmentDAO dao = new InvestmentDAO();

    // ---- Form fields ----
    private PlaceholderTextField nameField;
    private PlaceholderTextField tickerField;
    private JComboBox<String>    typeBox;
    private PlaceholderTextField sharesField;
    private PlaceholderTextField buyPriceField;
    private PlaceholderTextField currentPriceField;
    private JTextField           dateField;
    private JLabel               formStatus;

    // ---- Portfolio stat labels (updated on each refresh) ----
    private JLabel totalInvestedLabel;
    private JLabel currentValueLabel;
    private JLabel gainLossLabel;

    // ---- Table ----
    private DefaultTableModel tableModel;
    private JTable            table;

    public InvestmentPanel() {
        // Create the DB table on first run — IF NOT EXISTS is idempotent
        dao.createTableIfNotExists();

        setBackground(BG);
        setLayout(new BorderLayout());
        buildUI();
        loadData(); // populate table and stats on first display
    }

    // ----------------------------------------------------------------
    // UI construction
    // ----------------------------------------------------------------

    private void buildUI() {
        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Header row: title + refresh button
        content.add(buildHeaderRow());
        content.add(Box.createVerticalStrut(16));

        // 2. Portfolio summary cards
        JPanel statsRow = buildStatsRow();
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        content.add(statsRow);
        content.add(Box.createVerticalStrut(16));

        // 3. Add investment form
        JPanel formCard = buildFormCard();
        formCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        content.add(formCard);
        content.add(Box.createVerticalStrut(16));

        // 4. Table of investments (fills remaining space)
        content.add(buildTablePanel());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ---- Header ----

    private JPanel buildHeaderRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel title = new JLabel("Investment Tracker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_CLR);

        JButton refresh = createButton("Refresh", new Color(100, 116, 139));
        refresh.setPreferredSize(new Dimension(100, 34));
        refresh.addActionListener(e -> loadData());

        row.add(title,   BorderLayout.WEST);
        row.add(refresh, BorderLayout.EAST);
        return row;
    }

    // ---- Stats cards ----

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setBackground(BG);

        totalInvestedLabel = new JLabel("$0.00");
        currentValueLabel  = new JLabel("$0.00");
        gainLossLabel      = new JLabel("$0.00");

        row.add(buildStatCard("Total Invested",  totalInvestedLabel, new Color(37,  99,  235)));
        row.add(buildStatCard("Current Value",   currentValueLabel,  new Color(16,  185, 129)));
        row.add(buildStatCard("Total Gain/Loss", gainLossLabel,      new Color(100, 116, 139)));

        return row;
    }

    private JPanel buildStatCard(String label, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
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
        gbc.gridy = 0; card.add(lbl, gbc);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(TEXT_CLR);
        gbc.gridy = 1; gbc.insets = new Insets(4, 0, 0, 0);
        card.add(valueLabel, gbc);

        return card;
    }

    // ---- Add Investment form ----

    private JPanel buildFormCard() {
        // Card with title bar
        JLabel titleLabel = new JLabel("  Add Investment");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_CLR);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(239, 246, 255));
        titleLabel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(CARD_BG);
        inner.setBorder(new EmptyBorder(12, 16, 16, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets  = new Insets(4, 4, 4, 4);

        // Row 1: Name, Ticker, Type
        nameField   = new PlaceholderTextField("e.g. Apple Inc.");
        tickerField = new PlaceholderTextField("AAPL");
        typeBox = new JComboBox<>(new String[]{"Stock", "ETF", "Crypto", "Bond", "Other"});
        styleField(nameField);
        styleField(tickerField);
        typeBox.setFont(FIELD_FONT);
        typeBox.setBackground(new Color(248, 250, 252));
        typeBox.setPreferredSize(new Dimension(0, 38));

        addFormLabel(inner, gbc, "Name",   0, 0);
        addFormLabel(inner, gbc, "Ticker", 1, 0);
        addFormLabel(inner, gbc, "Type",   2, 0);
        addFormField(inner, gbc, nameField,   0, 1);
        addFormField(inner, gbc, tickerField, 1, 1);
        addFormField(inner, gbc, typeBox,     2, 1);

        // Row 2: Shares, Buy Price, Current Price, Date
        sharesField       = new PlaceholderTextField("e.g. 10");
        buyPriceField     = new PlaceholderTextField("e.g. 150.00");
        currentPriceField = new PlaceholderTextField("e.g. 175.00");
        dateField         = new JTextField(DateUtils.todayAsInputString());
        styleField(sharesField);
        styleField(buyPriceField);
        styleField(currentPriceField);
        styleField(dateField);

        addFormLabel(inner, gbc, "Shares",        0, 2);
        addFormLabel(inner, gbc, "Buy Price ($)",  1, 2);
        addFormLabel(inner, gbc, "Current Price ($)", 2, 2);
        addFormField(inner, gbc, sharesField,       0, 3);
        addFormField(inner, gbc, buyPriceField,     1, 3);
        addFormField(inner, gbc, currentPriceField, 2, 3);

        // Date label spans col 0 of row 4
        JLabel dateLabel = new JLabel("Purchase Date (YYYY-MM-DD)");
        dateLabel.setFont(LABEL_FONT);
        dateLabel.setForeground(LABEL_CLR);
        GridBagConstraints dlc = (GridBagConstraints) gbc.clone();
        dlc.gridx = 0; dlc.gridy = 4; dlc.gridwidth = 1;
        inner.add(dateLabel, dlc);

        GridBagConstraints dfc = (GridBagConstraints) gbc.clone();
        dfc.gridx = 0; dfc.gridy = 5; dfc.gridwidth = 1;
        inner.add(dateField, dfc);

        // Status label
        formStatus = new JLabel(" ");
        formStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        GridBagConstraints sc = (GridBagConstraints) gbc.clone();
        sc.gridx = 0; sc.gridy = 6; sc.gridwidth = 3;
        inner.add(formStatus, sc);

        // Add Investment button (spans full width)
        JButton addBtn = createButton("+ Add Investment", PRIMARY);
        addBtn.addActionListener(e -> handleAddInvestment());
        GridBagConstraints bc = (GridBagConstraints) gbc.clone();
        bc.gridx = 0; bc.gridy = 7; bc.gridwidth = 3;
        bc.insets = new Insets(8, 4, 4, 4);
        inner.add(addBtn, bc);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new LineBorder(BORDER_CLR, 1, true));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(inner,      BorderLayout.CENTER);
        return card;
    }

    private void addFormLabel(JPanel p, GridBagConstraints gbc, String text, int col, int row) {
        JLabel l = new JLabel(text);
        l.setFont(LABEL_FONT);
        l.setForeground(LABEL_CLR);
        GridBagConstraints c = (GridBagConstraints) gbc.clone();
        c.gridx = col; c.gridy = row; c.gridwidth = 1;
        p.add(l, c);
    }

    private void addFormField(JPanel p, GridBagConstraints gbc, JComponent field, int col, int row) {
        GridBagConstraints c = (GridBagConstraints) gbc.clone();
        c.gridx = col; c.gridy = row; c.gridwidth = 1;
        p.add(field, c);
    }

    // ---- Investments table ----

    private JPanel buildTablePanel() {
        // Card wrapper
        JLabel titleLabel = new JLabel("  My Investments");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_CLR);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(239, 246, 255));
        titleLabel.setBorder(new EmptyBorder(12, 16, 12, 16));

        String[] columns = {
            "Name", "Ticker", "Type", "Shares",
            "Buy Price", "Current Price",
            "Total Cost", "Current Value",
            "Gain/Loss ($)", "Gain/Loss (%)", "Delete"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                // Only the Delete column is "editable" (used for button renderer/editor)
                return col == 10;
            }
        };

        table = new JTable(tableModel);
        // Apply consistent table styling from UITheme (dark header, alternating rows)
        UITheme.styleTable(table);

        // Custom renderer: color gain/loss columns and render Delete button
        table.setDefaultRenderer(Object.class, new GainLossRenderer());
        table.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        table.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox()));

        // Column widths
        int[] widths = {130, 70, 70, 60, 80, 90, 90, 90, 90, 90, 70};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(0, 220));
        tableScroll.setBorder(null);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new LineBorder(BORDER_CLR, 1, true));
        card.add(titleLabel,  BorderLayout.NORTH);
        card.add(tableScroll, BorderLayout.CENTER);
        return card;
    }

    // ----------------------------------------------------------------
    // Data loading
    // ----------------------------------------------------------------

    /**
     * Reloads all investments from the database, rebuilds the table rows,
     * and recalculates the portfolio summary cards.
     */
    private void loadData() {
        List<Investment> investments = dao.getAllInvestments(DEFAULT_USER_ID);

        // Rebuild table
        tableModel.setRowCount(0);
        double totalCost  = 0;
        double totalValue = 0;

        for (Investment inv : investments) {
            tableModel.addRow(new Object[]{
                inv.getName(),
                inv.getTicker(),
                inv.getType(),
                String.format("%.4f", inv.getShares()),
                String.format("$%.2f", inv.getBuyPrice()),
                String.format("$%.2f", inv.getCurrentPrice()),
                String.format("$%.2f", inv.getTotalCost()),
                String.format("$%.2f", inv.getCurrentValue()),
                String.format("$%.2f", inv.getGainLoss()),
                String.format("%.2f%%", inv.getGainLossPct()),
                "Delete"   // text shown in button cell — id encoded via row model
            });
            totalCost  += inv.getTotalCost();
            totalValue += inv.getCurrentValue();
        }

        // Store investment IDs for use by the delete editor
        this.currentInvestments = investments;

        // Update portfolio summary labels
        double gainLoss = totalValue - totalCost;
        totalInvestedLabel.setText(String.format("$%.2f", totalCost));
        currentValueLabel.setText(String.format("$%.2f", totalValue));

        gainLossLabel.setText(String.format("$%.2f", gainLoss));
        gainLossLabel.setForeground(gainLoss >= 0 ? SUCCESS : DANGER);
    }

    // ----------------------------------------------------------------
    // Event handlers
    // ----------------------------------------------------------------

    /** Validates and submits the Add Investment form. */
    private void handleAddInvestment() {
        String name         = nameField.getText().trim();
        String ticker       = tickerField.getText().trim();
        String type         = (String) typeBox.getSelectedItem();
        String sharesText   = sharesField.getText().trim();
        String buyText      = buyPriceField.getText().trim();
        String currText     = currentPriceField.getText().trim();
        String date         = dateField.getText().trim();

        // Basic validation
        if (name.isEmpty()) {
            showFormStatus("Investment name is required.", DANGER); return;
        }
        if (!isPositiveDouble(sharesText)) {
            showFormStatus("Shares must be a positive number.", DANGER); return;
        }
        if (!isPositiveDouble(buyText)) {
            showFormStatus("Buy price must be a positive number.", DANGER); return;
        }

        double shares       = Double.parseDouble(sharesText);
        double buyPrice     = Double.parseDouble(buyText);
        double currentPrice = currText.isEmpty() ? buyPrice : parseDoubleOrZero(currText);

        dao.addInvestment(DEFAULT_USER_ID, name, ticker, type,
                          shares, buyPrice, currentPrice, date, "");
        clearForm();
        loadData();
        showFormStatus("Investment added!", SUCCESS);
    }

    private void clearForm() {
        nameField.setText("");
        tickerField.setText("");
        sharesField.setText("");
        buyPriceField.setText("");
        currentPriceField.setText("");
        dateField.setText(DateUtils.todayAsInputString());
        typeBox.setSelectedIndex(0);
    }

    private void showFormStatus(String msg, Color color) {
        formStatus.setForeground(color);
        formStatus.setText(msg);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private void styleField(JTextField field) {
        field.setFont(FIELD_FONT);
        field.setForeground(TEXT_CLR);
        field.setBackground(new Color(248, 250, 252));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_CLR, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        field.setPreferredSize(new Dimension(0, 38));
    }

    /** Delegates to UITheme so all buttons share the same hover behaviour. */
    private JButton createButton(String text, Color color) {
        return UITheme.button(text, color);
    }

    private boolean isPositiveDouble(String s) {
        if (s == null || s.isEmpty()) return false;
        try { return Double.parseDouble(s) > 0; }
        catch (NumberFormatException e) { return false; }
    }

    private double parseDoubleOrZero(String s) {
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return 0; }
    }

    // ----------------------------------------------------------------
    // Investments list (kept in sync with table for delete operations)
    // ----------------------------------------------------------------

    /** Mirrors the DB list so the Delete button can find the correct ID by row index. */
    private List<Investment> currentInvestments = new java.util.ArrayList<>();

    // ----------------------------------------------------------------
    // Custom table renderers and editors
    // ----------------------------------------------------------------

    /**
     * Renders gain/loss columns with green (positive) or red (negative) text,
     * and renders the Delete column as a red button.
     */
    private class GainLossRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean selected, boolean focused, int row, int col) {
            super.getTableCellRendererComponent(tbl, value, selected, focused, row, col);
            setBackground(selected ? new Color(219, 234, 254) : CARD_BG);
            setForeground(TEXT_CLR);

            // Color gain/loss columns
            if (col == 8 || col == 9) {
                String text = value == null ? "" : value.toString();
                boolean isNegative = text.contains("-");
                setForeground(isNegative ? DANGER : SUCCESS);
            }
            return this;
        }
    }

    /** Renders the Delete column as a styled red JButton. */
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setForeground(Color.WHITE);
            setBackground(DANGER);
            setBorderPainted(false);
            setFocusPainted(false);
            setText("Delete");
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    /**
     * Makes the Delete column cells clickable.
     * On click, deletes the investment at that row from the DB and reloads.
     */
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int     clickedRow;

        ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Delete");
            button.setFont(new Font("Segoe UI", Font.BOLD, 11));
            button.setForeground(Color.WHITE);
            button.setBackground(DANGER);
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.addActionListener(e -> {
                fireEditingStopped();
                if (clickedRow >= 0 && clickedRow < currentInvestments.size()) {
                    int id = currentInvestments.get(clickedRow).getId();
                    int confirm = JOptionPane.showConfirmDialog(
                        InvestmentPanel.this,
                        "Delete this investment?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        dao.deleteInvestment(id);
                        loadData();
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object value,
                boolean isSelected, int row, int column) {
            clickedRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() { return "Delete"; }
    }
}
