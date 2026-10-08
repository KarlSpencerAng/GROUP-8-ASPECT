/**
 * HistoricalRecordsRepository.java
 * Purpose: Historical academic records database operations.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

import java.sql.*;
import java.util.*;

/** Database-backed staff historical records. Uses the configured database. */
public final class HistoricalRecordsRepository {
    private HistoricalRecordsRepository() {}
    private static Connection connect() throws SQLException {
        Connection c = DatabaseConnection.getConnection();
        if (!"GradingSystem_Test".equalsIgnoreCase(c.getCatalog())) {
            c.close();
            throw new SQLException("Historical-record integration is restricted to GradingSystem_Test during testing.");
        }
        return c;
    }
    public static List<AcademicData.HistoricalRecord> loadAll() throws SQLException {
        String sql = "SELECT u.StudentNumber, cc.SubjectCode, h.Status, h.Note "
            + "FROM Historical_Records h JOIN Users u ON u.UserID=h.StudentID "
            + "JOIN Curriculum_Courses cc ON cc.CurriculumCourseID=h.CurriculumCourseID "
            + "WHERE u.Role='Student' ORDER BY u.StudentNumber,cc.SubjectCode";
        List<AcademicData.HistoricalRecord> result = new ArrayList<>();
        try (Connection c=connect(); PreparedStatement ps=c.prepareStatement(sql);
             ResultSet rs=ps.executeQuery()) {
            while(rs.next()) result.add(new AcademicData.HistoricalRecord(
                rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)));
        }
        return result;
    }
    public static void save(String studentNumber,String code,String status,String note) throws SQLException {
        if (!Arrays.asList("Taken","Incomplete","Exempted/Credited").contains(status))
            throw new IllegalArgumentException("Invalid historical status.");
        if(note==null || note.trim().isEmpty() || note.trim().length()>255)
            throw new IllegalArgumentException("Reference/reason must be 1 to 255 characters.");
        CurriculumPanel.curriculumUnits(code);
        try(Connection c=connect()) {
            c.setAutoCommit(false);
            try {
                int sid, cid;
                try(PreparedStatement ps=c.prepareStatement("SELECT UserID FROM Users WHERE StudentNumber=? AND Role='Student'")) {
                    ps.setString(1,studentNumber);
                    try(ResultSet rs=ps.executeQuery()) {
                        if(!rs.next()) throw new SQLException("Student not found.");
                        sid=rs.getInt(1);
                    }
                }
                try(PreparedStatement ps=c.prepareStatement("SELECT CurriculumCourseID FROM Curriculum_Courses WHERE SubjectCode=?")) {
                    ps.setString(1,code);
                    try(ResultSet rs=ps.executeQuery()) {
                        if(!rs.next()) throw new SQLException("Curriculum course not found in database: "+code);
                        cid=rs.getInt(1);
                    }
                }
                // A historical record must not override a current grade.
                try(PreparedStatement ps=c.prepareStatement("SELECT GradeID FROM Grades g JOIN Subjects s ON s.SubjectID=g.SubjectID WHERE g.StudentID=? AND s.SubjectCode=? LIMIT 1")) {
                    ps.setInt(1,sid);ps.setString(2,code);
                    try(ResultSet rs=ps.executeQuery()) {
                        if(rs.next()) throw new SQLException("A current grade exists for this course. Resolve it first.");
                    }
                }
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO Historical_Records (StudentID,CurriculumCourseID,Status,Note,IsDemo) VALUES (?,?,?,?,0) ON DUPLICATE KEY UPDATE Status=VALUES(Status),Note=VALUES(Note),IsDemo=0")) {
                    ps.setInt(1,sid);ps.setInt(2,cid);ps.setString(3,status);ps.setString(4,note.trim());ps.executeUpdate();
                }
                c.commit();
            } catch(SQLException|RuntimeException e) {c.rollback();throw e;}
        }
    }
    public static void remove(String studentNumber,String code) throws SQLException {
        String sql="DELETE h FROM Historical_Records h JOIN Users u ON u.UserID=h.StudentID JOIN Curriculum_Courses cc ON cc.CurriculumCourseID=h.CurriculumCourseID WHERE u.StudentNumber=? AND u.Role='Student' AND cc.SubjectCode=?";
        try(Connection c=connect();PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,studentNumber);ps.setString(2,code);
            if(ps.executeUpdate()!=1) throw new SQLException("Historical record not found.");
        }
    }
}
