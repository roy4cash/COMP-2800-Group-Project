/**
 * File: BudgetDAO.java
 * Purpose: JDBC data-access layer for reading and writing monthly budget records.
 *
 * The UI and manager layers call this class instead of embedding SQL directly,
 * which keeps database logic centralized and easier to maintain.
 */
package db;

import model.Budget;

import java.sql.*;

/**
 * Handles reading and writing budget records.
 * saveBudget() automatically inserts or updates as needed.
 */
public class BudgetDAO {

    private String lastLoadErrorMessage;

    /**
     * Loads one monthly budget record for a specific user.
     *
     * Returning a zero-value Budget object when no row exists keeps callers
     * simple because they do not need null checks just to render the UI.
     */
    public Budget getBudget(int userId, int month, int year) {
        lastLoadErrorMessage = null;
        String sql = "SELECT id, amount FROM budgets WHERE user_id=? AND month=? AND year=?";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Budget(rs.getInt("id"), userId, month, year, rs.getDouble("amount"));
            }

        } catch (SQLException e) {
            System.err.println("BudgetDAO.getBudget: " + e.getMessage());
            lastLoadErrorMessage = e.getMessage();
        }

        // Return a zero budget if none exists yet
        return new Budget(0, userId, month, year, 0.0);
    }

    /**
     * Saves the monthly budget by choosing between insert and update logic.
     *
     * This split keeps the SQL straightforward and avoids database-specific
     * upsert syntax that would make the code harder for students to follow.
     */
    public String saveBudget(int userId, int month, int year, double amount) {
        Budget existing = getBudget(userId, month, year);
        if (existing.getId() == 0) {
            return insertBudget(userId, month, year, amount);
        } else {
            return updateBudget(existing.getId(), amount);
        }
    }

    /** Inserts a new budget row when the selected month does not yet have one. */
    private String insertBudget(int userId, int month, int year, double amount) {
        String sql = "INSERT INTO budgets (user_id, month, year, amount) VALUES (?, ?, ?, ?)";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ps.setDouble(4, amount);
            ps.executeUpdate();
            return null;

        } catch (SQLException e) {
            System.err.println("BudgetDAO.insertBudget: " + e.getMessage());
            return e.getMessage();
        }
    }

    /** Updates the amount on an existing budget row. */
    private String updateBudget(int budgetId, double amount) {
        String sql = "UPDATE budgets SET amount=? WHERE id=?";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setDouble(1, amount);
            ps.setInt(2, budgetId);
            ps.executeUpdate();
            return null;

        } catch (SQLException e) {
            System.err.println("BudgetDAO.updateBudget: " + e.getMessage());
            return e.getMessage();
        }
    }

    /** Last error encountered while loading the budget, or null if the last load succeeded. */
    public String getLastLoadErrorMessage() {
        return lastLoadErrorMessage;
    }
}
