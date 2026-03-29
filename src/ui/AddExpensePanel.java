package ui;

import model.Category;
import observer.ExpenseManager;
import util.DateUtils;
import util.PlaceholderTextField;
import util.UITheme;
import util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Form panel for adding a new expense and setting the monthly budget.
 *
 * Layout: two side-by-side cards on a light-gray background.
 *   Left  — "Add New Expense":  category dropdown, description, amount, date, Add button.
 *   Right — "Monthly Budget":   info text, budget amount field, Set Budget button.
 *
 * Uses PlaceholderTextField so description and amount show hint text instead of
 * pre-filled values, preventing accidental submission of placeholder data.
 */
public class AddExpensePanel extends JPanel {

    // ---- Colour scheme shared across all panels ----
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

    // ---- Business logic ----
    private final ExpenseManager manager;

    // ---- Form widgets ----
    private JComboBox<Category>   categoryBox;
    private PlaceholderTextField  descriptionField;
    private PlaceholderTextField  amountField;
    private JTextField            dateField;
    private JTextField            budgetField;
    private JLabel                statusLabel;

    public AddExpensePanel(ExpenseManager manager) {
        this.manager = manager;
        setBackground(BG);
        setLayout(new GridBagLayout());
        buildUI();
    }

    // ----------------------------------------------------------------
    // UI construction
    // ----------------------------------------------------------------

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(20, 20, 20, 20);
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left card — wider (55 %)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.55;
        add(buildExpenseCard(), gbc);

