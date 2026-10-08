/**
 * TestGradeRevisionAuthorization.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestGradeRevisionAuthorization {

    private static String snapshot(Connection connection)
            throws Exception {

        String sql =
            "SELECT g.CO1, g.CO2, g.CO3, "
          + "g.FinalExam, g.OLA, g.Coursera, "
          + "g.FinalPercentage, g.NumericalGrade, "
          + "g.SubmissionStatus, gc.Status, "
          + "(SELECT COUNT(*) "
          + "FROM Grade_Revision_History "
          + "WHERE ConcernID = 4) AS HistoryCount "
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
                        "Test records not found.");
            }

            StringBuilder values = new StringBuilder();

            for (int i = 1; i <= 11; i++) {
                values.append(result.getString(i))
                      .append("|");
            }

            return values.toString();
        }
    }

    public static void main(String[] args) {

        try (Connection connection =
                DatabaseConnection.getConnection()) {

            if (!"GradingSystem_Test".equalsIgnoreCase(
                    connection.getCatalog())) {
                throw new IllegalStateException(
                        "STOP: Use GradingSystem_Test.");
            }

            System.out.println(
                    "Connected to: GradingSystem_Test");

            String before = snapshot(connection);

            // Teacher 8 is not assigned to CSS123P.
            try {
                GradeRevisionRepository.submitRevision(
                        8, 4, 91, 88, 92, 89, 94);

                throw new AssertionError(
                        "FAIL: Unauthorized revision accepted.");

            } catch (IllegalStateException expected) {

                if (expected.getMessage() == null
                        || !expected.getMessage()
                                .contains("not authorized")) {
                    throw expected;
                }

                System.out.println(
                        "PASS: Unauthorized teacher rejected.");
            }

            String after = snapshot(connection);

            if (!before.equals(after)) {
                throw new AssertionError(
                        "FAIL: Database changed.");
            }

            System.out.println(
                    "PASS: Grade, concern and history unchanged.");

            System.out.println(
                    "Authorization test completed.");

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
