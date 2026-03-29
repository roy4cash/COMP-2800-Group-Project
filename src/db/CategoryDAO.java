package db;

import model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for the categories table.
 *
 * If the database returns no categories (e.g. fresh DB, missing seed data),
 * this DAO auto-inserts a hardcoded fallback list and returns it.
 * This prevents the category dropdown in AddExpensePanel from being empty.
 */
public class CategoryDAO {

    /** Hardcoded default categories — id matches what the DB should have. */
    private static final Object[][] DEFAULT_CATEGORIES = {
        {1, "Food"},
        {2, "Transport"},
        {3, "Entertainment"},
        {4, "Utilities"},
        {5, "Health"},
        {6, "Education"},
        {7, "Shopping"},
        {8, "Investment"},
        {9, "Other"}
    };

    /**
     * Returns all categories ordered by name.
     * Falls back to in-memory defaults if the DB is unreachable or empty,
     * so the category dropdown is never blank.
     */
    public List<Category> getAllCategories() {
        List<Category> categories = fetchCategories();

        if (categories.isEmpty()) {
            System.out.println("CategoryDAO: DB empty — seeding defaults.");
            insertDefaultCategories();
            categories = fetchCategories();
        }

        // Last-resort: if DB is completely unreachable, return hardcoded list
        if (categories.isEmpty()) {
            System.out.println("CategoryDAO: Using in-memory fallback categories.");
            categories = getHardcodedCategories();
        }

        return categories;
    }

    /** Returns hardcoded categories without any DB call. */
    private List<Category> getHardcodedCategories() {
        List<Category> list = new ArrayList<>();
        for (Object[] row : DEFAULT_CATEGORIES) {
            list.add(new Category((Integer) row[0], (String) row[1]));
        }
        return list;
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    /** Executes the SELECT and returns whatever is in the table. */
    private List<Category> fetchCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name FROM categories ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }

        } catch (SQLException e) {
            System.err.println("CategoryDAO.fetchCategories: " + e.getMessage());
        }

        return categories;
    }

    /**
     * Inserts the hardcoded default categories into the DB.
     * Uses INSERT ON CONFLICT DO NOTHING so calling this multiple times
     * is safe — no duplicate rows will be created.
     */
    private void insertDefaultCategories() {
        String sql = "INSERT INTO categories (id, name) VALUES (?, ?) ON CONFLICT DO NOTHING";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            for (Object[] row : DEFAULT_CATEGORIES) {
                ps.setInt(1, (Integer) row[0]);
                ps.setString(2, (String) row[1]);
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("CategoryDAO: Default categories inserted successfully.");

        } catch (SQLException e) {
            System.err.println("CategoryDAO.insertDefaultCategories: " + e.getMessage());
        }
    }
}
