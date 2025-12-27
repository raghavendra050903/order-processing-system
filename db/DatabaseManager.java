package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL =
            "jdbc:sqlite:orders.db?busy_timeout=5000";

    static {
        try {
            Class.forName("org.sqlite.JDBC");

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                stmt.execute("PRAGMA journal_mode=WAL;");

                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS orders (" +
                        "order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "customer_id INTEGER," +
                        "order_amount REAL," +
                        "order_time TEXT DEFAULT CURRENT_TIMESTAMP," +
                        "status TEXT," +
                        "priority TEXT," +
                        "retry_count INTEGER DEFAULT 0)"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(DB_URL);
    }
}
