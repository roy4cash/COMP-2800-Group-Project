package ui;

import db.InvestmentDAO;
import model.Investment;
import util.DateUtils;
import util.DbErrorFormatter;
import util.PlaceholderTextField;
import util.UITheme;
import util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
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
 *   2. Portfolio summary cards: totals, holdings count, and best/worst performer
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
    // Hard-coded single user (same as rest of app)
    private static final int DEFAULT_USER_ID = 1;

    private final InvestmentDAO dao = new InvestmentDAO();
    private String tableSetupError;

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
    private JLabel holdingsCountLabel;
    private JLabel bestPerformerLabel;
    private JLabel worstPerformerLabel;
    private JButton addButton;
    private JButton refreshButton;
    private JLabel tableStatusLabel;

    // ---- Table ----
    private DefaultTableModel tableModel;
    private JTable            table;
    private final CardLayout  tableContentLayout = new CardLayout();
    private final JPanel      tableContentPanel  = new JPanel(tableContentLayout);
    private JLabel            tableStateTitleLabel;
    private JLabel            tableStateBodyLabel;

    public InvestmentPanel() {
        // Create the DB table on first run — IF NOT EXISTS is idempotent
        tableSetupError = dao.createTableIfNotExists();

        setBackground(BG);
        setLayout(new BorderLayout());
        buildUI();
        wireFormStatusResetters();
        loadData(); // populate table and stats on first display
    }

    // ----------------------------------------------------------------
    // UI construction
    // ----------------------------------------------------------------

    private void buildUI() {
        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 24, 20));

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
        UITheme.styleScrollPane(scroll);
        add(scroll, BorderLayout.CENTER);
    }

    // ---- Header ----

    private JPanel buildHeaderRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(UITheme.pageTitle("Investment Tracker"));
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(UITheme.pageSubtitle("Manual portfolio tracking for stocks, ETFs, crypto, bonds, and other assets."));

        refreshButton = createButton("Refresh Portfolio", new Color(100, 116, 139));
        refreshButton.setPreferredSize(new Dimension(156, 36));
        refreshButton.addActionListener(e -> loadData());

        row.add(textPanel, BorderLayout.WEST);
        row.add(refreshButton, BorderLayout.EAST);
        return row;
    }

    // ---- Stats cards ----

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(2, 3, 16, 16));
        row.setBackground(BG);

        totalInvestedLabel = new JLabel("$0.00");
        currentValueLabel  = new JLabel("$0.00");
        gainLossLabel      = new JLabel("$0.00");
        holdingsCountLabel = new JLabel("0");
        bestPerformerLabel = new JLabel("None");
        worstPerformerLabel = new JLabel("None");

        row.add(buildStatCard("Total Invested",  totalInvestedLabel, "cost basis", new Color(37,  99,  235)));
        row.add(buildStatCard("Current Value",   currentValueLabel,  "market value", new Color(16,  185, 129)));
        row.add(buildStatCard("Total Gain/Loss", gainLossLabel,      "unrealized result", new Color(100, 116, 139)));
        row.add(buildStatCard("Holdings Count", holdingsCountLabel, "active positions", new Color(14, 165, 233)));
        row.add(buildStatCard("Best Performer", bestPerformerLabel, "highest gain by percentage", SUCCESS));
        row.add(buildStatCard("Worst Performer", worstPerformerLabel, "lowest gain by percentage", DANGER));

        return row;
    }

    private JPanel buildStatCard(String label, JLabel valueLabel, String subtext, Color accent) {
        valueLabel.setForeground(TEXT_CLR);
        return UITheme.statCard(label, valueLabel, subtext, accent);
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

        JLabel helper = new JLabel(
            "<html><div style='color:#64748B; line-height:1.45'>Enter holdings manually. " +
            "Current prices are user-entered values and are not fetched from a live market feed.</div></html>");
        helper.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        helper.setForeground(LABEL_CLR);
        GridBagConstraints hc = (GridBagConstraints) gbc.clone();
        hc.gridx = 0; hc.gridy = 0; hc.gridwidth = 3;
        hc.insets = new Insets(0, 4, 10, 4);
        inner.add(helper, hc);

        // Row 1: Name, Ticker, Type
        nameField   = new PlaceholderTextField("e.g. Apple Inc.");
        tickerField = new PlaceholderTextField("AAPL");
        typeBox = new JComboBox<>(new String[]{"Stock", "ETF", "Crypto", "Bond", "Other"});
        styleField(nameField);
        styleField(tickerField);
        UITheme.styleComboBox(typeBox);
        typeBox.setPreferredSize(new Dimension(0, 38));

        addFormLabel(inner, gbc, "Name",   0, 1);
        addFormLabel(inner, gbc, "Ticker", 1, 1);
        addFormLabel(inner, gbc, "Type",   2, 1);
        addFormField(inner, gbc, nameField,   0, 2);
        addFormField(inner, gbc, tickerField, 1, 2);
        addFormField(inner, gbc, typeBox,     2, 2);

        // Row 2: Shares, Buy Price, Current Price, Date
        sharesField       = new PlaceholderTextField("e.g. 10");
        buyPriceField     = new PlaceholderTextField("e.g. 150.00");
        currentPriceField = new PlaceholderTextField("e.g. 175.00");
        dateField         = new JTextField(DateUtils.todayAsInputString());
        styleField(sharesField);
        styleField(buyPriceField);
        styleField(currentPriceField);
        styleField(dateField);

        addFormLabel(inner, gbc, "Shares",        0, 3);
        addFormLabel(inner, gbc, "Buy Price ($)",  1, 3);
        addFormLabel(inner, gbc, "Current Price ($)", 2, 3);
        addFormField(inner, gbc, sharesField,       0, 4);
        addFormField(inner, gbc, buyPriceField,     1, 4);
        addFormField(inner, gbc, currentPriceField, 2, 4);

        // Date label spans col 0 of row 4
        JLabel dateLabel = new JLabel("Purchase Date (YYYY-MM-DD)");
        dateLabel.setFont(LABEL_FONT);
        dateLabel.setForeground(LABEL_CLR);
        GridBagConstraints dlc = (GridBagConstraints) gbc.clone();
        dlc.gridx = 0; dlc.gridy = 5; dlc.gridwidth = 1;
        inner.add(dateLabel, dlc);

        GridBagConstraints dfc = (GridBagConstraints) gbc.clone();
        dfc.gridx = 0; dfc.gridy = 6; dfc.gridwidth = 1;
        inner.add(dateField, dfc);

        JLabel dateHint = new JLabel("Use the purchase date only. Notes are not required in this build.");
        dateHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateHint.setForeground(LABEL_CLR);
        GridBagConstraints dhc = (GridBagConstraints) gbc.clone();
        dhc.gridx = 1; dhc.gridy = 6; dhc.gridwidth = 2;
        inner.add(dateHint, dhc);

        // Status label
        formStatus = new JLabel(" ");
        formStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formStatus.setForeground(LABEL_CLR);
        GridBagConstraints sc = (GridBagConstraints) gbc.clone();
        sc.gridx = 0; sc.gridy = 7; sc.gridwidth = 3;
        sc.insets = new Insets(8, 4, 4, 4);
        inner.add(formStatus, sc);

        // Add Investment button (spans full width)
        addButton = createButton("+ Add Investment", PRIMARY);
        addButton.addActionListener(e -> handleAddInvestment());
        GridBagConstraints bc = (GridBagConstraints) gbc.clone();
        bc.gridx = 0; bc.gridy = 8; bc.gridwidth = 3;
        bc.insets = new Insets(10, 4, 4, 4);
        inner.add(addButton, bc);

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
        UITheme.styleScrollPane(tableScroll);

        tableStatusLabel = new JLabel(" ");
        tableStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableStatusLabel.setForeground(LABEL_CLR);
        tableStatusLabel.setBorder(new EmptyBorder(8, 16, 8, 16));

        tableContentPanel.setOpaque(false);
        tableContentPanel.add(tableScroll, "table");
        tableContentPanel.add(buildTableStatePanel(), "state");

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titleLabel, BorderLayout.NORTH);
        header.add(tableStatusLabel, BorderLayout.SOUTH);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new LineBorder(BORDER_CLR, 1, true));
        card.add(header, BorderLayout.NORTH);
        card.add(tableContentPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableStatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(new EmptyBorder(32, 24, 32, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 0, 4, 0);

        tableStateTitleLabel = new JLabel("No investments yet");
        tableStateTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableStateTitleLabel.setForeground(TEXT_CLR);
        gbc.gridy = 0;
        panel.add(tableStateTitleLabel, gbc);

        tableStateBodyLabel = new JLabel("Add your first holding to start tracking your portfolio.");
        tableStateBodyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableStateBodyLabel.setForeground(LABEL_CLR);
        gbc.gridy = 1;
        panel.add(tableStateBodyLabel, gbc);

        return panel;
    }

    // ----------------------------------------------------------------
    // Data loading
    // ----------------------------------------------------------------

    /**
     * Reloads all investments from the database, rebuilds the table rows,
     * and recalculates the portfolio summary cards.
     */
    private void loadData() {
        if (tableSetupError != null) {
            tableSetupError = dao.createTableIfNotExists();
        }
        if (refreshButton != null) {
            refreshButton.setEnabled(false);
        }
        if (tableStatusLabel != null) {
            tableStatusLabel.setForeground(LABEL_CLR);
            tableStatusLabel.setText("Refreshing portfolio...");
        }
        List<Investment> investments = dao.getAllInvestments(DEFAULT_USER_ID);
        String loadError = tableSetupError != null ? tableSetupError : dao.getLastLoadErrorMessage();

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
        holdingsCountLabel.setText(String.valueOf(investments.size()));

        Investment bestPerformer = getBestPerformer(investments);
        Investment worstPerformer = getWorstPerformer(investments);
        updatePerformerLabel(bestPerformerLabel, bestPerformer, SUCCESS);
        updatePerformerLabel(worstPerformerLabel, worstPerformer, DANGER);

        if (loadError != null && !loadError.trim().isEmpty()) {
            showTableState("Could not load investments", "Check your database settings and refresh again.");
            showTableStatus("Could not refresh investments: " + DbErrorFormatter.format(loadError), DANGER);
        } else if (investments.isEmpty()) {
            showTableState("No investments yet", "Add your first holding to start tracking your portfolio.");
            tableStatusLabel.setForeground(LABEL_CLR);
            tableStatusLabel.setText(" ");
            bestPerformerLabel.setText("None");
            bestPerformerLabel.setForeground(LABEL_CLR);
            worstPerformerLabel.setText("None");
            worstPerformerLabel.setForeground(LABEL_CLR);
        } else {
            showInvestmentTable();
            tableStatusLabel.setForeground(LABEL_CLR);
            tableStatusLabel.setText(investments.size() + (investments.size() == 1 ? " holding loaded." : " holdings loaded."));
        }

        if (refreshButton != null) {
            refreshButton.setEnabled(true);
        }
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

        String error = ValidationUtils.validateInvestmentInput(name, ticker, sharesText, buyText, currText, date);
        if (error != null) {
            showFormStatus(error, DANGER);
            return;
        }

        double shares       = Double.parseDouble(sharesText);
        double buyPrice     = Double.parseDouble(buyText);
        double currentPrice = currText.isEmpty() ? buyPrice : Double.parseDouble(currText);

        addButton.setEnabled(false);
        try {
            String dbError = dao.addInvestment(DEFAULT_USER_ID, name, ticker, type,
                               shares, buyPrice, currentPrice, date, "");
            if (dbError == null) {
                clearForm();
                loadData();
                showFormStatus("Investment added.", SUCCESS);
                showTableStatus("Portfolio refreshed after add.", SUCCESS);
            } else {
                showFormStatus("Could not save investment: " + DbErrorFormatter.format(dbError), DANGER);
            }
        } finally {
            addButton.setEnabled(true);
        }
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
        showTimedStatus(formStatus, msg, color);
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

    private void wireFormStatusResetters() {
        attachStatusClearer(nameField, formStatus);
        attachStatusClearer(tickerField, formStatus);
        attachStatusClearer(sharesField, formStatus);
        attachStatusClearer(buyPriceField, formStatus);
        attachStatusClearer(currentPriceField, formStatus);
        attachStatusClearer(dateField, formStatus);
        typeBox.addActionListener(e -> clearStatus(formStatus));
    }

    private void attachStatusClearer(JTextComponent field, JLabel label) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { clearStatus(label); }
            @Override public void removeUpdate(DocumentEvent e) { clearStatus(label); }
            @Override public void changedUpdate(DocumentEvent e) { clearStatus(label); }
        });
    }

    private void clearStatus(JLabel label) {
        Timer timer = (Timer) label.getClientProperty("statusTimer");
        if (timer != null) {
            timer.stop();
        }
        label.setText(" ");
        label.setForeground(LABEL_CLR);
    }

    private void showTimedStatus(JLabel label, String message, Color color) {
        clearStatus(label);
        label.setForeground(color);
        label.setText(message);

        Timer timer = new Timer(5000, e -> {
            label.setText(" ");
            label.setForeground(LABEL_CLR);
        });
        timer.setRepeats(false);
        label.putClientProperty("statusTimer", timer);
        timer.start();
    }

    private void showTableStatus(String message, Color color) {
        showTimedStatus(tableStatusLabel, message, color);
    }

    private void showTableState(String title, String body) {
        tableStateTitleLabel.setText(title);
        tableStateBodyLabel.setText(body);
        tableContentLayout.show(tableContentPanel, "state");
    }

    private void showInvestmentTable() {
        tableContentLayout.show(tableContentPanel, "table");
    }

    private Investment getBestPerformer(List<Investment> investments) {
        Investment best = null;
        for (Investment investment : investments) {
            if (best == null || investment.getGainLossPct() > best.getGainLossPct()) {
                best = investment;
            }
        }
        return best;
    }

    private Investment getWorstPerformer(List<Investment> investments) {
        Investment worst = null;
        for (Investment investment : investments) {
            if (worst == null || investment.getGainLossPct() < worst.getGainLossPct()) {
                worst = investment;
            }
        }
        return worst;
    }

    private void updatePerformerLabel(JLabel label, Investment investment, Color accent) {
        if (investment == null) {
            label.setText("None");
            label.setForeground(LABEL_CLR);
            label.setToolTipText(null);
            return;
        }
        label.setText(investment.getTicker().isEmpty() ? investment.getName() : investment.getTicker());
        label.setForeground(accent);
        label.setToolTipText(String.format(
            "%s | %.2f%% | $%.2f gain/loss",
            investment.getName(),
            investment.getGainLossPct(),
            investment.getGainLoss()
        ));
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
            setBackground(selected ? new Color(219, 234, 254) : (row % 2 == 0 ? CARD_BG : UITheme.ROW_ALT));
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
                    Investment investment = currentInvestments.get(clickedRow);
                    int id = investment.getId();
                    int confirm = JOptionPane.showConfirmDialog(
                        InvestmentPanel.this,
                        "<html>Delete this investment?<br><br>" +
                            "<b>" + investment.getName() + "</b><br>" +
                            investment.getTicker() + " • " + investment.getType() + " • " +
                            String.format("%.4f shares", investment.getShares()) +
                        "</html>",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        String error = dao.deleteInvestment(id);
                        if (error == null) {
                            loadData();
                            showTableStatus("Investment deleted.", SUCCESS);
                        } else {
                            showTableStatus("Could not delete investment: " + DbErrorFormatter.format(error), DANGER);
                        }
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
