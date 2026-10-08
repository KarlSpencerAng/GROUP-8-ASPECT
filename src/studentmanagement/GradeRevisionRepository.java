/**
 * GradeRevisionRepository.java
 * Purpose: Standard grade revision transactions.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class GradeRevisionRepository {

    private GradeRevisionRepository() {
    }

    private static BigDecimal number(double value) {
        if (!Double.isFinite(value)
                || value < 0 || value > 100) {
            throw new IllegalArgumentException(
                    "Every score must be between 0 and 100.");
        }

        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateFinal(
            BigDecimal co1,
            BigDecimal co2,
            BigDecimal co3,
            BigDecimal exam,
            BigDecimal ola,
            BigDecimal coursera) {

        return co1.multiply(new BigDecimal("0.15"))
                .add(co2.multiply(new BigDecimal("0.15")))
                .add(co3.multiply(new BigDecimal("0.15")))
                .add(exam.multiply(new BigDecimal("0.40")))
                .add(ola.multiply(new BigDecimal("0.10")))
                .add(coursera.multiply(new BigDecimal("0.05")))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal numericalGrade(
            BigDecimal percentage) {

        double value = percentage.doubleValue();

        if (value >= 98) return new BigDecimal("1.00");
        if (value >= 95) return new BigDecimal("1.25");
        if (value >= 92) return new BigDecimal("1.50");
        if (value >= 89) return new BigDecimal("1.75");
        if (value >= 86) return new BigDecimal("2.00");
        if (value >= 83) return new BigDecimal("2.25");
        if (value >= 80) return new BigDecimal("2.50");
        if (value >= 78) return new BigDecimal("2.75");
        if (value >= 75) return new BigDecimal("3.00");

        return new BigDecimal("5.00");
    }

    public static void submitRevision(
            int teacherId,
            int concernId,
            double newCO1,
            double newCO2,
            double newCO3,
            double newFinalExam,
            double newCoursera) throws SQLException {
    	
    	if (teacherId <= 0) {
    	    throw new IllegalArgumentException(
    	            "A valid teacher ID is required.");
    	}

        if (concernId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid concern ID.");
        }

        // Validate all proposed values before opening
        // the database transaction.
        BigDecimal co1 = number(newCO1);
        BigDecimal co2 = number(newCO2);
        BigDecimal co3 = number(newCO3);
        BigDecimal exam = number(newFinalExam);
        BigDecimal coursera = number(newCoursera);

        String lockConcern =
                "SELECT gc.StudentID, gc.SubjectID, gc.Status "
              + "FROM Grade_Concerns gc "
              + "JOIN Subjects s "
              + "ON s.SubjectID = gc.SubjectID "
              + "JOIN Users t "
              + "ON t.UserID = gc.TeacherID "
              + "WHERE gc.ConcernID = ? "
              + "AND gc.TeacherID = ? "
              + "AND s.TeacherID = ? "
              + "AND t.Role = 'Teacher' "
              + "FOR UPDATE";

        String lockGrade =
                "SELECT GradeID, CO1, CO2, CO3, "
              + "FinalExam, OLA, Coursera, "
              + "FinalPercentage, NumericalGrade, "
              + "CalculationComplete, SubmissionStatus "
              + "FROM Grades "
              + "WHERE StudentID = ? AND SubjectID = ? "
              + "FOR UPDATE";

        String saveHistory =
                "INSERT INTO Grade_Revision_History ("
              + "GradeID, ConcernID, "
              + "OldCO1, OldCO2, OldCO3, "
              + "OldFinalExam, OldOLA, OldCoursera, "
              + "OldFinalPercentage, OldNumericalGrade, "
              + "NewCO1, NewCO2, NewCO3, "
              + "NewFinalExam, NewOLA, NewCoursera, "
              + "NewFinalPercentage, NewNumericalGrade"
              + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        String updateGrade =
                "UPDATE Grades SET "
              + "CO1=?, CO2=?, CO3=?, FinalExam=?, "
              + "Coursera=?, FinalPercentage=?, "
              + "NumericalGrade=?, SubmissionStatus='Revised' "
              + "WHERE GradeID=? "
              + "AND CalculationComplete=1 "
              + "AND SubmissionStatus IN ('Submitted','Revised')";

        String updateConcern =
                "UPDATE Grade_Concerns SET Status='Revised' "
              + "WHERE ConcernID=? AND Status='Under Review'";

        try (Connection connection =
                DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int studentId;
                int subjectId;

                // Lock and validate the concern.
                try (PreparedStatement statement =
                        connection.prepareStatement(lockConcern)) {

                    statement.setInt(1, concernId);
                    statement.setInt(2, teacherId);
                    statement.setInt(3, teacherId);

                    try (ResultSet result =
                            statement.executeQuery()) {

                        if (!result.next()) {
                        	throw new IllegalStateException(
                        	        "Concern not found or teacher is not authorized.");
                        }

                        if (!"Under Review".equals(
                                result.getString("Status"))) {
                            throw new IllegalStateException(
                                    "Concern must be Under Review.");
                        }

                        studentId = result.getInt("StudentID");
                        subjectId = result.getInt("SubjectID");
                    }
                }

                int gradeId;
                BigDecimal[] old = new BigDecimal[8];
                BigDecimal ola;

                // Lock the original submitted grade.
                try (PreparedStatement statement =
                        connection.prepareStatement(lockGrade)) {

                    statement.setInt(1, studentId);
                    statement.setInt(2, subjectId);

                    try (ResultSet result =
                            statement.executeQuery()) {

                        if (!result.next()) {
                            throw new IllegalStateException(
                                    "Grade record not found.");
                        }

                        String status =
                                result.getString("SubmissionStatus");

                        if (result.getInt(
                                "CalculationComplete") != 1
                                || !("Submitted".equals(status)
                                || "Revised".equals(status))) {

                            throw new IllegalStateException(
                                    "A released grade is required.");
                        }

                        gradeId = result.getInt("GradeID");

                        String[] columns = {
                                "CO1", "CO2", "CO3",
                                "FinalExam", "OLA", "Coursera",
                                "FinalPercentage", "NumericalGrade"
                        };

                        for (int i = 0; i < columns.length; i++) {
                            old[i] = result.getBigDecimal(columns[i]);
                        }

                        ola = old[4];
                    }
                }

                // Ordinary revisions retain the stored OLA.
                BigDecimal newFinal = calculateFinal(
                        co1, co2, co3, exam, ola, coursera);

                BigDecimal newNumerical =
                        numericalGrade(newFinal);

                // Preserve the original grade and
                // the proposed revision in the audit table.
                try (PreparedStatement statement =
                        connection.prepareStatement(saveHistory)) {

                    statement.setInt(1, gradeId);
                    statement.setInt(2, concernId);

                    for (int i = 0; i < old.length; i++) {
                        statement.setBigDecimal(i + 3, old[i]);
                    }

                    BigDecimal[] revised = {
                            co1, co2, co3, exam,
                            ola, coursera, newFinal, newNumerical
                    };

                    for (int i = 0; i < revised.length; i++) {
                        statement.setBigDecimal(i + 11, revised[i]);
                    }

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to save revision history.");
                    }
                }

                // Update the released grade.
                try (PreparedStatement statement =
                        connection.prepareStatement(updateGrade)) {

                    statement.setBigDecimal(1, co1);
                    statement.setBigDecimal(2, co2);
                    statement.setBigDecimal(3, co3);
                    statement.setBigDecimal(4, exam);
                    statement.setBigDecimal(5, coursera);
                    statement.setBigDecimal(6, newFinal);
                    statement.setBigDecimal(7, newNumerical);
                    statement.setInt(8, gradeId);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Grade update was rejected.");
                    }
                }

                // Complete the associated concern.
                try (PreparedStatement statement =
                        connection.prepareStatement(updateConcern)) {

                    statement.setInt(1, concernId);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Concern status update was rejected.");
                    }
                }

                connection.commit();

            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
