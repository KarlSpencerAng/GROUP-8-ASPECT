/**
 * TestGradeRevisionRollback.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestGradeRevisionRollback {

    private static String snapshot(Connection connection)
            throws SQLException {

        String sql =
            "SELECT g.CO1, g.CO2, g.CO3, "
          + "g.FinalExam, g.OLA, g.Coursera, "
          + "g.FinalPercentage, g.NumericalGrade, "
          + "g.SubmissionStatus, gc.Status, "
          + "(SELECT COUNT(*) "
          + " FROM Grade_Revision_History "
          + " WHERE ConcernID = 1) AS HistoryCount "
          + "FROM Grades g "
          + "JOIN Grade_Concerns gc "
          + " ON gc.StudentID = g.StudentID "
          + " AND gc.SubjectID = g.SubjectID "
          + "WHERE g.GradeID = 1 "
          + "AND gc.ConcernID = 1";

        try (PreparedStatement statement =
                connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (!result.next()) {
                throw new IllegalStateException(
                        "Expected records not found.");
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

            String before = snapshot(connection);

            // Verify the expected baseline.
            if (!before.equals(
                    "90.00|88.00|92.00|89.00|95.00|94.00|"
                  + "90.30|1.75|Submitted|Under Review|0|")) {

                throw new IllegalStateException(
                        "STOP: Baseline differs. "
                      + "No changes were made.");
            }

            System.out.println(
                    "PASS: Original baseline verified.");

            connection.setAutoCommit(false);

            try {
                // Insert temporary audit history.
                // Original and new values are identical
                // because this is only a rollback test.
                String insert =
                    "INSERT INTO Grade_Revision_History ("
                  + "GradeID, ConcernID, "
                  + "OldCO1, OldCO2, OldCO3, "
                  + "OldFinalExam, OldOLA, OldCoursera, "
                  + "OldFinalPercentage, OldNumericalGrade, "
                  + "NewCO1, NewCO2, NewCO3, "
                  + "NewFinalExam, NewOLA, NewCoursera, "
                  + "NewFinalPercentage, NewNumericalGrade"
                  + ") "
                  + "SELECT GradeID, 1, "
                  + "CO1, CO2, CO3, FinalExam, "
                  + "OLA, Coursera, FinalPercentage, "
                  + "NumericalGrade, "
                  + "CO1, CO2, CO3, FinalExam, "
                  + "OLA, Coursera, FinalPercentage, "
                  + "NumericalGrade "
                  + "FROM Grades WHERE GradeID = 1";

                try (PreparedStatement statement =
                        connection.prepareStatement(insert)) {

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "History insertion failed.");
                    }
                }

                System.out.println(
                    "PASS: Temporary history inserted.");

                // Deliberately trigger a SQL error.
                // This column does not exist.
                String invalidSql =
                    "UPDATE Grades "
                  + "SET NonexistentTestColumn = 1 "
                  + "WHERE GradeID = 1";

                try (PreparedStatement statement =
                        connection.prepareStatement(invalidSql)) {

                    statement.executeUpdate();
                }

                throw new AssertionError(
                        "FAIL: Expected SQL error did not occur.");

            } catch (SQLException expected) {

                connection.rollback();

                System.out.println(
                    "PASS: Deliberate SQL error triggered.");
                System.out.println(
                    "PASS: Transaction rolled back.");

            } finally {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                }
            }

            String after = snapshot(connection);

            if (!before.equals(after)) {
                throw new AssertionError(
                        "FAIL: Database changed after rollback.");
            }

            System.out.println(
                "PASS: Grade, concern and history unchanged.");
            System.out.println(
                "Rollback test completed successfully.");

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}