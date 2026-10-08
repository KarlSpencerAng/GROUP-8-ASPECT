package studentmanagement;

import java.sql.*;
import java.util.*;

/** Read-only reports; all queries restrict data to the authenticated teacher's assigned subjects. */
public final class TeacherClassReportRepository {
    private TeacherClassReportRepository() {}
    public static final class Subject {
        public final int id; public final String name;
        Subject(int id,String name){this.id=id;this.name=name;}
        @Override public String toString(){return name;}
    }
    public static final class StudentRow {
        public final String number,name,status;
        public final Double percentage,numerical;
        StudentRow(String number,String name,Double percentage,Double numerical,String status){
            this.number=number;this.name=name;this.percentage=percentage;this.numerical=numerical;this.status=status;
        }
    }
    public static List<Subject> subjects(int teacherId) throws SQLException {
        List<Subject> result=new ArrayList<>();
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(
            "SELECT SubjectID,SubjectCode,SubjectName FROM Subjects WHERE TeacherID=? ORDER BY SubjectCode")){
            p.setInt(1,teacherId);
            try(ResultSet r=p.executeQuery()){while(r.next())result.add(new Subject(r.getInt(1),r.getString(2)+" - "+r.getString(3)));}
        }
        return result;
    }
    public static void requireAssignment(Connection c,int teacherId,int subjectId) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM Subjects s JOIN Users u ON u.UserID=s.TeacherID WHERE s.SubjectID=? AND s.TeacherID=? AND u.Role='Teacher'")){
            p.setInt(1,subjectId);p.setInt(2,teacherId);
            try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("This subject is not assigned to the logged-in teacher.");}
        }
    }
    public static List<StudentRow> roster(int teacherId,int subjectId) throws SQLException {
        List<StudentRow> rows=new ArrayList<>();
        try(Connection c=DatabaseConnection.getConnection()){
            requireAssignment(c,teacherId,subjectId);
            String sql="SELECT u.StudentNumber,CONCAT(u.LastName,', ',u.FirstName),g.FinalPercentage,g.NumericalGrade,g.SubmissionStatus " +
                "FROM Enrollments e JOIN Users u ON u.UserID=e.StudentID AND u.Role='Student' " +
                "LEFT JOIN Grades g ON g.StudentID=e.StudentID AND g.SubjectID=e.SubjectID " +
                "WHERE e.SubjectID=? AND e.Status='Enrolled' ORDER BY u.LastName,u.FirstName";
            try(PreparedStatement p=c.prepareStatement(sql)){
                p.setInt(1,subjectId);
                try(ResultSet r=p.executeQuery()){
                    while(r.next()){
                        Double pct=r.getObject(3)==null?null:r.getDouble(3);
                        Double numerical=r.getObject(4)==null?null:r.getDouble(4);
                        rows.add(new StudentRow(r.getString(1),r.getString(2),pct,numerical,r.getString(5)==null?"Not graded":r.getString(5)));
                    }
                }
            }
        }
        return rows;
    }
}
