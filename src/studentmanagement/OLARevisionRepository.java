/**
 * OLARevisionRepository.java
 * Purpose: OLA revision transactions and audit.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;

/** Atomically persists an authorized, reviewed OLA correction. */
public final class OLARevisionRepository {
    private OLARevisionRepository() {}

    private static BigDecimal decimal(double d) {
        return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal numerical(BigDecimal percent) {
        double v = percent.doubleValue();
        return decimal(v >= 98 ? 1 : v >= 95 ? 1.25 : v >= 92 ? 1.5
                : v >= 89 ? 1.75 : v >= 86 ? 2 : v >= 83 ? 2.25
                : v >= 80 ? 2.5 : v >= 78 ? 2.75 : v >= 75 ? 3 : 5);
    }

    public static void submitRevision(int teacherId, int concernId,
                                      Map<Integer, Double> proposed) throws SQLException {
        if (teacherId <= 0 || concernId <= 0 || proposed == null || proposed.isEmpty())
            throw new IllegalArgumentException("Teacher, reviewed concern and changed scores are required.");
        for (Map.Entry<Integer, Double> entry : proposed.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null
                    || !Double.isFinite(entry.getValue()) || entry.getValue() < 0)
                throw new IllegalArgumentException("Invalid revised activity score.");
        }
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int studentId, subjectId;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT gc.StudentID, gc.SubjectID, gc.Status FROM Grade_Concerns gc "
                        + "JOIN Subjects s ON s.SubjectID=gc.SubjectID "
                        + "JOIN Users t ON t.UserID=gc.TeacherID "
                        + "WHERE gc.ConcernID=? AND gc.TeacherID=? AND s.TeacherID=? "
                        + "AND t.Role='Teacher' FOR UPDATE")) {
                    ps.setInt(1, concernId); ps.setInt(2, teacherId); ps.setInt(3, teacherId);
                    try (ResultSet r = ps.executeQuery()) {
                        if (!r.next()) throw new IllegalStateException("Teacher is not authorized for this concern.");
                        if (!"Under Review".equals(r.getString("Status")))
                            throw new IllegalStateException("Concern must be Under Review.");
                        studentId = r.getInt("StudentID"); subjectId = r.getInt("SubjectID");
                    }
                }
                int gradeId;
                BigDecimal co1, co2, co3, exam, coursera;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT * FROM Grades WHERE StudentID=? AND SubjectID=? FOR UPDATE")) {
                    ps.setInt(1, studentId); ps.setInt(2, subjectId);
                    try (ResultSet r = ps.executeQuery()) {
                        if (!r.next() || r.getInt("CalculationComplete") != 1
                                || !("Submitted".equals(r.getString("SubmissionStatus"))
                                || "Revised".equals(r.getString("SubmissionStatus"))))
                            throw new IllegalStateException("A submitted, calculated grade is required.");
                        gradeId = r.getInt("GradeID");
                        co1 = r.getBigDecimal("CO1"); co2 = r.getBigDecimal("CO2");
                        co3 = r.getBigDecimal("CO3"); exam = r.getBigDecimal("FinalExam");
                        coursera = r.getBigDecimal("Coursera");
                    }
                }
                // Lock every activity score for this student, not only the changed ones.
                BigDecimal ola = BigDecimal.ZERO;
                BigDecimal weightTotal = BigDecimal.ZERO;
                Set<Integer> seen = new HashSet<>();
                List<BigDecimal[]> changes = new ArrayList<>();
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT a.AssessmentID, a.MaximumScore, a.Weight, os.Score "
                        + "FROM OLA_Assessments a LEFT JOIN OLA_Scores os "
                        + "ON os.AssessmentID=a.AssessmentID AND os.StudentID=? "
                        + "WHERE a.SubjectID=? ORDER BY a.AssessmentID FOR UPDATE")) {
                    ps.setInt(1, studentId); ps.setInt(2, subjectId);
                    try (ResultSet r = ps.executeQuery()) {
                        while (r.next()) {
                            int id = r.getInt("AssessmentID");
                            BigDecimal max = r.getBigDecimal("MaximumScore");
                            BigDecimal weight = r.getBigDecimal("Weight");
                            BigDecimal old = r.getBigDecimal("Score");
                            if (old == null) throw new IllegalStateException("Missing original OLA score for assessment " + id);
                            Double input = proposed.get(id);
                            BigDecimal score = input == null ? old : decimal(input);
                            if (score.signum() < 0 || score.compareTo(max) > 0)
                                throw new IllegalArgumentException("Score exceeds activity maximum for assessment " + id);
                            if (input != null && score.compareTo(old) != 0)
                                changes.add(new BigDecimal[]{BigDecimal.valueOf(id), old, score});
                            seen.add(id);
                            weightTotal = weightTotal.add(weight);
                            ola = ola.add(score.multiply(weight)
                                    .divide(max, 10, RoundingMode.HALF_UP));
                        }
                    }
                }
                if (!seen.containsAll(proposed.keySet()))
                    throw new IllegalArgumentException("An assessment does not belong to this subject.");
                if (weightTotal.compareTo(new BigDecimal("100")) != 0)
                    throw new IllegalStateException("OLA assessment weights must total 100%.");
                if (changes.isEmpty()) throw new IllegalArgumentException("Change at least one activity score.");
                ola = ola.setScale(2, RoundingMode.HALF_UP);
                BigDecimal percent = co1.multiply(new BigDecimal(".15"))
                        .add(co2.multiply(new BigDecimal(".15")))
                        .add(co3.multiply(new BigDecimal(".15")))
                        .add(exam.multiply(new BigDecimal(".40")))
                        .add(ola.multiply(new BigDecimal(".10")))
                        .add(coursera.multiply(new BigDecimal(".05")))
                        .setScale(2, RoundingMode.HALF_UP);
                try (PreparedStatement scorePs = c.prepareStatement(
                            "UPDATE OLA_Scores SET Score=? WHERE AssessmentID=? AND StudentID=?");
                     PreparedStatement auditPs = c.prepareStatement(
                            "INSERT INTO OLA_Revision_History "
                            + "(ConcernID,AssessmentID,StudentID,TeacherID,OldScore,NewScore) "
                            + "VALUES (?,?,?,?,?,?)")) {
                    for (BigDecimal[] change : changes) {
                        int id = change[0].intValueExact();
                        scorePs.setBigDecimal(1, change[2]); scorePs.setInt(2, id);
                        scorePs.setInt(3, studentId);
                        if (scorePs.executeUpdate() != 1) throw new SQLException("Score update failed.");
                        auditPs.setInt(1, concernId); auditPs.setInt(2, id);
                        auditPs.setInt(3, studentId); auditPs.setInt(4, teacherId);
                        auditPs.setBigDecimal(5, change[1]); auditPs.setBigDecimal(6, change[2]);
                        if (auditPs.executeUpdate() != 1) throw new SQLException("Audit insertion failed.");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE Grades SET OLA=?,FinalPercentage=?,NumericalGrade=?, "
                        + "SubmissionStatus='Revised' WHERE GradeID=?")) {
                    ps.setBigDecimal(1, ola); ps.setBigDecimal(2, percent);
                    ps.setBigDecimal(3, numerical(percent)); ps.setInt(4, gradeId);
                    if (ps.executeUpdate() != 1) throw new SQLException("Grade update failed.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE Grade_Concerns SET Status='Revised' "
                        + "WHERE ConcernID=? AND Status='Under Review'")) {
                    ps.setInt(1, concernId);
                    if (ps.executeUpdate() != 1) throw new SQLException("Concern update failed.");
                }
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }
}
