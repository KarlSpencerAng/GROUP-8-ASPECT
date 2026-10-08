/**
 * TestSuccessfulGradeRevision.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestSuccessfulGradeRevision {

    public static void main(String[] args) {

        try (Connection connection =
                DatabaseConnection.getConnection()) {

            if (!"GradingSystem_Test".equalsIgnoreCase(
                    connection.getCatalog())) {
                throw new IllegalStateException(
                        "STOP: Use GradingSystem_Test.");
            }

            // Confirm the original test records.
            String check =
                "SELECT g.CO1, g.FinalPercentage, "
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
                    connection.prepareStatement(check);
                 ResultSet result = statement.executeQuery()) {

                if (!result.next()
                        || result.getDouble("CO1") != 90
                        || Math.abs(result.getDouble(
                                "FinalPercentage") - 89.70) > 0.001
                        || !"Submitted".equals(
                                result.getString("SubmissionStatus"))
                        || !"Under Review".equals(
                                result.getString("Status"))
                        || result.getInt("HistoryCount") != 0) {

                    System.out.println(
                        "STOP: Test records differ from "
                        + "the expected baseline.");
                    return;
                }
            }

            // Perform one authorized revision.
            GradeRevisionRepository.submitRevision(
                    6, 4, 91, 88, 92, 89, 94);

            System.out.println(
                    "Revision transaction executed.");

            // Verify the new database values.
            try (PreparedStatement statement =
                    connection.prepareStatement(check);
                 ResultSet result = statement.executeQuery()) {

                if (!result.next()
                        || result.getDouble("CO1") != 91
                        || Math.abs(result.getDouble(
                                "FinalPercentage") - 89.85) > 0.001
                        || !"Revised".equals(
                                result.getString("SubmissionStatus"))
                        || !"Revised".equals(
                                result.getString("Status"))
                        || result.getInt("HistoryCount") != 1) {

                    throw new AssertionError(
                            "FAIL: Revision results do not match.");
                }
            }

            System.out.println(
                    "PASS: Grade and concern updated.");
            System.out.println(
                    "PASS: Exactly one history record created.");
            System.out.println(
                    "Successful revision test completed.");

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
