package db;

import model.Expense;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Handles all CRUD operations for the expenses table.
 */
public class ExpenseDAO {

    /**
     * Returns all expenses for the given user, newest first.
     * The category name is fetched with a JOIN so the UI does not need a separate lookup.
     */
    public List<Expense> getAllExpenses(int userId) {
        List<Expense> expenses = new ArrayList<>();
        String sql =
            "SELECT e.id, e.user_id, e.category_id, c.name AS category_name, " +
            "       e.description, e.amount, e.expense_date " +
            "FROM   expenses e " +
            "JOIN   categories c ON e.category_id = c.id " +
            "WHERE  e.user_id = ? " +
            "ORDER BY e.expense_date DESC";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                expenses.add(new Expense(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getString("description"),
                    rs.getDouble("amount"),
                    rs.getDate("expense_date").toLocalDate()
                ));
            }

        } catch (SQLException e) {
            System.err.println("ExpenseDAO.getAllExpenses: " + e.getMessage());
        }

        return expenses;
    }

    /**
     * Inserts a new expense. Returns null on success, or the error message string on failure.
     * Returning the message lets the UI display the exact DB error to the user.
     */
    public String addExpense(int userId, int categoryId, String description,
                             double amount, LocalDate date) {
        String sql =
            "INSERT INTO expenses (user_id, category_id, description, amount, expense_date) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setString(3, description);
            ps.setDouble(4, amount);
            ps.setDate(5, java.sql.Date.valueOf(date));
            ps.executeUpdate();
            return null; // success

        } catch (SQLException e) {
            System.err.println("ExpenseDAO.addExpense ERROR: " + e.getMessage());
            return e.getMessage(); // return actual error to caller
        }
    }

    /** Deletes the expense with the given ID. */
    public void deleteExpense(int expenseId) {
        String sql = "DELETE FROM expenses WHERE id = ?";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setInt(1, expenseId);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("ExpenseDAO.deleteExpense: " + e.getMessage());
        }
    }

    /** Returns the total amount spent by the user in the given month/year. */
    public double getTotalForMonth(int userId, int month, int year) {
        String sql =
            "SELECT COALESCE(SUM(amount), 0) " +
            "FROM   expenses " +
            "WHERE  user_id = ? AND EXTRACT(MONTH FROM expense_date) = ? AND EXTRACT(YEAR FROM expense_date) = ?";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);

        } catch (SQLException e) {
            System.err.println("ExpenseDAO.getTotalForMonth: " + e.getMessage());
        }

        return 0.0;
    }

    /**
     * Returns a map of category name -> total spent for the current month.
     * Used to build the pie chart.
     */
    public Map<String, Double> getSpendingByCategory(int userId, int month, int year) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql =
            "SELECT c.name, SUM(e.amount) AS total " +
            "FROM   expenses e " +
            "JOIN   categories c ON e.category_id = c.id " +
            "WHERE  e.user_id = ? AND EXTRACT(MONTH FROM e.expense_date) = ? AND EXTRACT(YEAR FROM e.expense_date) = ? " +
            "GROUP BY c.name " +
            "ORDER BY total DESC";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                result.put(rs.getString("name"), rs.getDouble("total"));
            }

        } catch (SQLException e) {
            System.err.println("ExpenseDAO.getSpendingByCategory: " + e.getMessage());
        }

        return result;
    }
}
