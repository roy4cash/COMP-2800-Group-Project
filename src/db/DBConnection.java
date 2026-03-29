package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides a fresh database connection on every call.
 * Each DAO closes its own connection via try-with-resources,
 * so we must NOT cache/reuse a single shared connection.
 */
public class DBConnection {

    private static final String DB_URL      = "jdbc:postgresql://db.wmrkmjdzgbtojpkmmzkr.supabase.co:5432/postgres";
    private static final String DB_USER     = "postgres";
    private static final String DB_PASSWORD = "lovecomp2800";

    private DBConnection() {}

    /** Returns a brand-new connection each time. Caller is responsible for closing it. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
