/**
 * TestDuplicateGradeRevision.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestDuplicateGradeRevision {

    public static void main(String[] args) {

        try (Connection connection =
                DatabaseConnection.getConnection()) {

            if (!"GradingSystem_Test".equalsIgnoreCase(
                    connection.getCatalog())) {
                throw new IllegalStateException(
                        "STOP: Use GradingSystem_Test.");
            }

            // Concern 4 has already been revised.
            try {
                GradeRevisionRepository.submitRevision(
                        6, 4, 95, 88, 92, 89, 94);

                throw new AssertionError(
                        "FAIL: Duplicate revision accepted.");

            } catch (IllegalStateException expected) {
                System.out.println(
                        "PASS: Duplicate revision rejected.");
            }

            String sql =
                    "SELECT g.CO1, g.FinalPercentage, "
                  + "g.SubmissionStatus, gc.Status, "
                  + "(SELECT COUNT(*) "
                  + " FROM Grade_Revision_History "
                  + " WHERE ConcernID = 4) AS HistoryCount "
                  + "FROM Grades g "
                  + "JOIN Grade_Concerns gc "
                  + " ON gc.StudentID = g.StudentID "
                  + " AND gc.SubjectID = g.SubjectID "
                  + "WHERE g.GradeID = 28 "
                  + "AND gc.ConcernID = 4";

            try (PreparedStatement statement =
                    connection.prepareStatement(sql);
                 ResultSet result =
                    statement.executeQuery()) {

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
                            "FAIL: Existing revision changed.");
                }
            }

            System.out.println(
                    "PASS: Existing revision remains unchanged.");
            System.out.println(
                    "Duplicate protection test completed.");

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
