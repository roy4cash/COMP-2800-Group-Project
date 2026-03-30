package db;

import model.Investment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for the investments table.
 *
 * The investments table is created automatically on first use via
 * createTableIfNotExists(), so the user does not need to run any SQL manually.
 *
 * All methods use parameterized queries to prevent SQL injection.
 */
public class InvestmentDAO {

    private String lastLoadErrorMessage;

    // DDL — executed once when InvestmentPanel is constructed
    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS investments (" +
        "  id            INT AUTO_INCREMENT PRIMARY KEY," +
        "  user_id       INT NOT NULL," +
        "  name          VARCHAR(100) NOT NULL," +
        "  ticker        VARCHAR(20)," +
        "  type          VARCHAR(50) DEFAULT 'Stock'," +
        "  shares        DECIMAL(15,6) NOT NULL DEFAULT 0," +
        "  buy_price     DECIMAL(15,2) NOT NULL," +
        "  current_price DECIMAL(15,2) DEFAULT 0," +
        "  purchase_date DATE," +
        "  notes         VARCHAR(255)," +
        "  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP," +
        "  KEY idx_investments_user_created (user_id, created_at)," +
        "  CONSTRAINT fk_investments_user FOREIGN KEY (user_id) REFERENCES users(id)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    /**
     * Creates the investments table if it does not already exist.
     * Safe to call on every application startup — IF NOT EXISTS prevents errors.
     */
    public String createTableIfNotExists() {
        try (Connection conn = DBConnection.getConnection();
             Statement  stmt = conn.createStatement()) {

            stmt.execute(CREATE_TABLE_SQL);
            System.out.println("InvestmentDAO: investments table ready.");
            return null;

        } catch (SQLException e) {
            System.err.println("InvestmentDAO.createTableIfNotExists: " + e.getMessage());
            return e.getMessage();
        }
    }

    /**
     * Returns all investments for the given user, ordered by creation date descending.
     *
     * @param userId  the user whose investments to fetch
     * @return        list of Investment objects, newest first
     */
    public List<Investment> getAllInvestments(int userId) {
        List<Investment> list = new ArrayList<>();
        lastLoadErrorMessage = null;
        String sql =
            "SELECT id, user_id, name, ticker, type, shares, buy_price, current_price, " +
            "       purchase_date, notes " +
            "FROM   investments " +
            "WHERE  user_id = ? " +
            "ORDER BY created_at DESC";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Investment(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("ticker"),
                    rs.getString("type"),
                    rs.getDouble("shares"),
                    rs.getDouble("buy_price"),
                    rs.getDouble("current_price"),
                    rs.getString("purchase_date"),
                    rs.getString("notes")
                ));
            }

        } catch (SQLException e) {
            System.err.println("InvestmentDAO.getAllInvestments: " + e.getMessage());
            lastLoadErrorMessage = e.getMessage();
        }

        return list;
    }

    /**
     * Inserts a new investment record into the database.
     *
     * @param userId        owner user ID
     * @param name          human-readable investment name
     * @param ticker        ticker symbol (can be empty string)
     * @param type          asset type: Stock, ETF, Crypto, Bond, Other
     * @param shares        number of shares/units
     * @param buyPrice      purchase price per share
     * @param currentPrice  current market price per share
     * @param date          purchase date as YYYY-MM-DD string
     * @param notes         optional notes
     */
    public String addInvestment(int userId, String name, String ticker, String type,
                                double shares, double buyPrice, double currentPrice,
                                String date, String notes) {
        String sql =
            "INSERT INTO investments " +
            "  (user_id, name, ticker, type, shares, buy_price, current_price, purchase_date, notes) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, ticker);
            ps.setString(4, type);
            ps.setDouble(5, shares);
            ps.setDouble(6, buyPrice);
            ps.setDouble(7, currentPrice);
            ps.setDate(8, java.sql.Date.valueOf(date));
            ps.setString(9, notes);
            ps.executeUpdate();
            return null;

        } catch (SQLException e) {
            System.err.println("InvestmentDAO.addInvestment: " + e.getMessage());
            return e.getMessage();
        }
    }

    /**
     * Updates only the current_price field for the given investment.
     * Used when the user manually refreshes a price quote.
     *
     * @param id     the investment's database ID
     * @param price  the new current market price
     */
    public void updateCurrentPrice(int id, double price) {
        String sql = "UPDATE investments SET current_price = ? WHERE id = ?";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setDouble(1, price);
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("InvestmentDAO.updateCurrentPrice: " + e.getMessage());
        }
    }

    /**
     * Deletes the investment with the given ID.
     *
     * @param id  the database primary key of the investment to remove
     */
    public String deleteInvestment(int id) {
        String sql = "DELETE FROM investments WHERE id = ?";

        try (Connection       conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int deleted = ps.executeUpdate();
            if (deleted == 0) {
                return "Investment not found. It may have already been removed.";
            }
            return null;

        } catch (SQLException e) {
            System.err.println("InvestmentDAO.deleteInvestment: " + e.getMessage());
            return e.getMessage();
        }
    }

    /** Last error encountered while loading investments, or null if the last load succeeded. */
    public String getLastLoadErrorMessage() {
        return lastLoadErrorMessage;
    }
}
