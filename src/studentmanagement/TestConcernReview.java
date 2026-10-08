/**
 * TestConcernReview.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestConcernReview {

    private static String getStatus(
            Connection connection, int concernId)
            throws Exception {

        String sql =
                "SELECT Status FROM Grade_Concerns "
              + "WHERE ConcernID = ?";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, concernId);

            try (ResultSet results =
                         statement.executeQuery()) {

                if (!results.next()) {
                    throw new IllegalStateException(
                            "Concern not found.");
                }

                return results.getString("Status");
            }
        }
    }

    public static void main(String[] args) {

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            String database = connection.getCatalog();

            if (!"GradingSystem_Test"
                    .equalsIgnoreCase(database)) {
                throw new IllegalStateException(
                        "STOP: Use GradingSystem_Test.");
            }

            System.out.println(
                    "Connected to: " + database);

            int concernId = 4;

            // Ensure the concern is Pending.
            String originalStatus =
                    getStatus(connection, concernId);

            if (!"Pending".equals(originalStatus)) {
                System.out.println(
                        "STOP: Concern 4 is already "
                        + originalStatus);
                return;
            }

            // Test 1: Change Pending to Under Review.
            GradeConcernRepository.markUnderReview(
                    concernId);

            String updatedStatus =
                    getStatus(connection, concernId);

            if (!"Under Review".equals(updatedStatus)) {
                throw new AssertionError(
                        "FAIL: Status was not saved.");
            }

            System.out.println(
                    "PASS: Concern 4 is Under Review.");

            // Test 2: Attempt the same update again.
            try {
                GradeConcernRepository.markUnderReview(
                        concernId);

                throw new AssertionError(
                        "FAIL: Repeated update was accepted.");

            } catch (IllegalStateException expected) {
                System.out.println(
                        "PASS: Repeated update rejected.");
            }

            System.out.println(
                    "Concern review test completed.");

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