        // Right card — narrower (45 %)
        gbc.gridx = 1; gbc.weightx = 0.45;
        add(buildBudgetCard(), gbc);
    }

    // ---- Left card: Add New Expense ----

    private JPanel buildExpenseCard() {
        JPanel card  = createCard("Add New Expense");
        JPanel inner = getInnerPanel(card);

        GridBagConstraints gbc = fieldGbc();
        int row = 0;

        // Category dropdown
        categoryBox = new JComboBox<>();
        styleCombo(categoryBox);
        loadCategories();
        row = addRow(inner, "Category", categoryBox, row, gbc);

        // Description — PlaceholderTextField (no pre-filled value)
        descriptionField = new PlaceholderTextField("e.g. Morning coffee");
        styleField(descriptionField);
        row = addRow(inner, "Description", descriptionField, row, gbc);

        // Amount — PlaceholderTextField (no pre-filled value)
        amountField = new PlaceholderTextField("0.00");
        styleField(amountField);
        row = addRow(inner, "Amount ($)", amountField, row, gbc);

        // Date — pre-filled with today's date (this is intentional)
        dateField = new JTextField(DateUtils.todayAsInputString());
        styleField(dateField);
        row = addRow(inner, "Date (YYYY-MM-DD)", dateField, row, gbc);

        // Status label — shows success or error messages
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.weighty = 0; gbc.insets = new Insets(4, 16, 4, 16);
        inner.add(statusLabel, gbc);
        gbc.gridwidth = 1;

        // Spacer pushes button to bottom
        JPanel spacer = new JPanel(); spacer.setOpaque(false);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        inner.add(spacer, gbc);

        // Add Expense button — primary blue
        JButton addBtn = createButton("+ Add Expense", PRIMARY);
        addBtn.addActionListener(e -> handleAddExpense());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 16, 16, 16);
        inner.add(addBtn, gbc);

        return card;
    }

    // ---- Right card: Monthly Budget ----

    private JPanel buildBudgetCard() {
        JPanel card  = createCard("Monthly Budget");
        JPanel inner = getInnerPanel(card);

        GridBagConstraints gbc = fieldGbc();
        int row = 0;

        // Informational text
        JLabel info = new JLabel(
            "<html><div style='width:210px; color:#64748B; line-height:1.5'>" +
            "Set your spending limit for this month. " +
            "You will receive a warning when you reach 80% and 100% of your budget." +
            "</div></html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.weighty = 0;
        gbc.insets = new Insets(16, 16, 12, 16);
        inner.add(info, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 16, 6, 16);

        // Budget amount field — show current budget if one is set, else empty
        double current = manager.getCurrentBudget().getAmount();
        budgetField = new JTextField(current > 0 ? String.valueOf((int) current) : "");
        budgetField.setFont(FIELD_FONT);
        budgetField.setForeground(TEXT_CLR);
        budgetField.setBackground(new Color(248, 250, 252));
        budgetField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_CLR, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        budgetField.setPreferredSize(new Dimension(0, 38));
        row = addRow(inner, "Budget Amount ($)", budgetField, row, gbc);

        // Spacer
        JPanel spacer = new JPanel(); spacer.setOpaque(false);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        inner.add(spacer, gbc);

        // Set Budget button — green
        JButton setBtn = createButton("Set Budget", SUCCESS);
        setBtn.addActionListener(e -> handleSetBudget());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 16, 16, 16);
        inner.add(setBtn, gbc);

        return card;
    }

    // ----------------------------------------------------------------
    // Widget helpers
    // ----------------------------------------------------------------

    /**
     * Creates a white card with a light-blue title bar at the top.
     * Returns the card panel; use getInnerPanel(card) to get the
     * inner GridBagLayout panel where rows should be added.
     */
    private JPanel createCard(String title) {
        // Title bar
        JLabel titleLabel = new JLabel("  " + title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_CLR);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(239, 246, 255));
        titleLabel.setBorder(new EmptyBorder(12, 16, 12, 16));
        titleLabel.setPreferredSize(new Dimension(0, 46));

        // Inner panel where form rows go
        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(CARD_BG);

        // Outer card
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_CLR, 1, true),
            new EmptyBorder(0, 0, 0, 0)));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(inner,      BorderLayout.CENTER);

        return card;
    }

    /**
     * Returns the inner GridBagLayout panel inside a card created by createCard().
     * The inner panel is the first JPanel child that is NOT the card itself.
     */
    private JPanel getInnerPanel(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JPanel) return (JPanel) c;
        }
        return card; // fallback — should not happen
    }

    /**
     * Adds a label row followed by a component row inside the given panel.
     * Returns the next available row index.
     */
    private int addRow(JPanel panel, String labelText, JComponent field,
                       int row, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(LABEL_CLR);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(row == 0 ? 16 : 10, 16, 2, 16);
        panel.add(label, gbc);

        gbc.gridy = row + 1;
        gbc.insets = new Insets(0, 16, 6, 16);
        panel.add(field, gbc);

        return row + 2;
    }

    /** Applies consistent styling to a text field. */
    private void styleField(JTextField field) {
        field.setFont(FIELD_FONT);
        field.setForeground(TEXT_CLR);
        field.setBackground(new Color(248, 250, 252));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_CLR, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        field.setPreferredSize(new Dimension(0, 38));
    }

    private void styleCombo(JComboBox<Category> combo) {
        combo.setFont(FIELD_FONT);
        combo.setBackground(new Color(248, 250, 252));
        combo.setPreferredSize(new Dimension(0, 38));
    }

    /** Creates a full-width button with hover effect via UITheme. */
    private JButton createButton(String text, Color color) {
        JButton btn = UITheme.button(text, color);
        btn.setPreferredSize(new Dimension(0, 42)); // slightly taller than default
        return btn;
    }

    /** Default GridBagConstraints for form rows. */
    private GridBagConstraints fieldGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets  = new Insets(6, 16, 6, 16);
        return gbc;
    }

    /** Populates the category dropdown from the manager (which falls back to defaults). */
    private void loadCategories() {
        List<Category> categories = manager.getAllCategories();
        categoryBox.removeAllItems();
        for (Category c : categories) {
            categoryBox.addItem(c);
        }
        if (categories.isEmpty()) {
            System.err.println("AddExpensePanel: still no categories after fallback — check DB.");
        }
    }

    // ----------------------------------------------------------------
    // Event handlers
    // ----------------------------------------------------------------

    private void handleAddExpense() {
        // Read raw text from fields; placeholder fields return "" when empty
        String description = descriptionField.getText().trim();
        String amountText  = amountField.getText().trim();
        String dateText    = dateField.getText().trim();

        // Validate inputs using existing utility
        String error = ValidationUtils.validateExpenseInput(description, amountText, dateText);
        if (error != null) { showStatus(error, DANGER); return; }

        Category category = (Category) categoryBox.getSelectedItem();
        if (category == null) { showStatus("Please select a category.", DANGER); return; }

        double    amount = Double.parseDouble(amountText);
        LocalDate date   = LocalDate.parse(dateText);

        String error = manager.addExpense(category.getId(), description, amount, date);
        if (error == null) {
            clearExpenseFields();
            showStatus("Expense added successfully!", SUCCESS);
        } else {
            // Show the actual DB error so we know exactly what went wrong
            showStatus("DB Error: " + error, DANGER);
        }
    }

    private void handleSetBudget() {
        String budgetText = budgetField.getText().trim();
        if (!ValidationUtils.isPositiveNumber(budgetText)) {
            showStatus("Budget must be a positive number.", DANGER);
            return;
        }
        manager.setBudget(Double.parseDouble(budgetText));
        showStatus("Budget updated!", SUCCESS);
    }

    /** Resets description and amount; keeps date set to today. */
    private void clearExpenseFields() {
        descriptionField.setText("");
        amountField.setText("");
        dateField.setText(DateUtils.todayAsInputString());
    }

    private void showStatus(String message, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setText(message);
    }
}
