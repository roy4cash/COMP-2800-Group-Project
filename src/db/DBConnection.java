package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides a fresh MySQL connection on every call.
 * Database settings come from environment variables or Java system properties
 * so the checked-in source stays aligned with the runtime setup instructions.
 */
public class DBConnection {

    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static final String URL_PROPERTY      = "fat.db.url";
    private static final String USER_PROPERTY     = "fat.db.user";
    private static final String PASSWORD_PROPERTY = "fat.db.password";

    private static final String URL_ENV      = "FAT_DB_URL";
    private static final String USER_ENV     = "FAT_DB_USER";
    private static final String PASSWORD_ENV = "FAT_DB_PASSWORD";

    private DBConnection() {}

    /** Returns a brand-new connection each time. Caller is responsible for closing it. */
    public static Connection getConnection() throws SQLException {
        String url      = readSetting(URL_PROPERTY, URL_ENV);
        String user     = readSetting(USER_PROPERTY, USER_ENV);
        String password = readSetting(PASSWORD_PROPERTY, PASSWORD_ENV);

        if (url.isBlank() || user.isBlank() || password.isBlank()) {
            throw new SQLException(
                "Missing database configuration. Set " +
                URL_ENV + ", " + USER_ENV + ", and " + PASSWORD_ENV +
                " or the matching Java system properties " +
                URL_PROPERTY + ", " + USER_PROPERTY + ", and " + PASSWORD_PROPERTY + "."
            );
        }

        if (!url.startsWith("jdbc:mysql://")) {
            throw new SQLException(
                "Invalid database URL. FAT now expects a MySQL JDBC URL such as " +
                "\"jdbc:mysql://localhost:3306/fat?serverTimezone=UTC\"."
            );
        }

        loadMySqlDriver();
        return DriverManager.getConnection(url, user, password);
    }

    private static void loadMySqlDriver() throws SQLException {
        try {
            Class.forName(MYSQL_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "MySQL JDBC driver not found. Add mysql-connector-j-8.4.0.jar " +
                "or a compatible MySQL Connector/J JAR to lib/ before running the app.",
                e
            );
        }
    }

    private static String readSetting(String propertyName, String envName) {
        String value = System.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            value = System.getenv(envName);
        }
        return value == null ? "" : value.trim();
    }
}
