package db;

import model.Budget;

import java.sql.*;

/**
 * Handles reading and writing budget records.
 * saveBudget() automatically inserts or updates as needed.
 */
public class BudgetDAO {

    public Budget getBudget(int userId, int month, int year) {
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
        }

        // Return a zero budget if none exists yet
        return new Budget(0, userId, month, year, 0.0);
    }

    /** Insert or update the budget for the given month/year. */
    public void saveBudget(int userId, int month, int year, double amount) {
        Budget existing = getBudget(userId, month, year);
        if (existing.getId() == 0) {
            insertBudget(userId, month, year, amount);
        } else {
            updateBudget(existing.getId(), amount);
        }
    }

    private void insertBudget(int userId, int month, int year, double amount) {
        String sql = "INSERT INTO budgets (user_id, month, year, amount) VALUES (?, ?, ?, ?)";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ps.setDouble(4, amount);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("BudgetDAO.insertBudget: " + e.getMessage());
        }
    }

    private void updateBudget(int budgetId, double amount) {
        String sql = "UPDATE budgets SET amount=? WHERE id=?";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setDouble(1, amount);
            ps.setInt(2, budgetId);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("BudgetDAO.updateBudget: " + e.getMessage());
        }
    }
}
