package observer;

import db.BudgetDAO;
import db.CategoryDAO;
import db.ExpenseDAO;
import model.Budget;
import model.Category;
import model.Expense;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Central class that holds all expense/budget business logic.
 *
 * Acts as the Subject in the Observer pattern:
 *   - UI panels register themselves as Observers.
 *   - Whenever data changes (add/delete expense, set budget),
 *     all registered panels are notified and refresh themselves.
 *
 * This separates business logic from the UI — panels never
 * touch the database directly.
 */
public class ExpenseManager implements Subject {

    // All actions use this hardcoded user (single-user app)
    private static final int DEFAULT_USER_ID = 1;

    private final ExpenseDAO  expenseDAO;
    private final BudgetDAO   budgetDAO;
    private final CategoryDAO categoryDAO;

    private final List<Observer> observers = new ArrayList<>();

    public ExpenseManager() {
        this.expenseDAO  = new ExpenseDAO();
        this.budgetDAO   = new BudgetDAO();
        this.categoryDAO = new CategoryDAO();
        ensureDefaultUserExists();
    }

    /**
     * Makes sure user_id=1 exists in the users table.
     * Without this, every INSERT into expenses fails with a foreign-key violation.
     */
    private void ensureDefaultUserExists() {
        // First try: insert user with explicit id=1
        String sql = "INSERT INTO users (id, username) VALUES (1, 'default_user') " +
                     "ON CONFLICT DO NOTHING";
        try (java.sql.Connection conn = db.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            System.out.println("ensureDefaultUserExists: OK");
        } catch (java.sql.SQLException e) {
            System.err.println("ensureDefaultUserExists FAILED: " + e.getMessage());
            // Fallback: insert without explicit id (let SERIAL assign it)
            String sql2 = "INSERT INTO users (username) VALUES ('default_user') ON CONFLICT (username) DO NOTHING";
            try (java.sql.Connection conn2 = db.DBConnection.getConnection();
                 java.sql.PreparedStatement ps2 = conn2.prepareStatement(sql2)) {
                ps2.executeUpdate();
                System.out.println("ensureDefaultUserExists fallback: OK");
            } catch (java.sql.SQLException e2) {
                System.err.println("ensureDefaultUserExists fallback FAILED: " + e2.getMessage());
            }
        }
    }

    // ----------------------------------------------------------------
    // Observer pattern
    // ----------------------------------------------------------------

    @Override
    public void addObserver(Observer o)    { observers.add(o); }

    @Override
    public void removeObserver(Observer o) { observers.remove(o); }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }

    // ----------------------------------------------------------------
    // Expense operations
    // ----------------------------------------------------------------

    /**
     * Adds a new expense and notifies all registered observers.
     * Returns null on success, or an error message string on failure.
     */
    public String addExpense(int categoryId, String description, double amount, LocalDate date) {
        String error = expenseDAO.addExpense(DEFAULT_USER_ID, categoryId, description, amount, date);
        notifyObservers();
        return error;
    }

    /** Deletes the expense with the given ID and notifies all registered observers. */
    public void deleteExpense(int expenseId) {
        expenseDAO.deleteExpense(expenseId);
        notifyObservers();
    }

    /** Returns every expense for the default user, newest first. */
    public List<Expense> getAllExpenses() {
        return expenseDAO.getAllExpenses(DEFAULT_USER_ID);
    }

    // ----------------------------------------------------------------
    // Budget operations
    // ----------------------------------------------------------------

    /** Returns the Budget object for the current month (amount may be 0 if not set). */
    public Budget getCurrentBudget() {
        LocalDate now = LocalDate.now();
        return budgetDAO.getBudget(DEFAULT_USER_ID, now.getMonthValue(), now.getYear());
    }

    /** Saves or updates the monthly budget and notifies observers. */
    public void setBudget(double amount) {
        LocalDate now = LocalDate.now();
        budgetDAO.saveBudget(DEFAULT_USER_ID, now.getMonthValue(), now.getYear(), amount);
        notifyObservers();
    }

    /** Returns the total amount spent in the current calendar month. */
    public double getTotalSpentThisMonth() {
        LocalDate now = LocalDate.now();
        return expenseDAO.getTotalForMonth(DEFAULT_USER_ID, now.getMonthValue(), now.getYear());
    }

    // ----------------------------------------------------------------
    // Chart / insights data
    // ----------------------------------------------------------------

    /**
     * Returns a map of category name to total amount spent in the current month.
     * Ordered by total descending (largest first).
     */
    public Map<String, Double> getSpendingByCategory() {
        LocalDate now = LocalDate.now();
        return expenseDAO.getSpendingByCategory(DEFAULT_USER_ID, now.getMonthValue(), now.getYear());
    }

    /**
     * Returns the total number of expense records for the default user.
     * Used by InsightsPanel to populate the "Transactions" stat card.
     */
    public int getExpenseCount() {
        return getAllExpenses().size();
    }

    /**
     * Calculates the average amount spent per day so far in the current month.
     *
     * Formula: totalSpentThisMonth / dayOfMonth
     *
     * Returns 0 if today is day 1 or no spending has occurred.
     */
    public double getAverageDailySpend() {
        LocalDate now       = LocalDate.now();
        int       dayOfMonth = now.getDayOfMonth();
        if (dayOfMonth == 0) return 0;

        double total = getTotalSpentThisMonth();
        return total / dayOfMonth;
    }

    /**
     * Returns the name of the category with the highest total spend in the current month.
     *
     * Iterates over the spending-by-category map (already sorted descending) and
     * returns the first key, or "None" if there is no spending data.
     */
    public String getTopCategory() {
        Map<String, Double> spending = getSpendingByCategory();
        if (spending.isEmpty()) return "None";

        // getSpendingByCategory returns a LinkedHashMap ordered by total DESC,
        // so the first entry is the top category
        return spending.keySet().iterator().next();
    }

    // ----------------------------------------------------------------
    // Category lookup
    // ----------------------------------------------------------------

    /**
     * Returns all available expense categories.
     * CategoryDAO will auto-seed defaults if the DB is empty.
     */
    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }
}
