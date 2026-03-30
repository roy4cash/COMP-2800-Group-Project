/**
 * File: DbErrorFormatter.java
 * Purpose: Converts raw database errors into shorter, user-facing messages.
 *
 * The project uses this helper so JDBC failures can be shown safely in the UI
 * without exposing unnecessarily technical text to end users.
 */
package util;

/**
 * Converts raw JDBC/DB errors into shorter, demo-friendly user messages.
 */
public final class DbErrorFormatter {

    /** Prevents instantiation because this is a static utility class. */
    private DbErrorFormatter() {}

    /**
     * Converts raw JDBC/MySQL error text into a shorter message suitable for UI labels.
     *
     * The mapping is intentionally keyword-based because the application only
     * needs to recognize a small set of common deployment and schema failures.
     */
    public static String format(String rawMessage) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            return "Database operation failed. Check the MySQL setup and try again.";
        }

        String message = rawMessage.trim();
        String lower = message.toLowerCase();

        if (lower.contains("missing database configuration")) {
            return "Database is not configured. Set FAT_DB_URL, FAT_DB_USER, and FAT_DB_PASSWORD first.";
        }
        if (lower.contains("mysql jdbc driver not found")) {
            return "MySQL JDBC driver is missing from lib/. Add mysql-connector-j before running the app.";
        }
        if (lower.contains("communications link failure")
                || lower.contains("connection refused")
                || lower.contains("could not connect")
                || lower.contains("connect timed out")) {
            return "Could not connect to MySQL. Make sure the MySQL service is running and the FAT_DB settings are correct.";
        }
        if (lower.contains("access denied")) {
            return "MySQL login failed. Check FAT_DB_USER and FAT_DB_PASSWORD.";
        }
        if (lower.contains("unknown database")) {
            return "Database 'fat' was not found. Create it and run sql/fat_schema.sql.";
        }
        if (lower.contains("doesn't exist")) {
            return "Required database tables are missing. Run sql/fat_schema.sql and try again.";
        }
        if (lower.contains("foreign key constraint fails")) {
            return "Related database records are missing. Re-run sql/fat_schema.sql to restore the default data.";
        }

        return "Database operation failed. Check the MySQL setup and try again.";
    }
}
