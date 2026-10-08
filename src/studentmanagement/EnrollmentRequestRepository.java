package studentmanagement;

import java.sql.*;
import java.util.*;

/** Database-backed enrollment requests; authorization is enforced on every write. */
public final class EnrollmentRequestRepository {
    private EnrollmentRequestRepository() {}
    public static final class Row {
        public final int id, studentId, subjectId;
        public final String student, subject, teacher, status;
        Row(ResultSet r) throws SQLException {
            id=r.getInt("RequestID"); studentId=r.getInt("StudentID"); subjectId=r.getInt("SubjectID");
            student=r.getString("StudentName"); subject=r.getString("SubjectName");
            teacher=r.getString("TeacherName"); status=r.getString("Status");
        }
        public String toString(){return subject+" | "+student+" | "+status;}
    }
    public static final class Subject {
        public final int id; public final String display;
        Subject(ResultSet r)throws SQLException {id=r.getInt("SubjectID"); display=r.getString("SubjectCode")+" — "+r.getString("SubjectName")+" ("+r.getString("TeacherName")+")";}
        public String toString(){return display;}
    }
    public static List<Subject> availableSubjects() throws SQLException {
        List<Subject> list=new ArrayList<>();
        String sql="SELECT s.SubjectID,s.SubjectCode,s.SubjectName,CONCAT(t.FirstName,' ',t.LastName) TeacherName FROM Subjects s JOIN Users t ON t.UserID=s.TeacherID AND t.Role='Teacher' ORDER BY s.SubjectCode";
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement q=c.prepareStatement(sql);ResultSet r=q.executeQuery()) {while(r.next())list.add(new Subject(r));}
        return list;
    }
    public static List<Row> studentRequests(int studentId)throws SQLException{return load("WHERE er.StudentID=?",studentId);}
    public static List<Row> teacherRequests(int teacherId)throws SQLException{return load("WHERE s.TeacherID=?",teacherId);}
    private static List<Row> load(String filter,int id)throws SQLException {
        List<Row> list=new ArrayList<>();
        String sql="SELECT er.RequestID,er.StudentID,er.SubjectID,er.Status,CONCAT(u.FirstName,' ',u.LastName,' (',u.StudentNumber,')') StudentName,CONCAT(s.SubjectCode,' — ',s.SubjectName) SubjectName,CONCAT(t.FirstName,' ',t.LastName) TeacherName FROM Enrollment_Requests er JOIN Users u ON u.UserID=er.StudentID JOIN Subjects s ON s.SubjectID=er.SubjectID JOIN Users t ON t.UserID=s.TeacherID "+filter+" ORDER BY er.RequestedAt DESC,er.RequestID DESC";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement q=c.prepareStatement(sql)){q.setInt(1,id);try(ResultSet r=q.executeQuery()){while(r.next())list.add(new Row(r));}}
        return list;
    }
    public static void request(int studentId,int subjectId)throws SQLException {
        try(Connection c=DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement q=c.prepareStatement("SELECT Role FROM Users WHERE UserID=? FOR UPDATE")){q.setInt(1,studentId);try(ResultSet r=q.executeQuery()){if(!r.next()||!"Student".equals(r.getString(1)))throw new SQLException("Invalid student account.");}}
                try(PreparedStatement q=c.prepareStatement("SELECT TeacherID FROM Subjects WHERE SubjectID=?")){q.setInt(1,subjectId);try(ResultSet r=q.executeQuery()){if(!r.next()||r.getObject(1)==null)throw new SQLException("This subject has no assigned teacher.");}}
                try(PreparedStatement q=c.prepareStatement("SELECT Status FROM Enrollments WHERE StudentID=? AND SubjectID=?")){q.setInt(1,studentId);q.setInt(2,subjectId);try(ResultSet r=q.executeQuery()){if(r.next()&&!"Withdrawn".equals(r.getString(1)))throw new SQLException("Already enrolled or completed in this subject.");}}
                String existing=null;
                try(PreparedStatement q=c.prepareStatement("SELECT Status FROM Enrollment_Requests WHERE StudentID=? AND SubjectID=? FOR UPDATE")){q.setInt(1,studentId);q.setInt(2,subjectId);try(ResultSet r=q.executeQuery()){if(r.next())existing=r.getString(1);}}
                if(existing!=null&&!"Rejected".equals(existing))throw new SQLException("A request for this subject already exists: "+existing);
                String sql=existing==null?"INSERT INTO Enrollment_Requests(StudentID,SubjectID,Status) VALUES (?,?,'Pending')":"UPDATE Enrollment_Requests SET Status='Pending',RequestedAt=NOW(),ReviewedAt=NULL,ReviewedBy=NULL,ReviewNote=NULL WHERE StudentID=? AND SubjectID=?";
                try(PreparedStatement q=c.prepareStatement(sql)){q.setInt(1,studentId);q.setInt(2,subjectId);q.executeUpdate();}
                c.commit();
            }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}
        }
    }
    public static void review(int teacherId,int requestId,boolean approve,String note)throws SQLException {
        try(Connection c=DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int studentId,subjectId;String status;
                String sql="SELECT er.StudentID,er.SubjectID,er.Status FROM Enrollment_Requests er JOIN Subjects s ON s.SubjectID=er.SubjectID JOIN Users t ON t.UserID=s.TeacherID AND t.Role='Teacher' WHERE er.RequestID=? AND s.TeacherID=? FOR UPDATE";
                try(PreparedStatement q=c.prepareStatement(sql)){q.setInt(1,requestId);q.setInt(2,teacherId);try(ResultSet r=q.executeQuery()){if(!r.next())throw new SQLException("Request not found or not assigned to this teacher.");studentId=r.getInt(1);subjectId=r.getInt(2);status=r.getString(3);}}
                if(!"Pending".equals(status))throw new SQLException("Only pending requests can be reviewed.");
                if(approve)upsertEnrollment(c,studentId,subjectId);
                try(PreparedStatement q=c.prepareStatement("UPDATE Enrollment_Requests SET Status=?,ReviewedAt=NOW(),ReviewedBy=?,ReviewNote=? WHERE RequestID=?")){q.setString(1,approve?"Approved":"Rejected");q.setInt(2,teacherId);q.setString(3,note);q.setInt(4,requestId);q.executeUpdate();}
                c.commit();
            }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}
        }
    }
    private static void upsertEnrollment(Connection c,int studentId,int subjectId)throws SQLException {
        String sql="INSERT INTO Enrollments(StudentID,SubjectID,EnrollmentDate,Status) VALUES(?,?,CURRENT_DATE(),'Enrolled') ON DUPLICATE KEY UPDATE Status=IF(Status='Withdrawn','Enrolled',Status)";
        try(PreparedStatement q=c.prepareStatement(sql)){q.setInt(1,studentId);q.setInt(2,subjectId);q.executeUpdate();}
    }
    public static void directEnroll(int teacherId,int studentId,int subjectId)throws SQLException {
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try {
                try(PreparedStatement q=c.prepareStatement("SELECT s.SubjectID FROM Subjects s JOIN Users t ON t.UserID=s.TeacherID AND t.Role='Teacher' WHERE s.SubjectID=? AND s.TeacherID=?")){q.setInt(1,subjectId);q.setInt(2,teacherId);try(ResultSet r=q.executeQuery()){if(!r.next())throw new SQLException("This class is not assigned to you.");}}
                try(PreparedStatement q=c.prepareStatement("SELECT UserID FROM Users WHERE UserID=? AND Role='Student'")){q.setInt(1,studentId);try(ResultSet r=q.executeQuery()){if(!r.next())throw new SQLException("Student account not found.");}}
                try(PreparedStatement q=c.prepareStatement("SELECT Status FROM Enrollments WHERE StudentID=? AND SubjectID=? FOR UPDATE")){q.setInt(1,studentId);q.setInt(2,subjectId);try(ResultSet r=q.executeQuery()){if(r.next()&&!"Withdrawn".equals(r.getString(1)))throw new SQLException("Student is already enrolled or completed.");}}
                upsertEnrollment(c,studentId,subjectId);
                try(PreparedStatement q=c.prepareStatement("INSERT INTO Enrollment_Requests(StudentID,SubjectID,Status,ReviewedAt,ReviewedBy,ReviewNote) VALUES (?,?,'Approved',NOW(),?,'Direct teacher enrollment') ON DUPLICATE KEY UPDATE Status='Approved',ReviewedAt=NOW(),ReviewedBy=VALUES(ReviewedBy),ReviewNote=VALUES(ReviewNote)")){q.setInt(1,studentId);q.setInt(2,subjectId);q.setInt(3,teacherId);q.executeUpdate();}
                c.commit();
            }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}
        }
    }
    public static Map<String,String> curriculumStatuses(int studentId)throws SQLException {
        Map<String,String> out=new HashMap<>();
        String sql="SELECT s.SubjectCode,e.Status FROM Enrollments e JOIN Subjects s ON s.SubjectID=e.SubjectID WHERE e.StudentID=?";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement q=c.prepareStatement(sql)){q.setInt(1,studentId);try(ResultSet r=q.executeQuery()){while(r.next())if("Enrolled".equals(r.getString(2)))out.put(r.getString(1),"In Current Load");}}
        String pending="SELECT s.SubjectCode FROM Enrollment_Requests er JOIN Subjects s ON s.SubjectID=er.SubjectID WHERE er.StudentID=? AND er.Status='Pending'";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement q=c.prepareStatement(pending)){q.setInt(1,studentId);try(ResultSet r=q.executeQuery()){while(r.next())out.putIfAbsent(r.getString(1),"Pending Enrollment");}}
        return out;
    }
}
