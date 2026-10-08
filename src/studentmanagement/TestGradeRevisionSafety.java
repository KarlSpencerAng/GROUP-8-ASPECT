/**
 * TestGradeRevisionSafety.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestGradeRevisionSafety {

    private static String snapshot(Connection connection)
            throws Exception {

        String sql =
            "SELECT g.CO1, g.CO2, g.CO3, "
          + "g.FinalExam, g.OLA, g.Coursera, "
          + "g.FinalPercentage, g.NumericalGrade, "
          + "g.SubmissionStatus, gc.Status, "
          + "(SELECT COUNT(*) "
          + " FROM Grade_Revision_History "
          + " WHERE ConcernID = 4) AS HistoryCount "
          + "FROM Grades g "
          + "JOIN Grade_Concerns gc "
          + "ON gc.StudentID = g.StudentID "
          + "AND gc.SubjectID = g.SubjectID "
          + "WHERE g.GradeID = 28 "
          + "AND gc.ConcernID = 4";

        try (PreparedStatement statement =
                connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (!result.next()) {
                throw new IllegalStateException(
                    "Expected test records not found.");
            }

            StringBuilder snapshot = new StringBuilder();

            for (int i = 1; i <= 11; i++) {
                snapshot.append(result.getString(i))
                        .append("|");
            }

            return snapshot.toString();
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

            String before = snapshot(connection);

            // Test 1: Reject an invalid score.
            try {
            	// Invalid score
            	GradeRevisionRepository.submitRevision(
            	        6, 4, 101, 88, 92, 89, 94);

                throw new AssertionError(
                    "FAIL: Invalid score was accepted.");

            } catch (IllegalArgumentException expected) {
                System.out.println(
                    "PASS: Invalid score rejected.");
            }

            // Test 2: Reject a nonexistent concern.
            try {
            	// Invalid concern ID
            	GradeRevisionRepository.submitRevision(
            	        6, -1, 90, 88, 92, 89, 94);
                throw new AssertionError(
                    "FAIL: Invalid concern ID was accepted.");

            } catch (IllegalArgumentException expected) {
                System.out.println(
                    "PASS: Invalid concern ID rejected.");
            }

            String after = snapshot(connection);

            if (!before.equals(after)) {
                throw new AssertionError(
                    "FAIL: Database records changed.");
            }

            System.out.println(
                "PASS: Grade, concern and history unchanged.");

            System.out.println(
                "Revision safety tests completed.");

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
