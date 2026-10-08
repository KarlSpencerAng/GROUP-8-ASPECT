/**
 * TestGradeConcernSubmission.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestGradeConcernSubmission {

    public static void main(String[] args) {

        try (Connection connection =
                DatabaseConnection.getConnection()) {

            // Safety check: never run this test on the original database.
            String database = connection.getCatalog();

            if (!"GradingSystem_Test".equalsIgnoreCase(database)) {
                throw new IllegalStateException(
                    "STOP: Connected to " + database
                    + ". Use Login_Test_Database.");
            }

            System.out.println("Connected to: " + database);

            String studentNumber = "20260001";
            String subjectCode = "CSS123P";

            // Prevent accidentally creating another test concern.
            String checkSql =
                "SELECT COUNT(*) FROM Grade_Concerns gc "
              + "JOIN Users u ON gc.StudentID = u.UserID "
              + "JOIN Subjects s ON gc.SubjectID = s.SubjectID "
              + "WHERE u.StudentNumber = ? "
              + "AND s.SubjectCode = ? "
              + "AND gc.Status IN ('Pending', 'Under Review')";

            try (PreparedStatement check =
                    connection.prepareStatement(checkSql)) {

                check.setString(1, studentNumber);
                check.setString(2, subjectCode);

                try (ResultSet result = check.executeQuery()) {
                    result.next();

                    if (result.getInt(1) > 0) {
                        System.out.println(
                            "STOP: An active CSS123P concern "
                            + "already exists.");
                        return;
                    }
                }
            }

            // Test 1: Submit a new concern.
            int concernId =
                GradeConcernRepository.submitConcern(
                    studentNumber,
                    subjectCode,
                    "Phase 6B test: Please review my CSS123P grade."
                );

            System.out.println(
                "PASS: Created concern ID " + concernId);

            // Test 2: Verify duplicate protection.
            try {
                GradeConcernRepository.submitConcern(
                    studentNumber,
                    subjectCode,
                    "Duplicate test concern"
                );

                throw new AssertionError(
                    "FAIL: Duplicate submission was accepted.");

            } catch (IllegalStateException expected) {
                if (expected.getMessage() == null
                        || !expected.getMessage()
                            .contains("active concern")) {
                    throw expected;
                }

                System.out.println(
                    "PASS: Duplicate concern was rejected.");
            }

            System.out.println(
                "Stage 6B repository test completed.");

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
