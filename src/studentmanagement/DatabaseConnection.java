/**
 * DatabaseConnection.java
 * Purpose: Database connection configuration.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private DatabaseConnection() {}

    public static Connection getConnection()
            throws SQLException {

        String url = System.getenv().getOrDefault(
            "GRADING_DB_URL",
            "jdbc:mysql://localhost:3306/GradingSystem"
            + "?useSSL=false&serverTimezone=Asia/Manila"
        );

        String user = System.getenv().getOrDefault(
            "GRADING_DB_USER", "root"
        );

        String password =
            System.getenv("GRADING_DB_PASSWORD");

        if (password == null) {
            throw new SQLException(
                "Set GRADING_DB_PASSWORD in Eclipse "
                + "Run Configurations > Environment."
            );
        }

        return DriverManager.getConnection(
            url, user, password
        );
    }
}
