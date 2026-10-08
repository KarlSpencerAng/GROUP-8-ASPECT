package studentmanagement;

import java.sql.*;
import java.util.*;

/** Read-only student grade reporting. Only Registrar-posted grades are official. */
public final class StudentAcademicGradesRepository {
    private StudentAcademicGradesRepository() {}
    public static final class Row {
        public String code, name, status, academicYear, term, section;
        public int units;
        public double co1, co2, co3, finalExam, ola, coursera, percent, universityGrade;
        public boolean complete;
        public boolean official() { return complete && "Posted".equals(status); }
        public boolean provisional() { return false; }
        public String period() { return academicYear + " / " + term; }
    }
    public static List<Row> load(String studentNumber) throws SQLException {
        String sql = "SELECT s.SubjectCode, s.SubjectName, s.Units, " +
            "s.CurriculumYear, s.Term, u.Section, g.CO1, g.CO2, g.CO3, g.FinalExam, g.OLA, g.Coursera, g.FinalPercentage, g.NumericalGrade, " +
            "g.CalculationComplete, g.SubmissionStatus " +
            "FROM Grades g JOIN Users u ON u.UserID=g.StudentID " +
            "JOIN Subjects s ON s.SubjectID=g.SubjectID " +
            "WHERE u.StudentNumber=? AND u.Role='Student' " +
            "AND g.CalculationComplete=1 AND g.SubmissionStatus='Posted' " +
            "ORDER BY s.CurriculumYear, s.Term, s.SubjectCode";
        List<Row> rows = new ArrayList<>();
        try (Connection c=DatabaseConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1, studentNumber);
            try (ResultSet rs=ps.executeQuery()) {
                while (rs.next()) {
                    Row r=new Row();
                    r.code=rs.getString("SubjectCode"); r.name=rs.getString("SubjectName");
                    r.units=rs.getInt("Units");
                    // The schema has curriculum year/term, NOT actual academic year/term.
                    Object cy=rs.getObject("CurriculumYear"), term=rs.getObject("Term");
                    r.academicYear="Curriculum " + (cy==null ? "Unspecified" : "Year " + cy);
                    r.term=term==null ? "Term unspecified" : "Term " + term;
                    r.section=rs.getString("Section");
                    r.co1=rs.getDouble("CO1"); r.co2=rs.getDouble("CO2"); r.co3=rs.getDouble("CO3");
                    r.finalExam=rs.getDouble("FinalExam"); r.ola=rs.getDouble("OLA"); r.coursera=rs.getDouble("Coursera");
                    r.percent=rs.getDouble("FinalPercentage");
                    r.universityGrade=rs.getDouble("NumericalGrade");
                    r.complete=rs.getBoolean("CalculationComplete");
                    r.status=rs.getString("SubmissionStatus"); rows.add(r);
                }
            }
        }
        return rows;
    }
}
