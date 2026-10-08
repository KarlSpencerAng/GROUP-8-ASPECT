package studentmanagement;

import java.sql.*;
import java.math.*;
import java.util.*;

/** Transactional, teacher-authorized direct revisions of already submitted grades. */
public final class DirectGradeRevisionRepository {
    private DirectGradeRevisionRepository() {}
    public static final class Entry {
        public final int gradeId;
        public final String student, subject;
        public final BigDecimal[] scores;
        public final String status;
        Entry(ResultSet r) throws SQLException {
            gradeId=r.getInt("GradeID"); student=r.getString("StudentNumber")+" - "+r.getString("FirstName")+" "+r.getString("LastName");
            subject=r.getString("SubjectCode")+" - "+r.getString("SubjectName");
            scores=new BigDecimal[6];
            String[] names={"CO1","CO2","CO3","FinalExam","OLA","Coursera"};
            for(int i=0;i<6;i++) scores[i]=r.getBigDecimal(names[i]);
            status=r.getString("SubmissionStatus");
        }
        public String toString(){return subject+" | "+student+" | "+status;}
    }
    public static List<Entry> list(int teacherId) throws SQLException {
        List<Entry> rows=new ArrayList<>();
        String sql="SELECT g.GradeID,u.StudentNumber,u.FirstName,u.LastName,s.SubjectCode,s.SubjectName, " +
            "g.CO1,g.CO2,g.CO3,g.FinalExam,g.OLA,g.Coursera,g.SubmissionStatus " +
            "FROM Grades g JOIN Subjects s ON s.SubjectID=g.SubjectID JOIN Users u ON u.UserID=g.StudentID " +
            "JOIN Users t ON t.UserID=s.TeacherID WHERE s.TeacherID=? AND t.Role='Teacher' " +
            "AND g.CalculationComplete=1 AND g.SubmissionStatus IN ('Submitted','Revised') " +
            "ORDER BY s.SubjectCode,u.LastName,u.FirstName";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement q=c.prepareStatement(sql)){
            q.setInt(1,teacherId); try(ResultSet r=q.executeQuery()){while(r.next())rows.add(new Entry(r));}
        }
        return rows;
    }
    private static BigDecimal score(String input) {
        BigDecimal v;
        try{v=new BigDecimal(input.trim()).setScale(2,RoundingMode.HALF_UP);}catch(Exception e){throw new IllegalArgumentException("Enter valid numeric scores.");}
        if(v.compareTo(BigDecimal.ZERO)<0||v.compareTo(new BigDecimal("100"))>0)throw new IllegalArgumentException("Scores must be 0 to 100.");
        return v;
    }
    public static void revise(int teacherId,int gradeId,String[] inputs,String reason) throws SQLException {
        if(teacherId<=0||gradeId<=0)throw new IllegalArgumentException("Invalid teacher or grade.");
        if(reason==null||reason.trim().length()<5||reason.trim().length()>1000)throw new IllegalArgumentException("Reason must contain 5–1000 characters.");
        if(inputs==null||inputs.length!=5)throw new IllegalArgumentException("Expected CO1, CO2, CO3, final exam and Coursera scores.");
        BigDecimal[] proposed=new BigDecimal[6];
        for(int i=0;i<4;i++)proposed[i]=score(inputs[i]);
        proposed[5]=score(inputs[4]);
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                String lock="SELECT g.CO1,g.CO2,g.CO3,g.FinalExam,g.OLA,g.Coursera,g.FinalPercentage,g.NumericalGrade " +
                    "FROM Grades g JOIN Subjects s ON s.SubjectID=g.SubjectID JOIN Users t ON t.UserID=s.TeacherID " +
                    "WHERE g.GradeID=? AND s.TeacherID=? AND t.Role='Teacher' " +
                    "AND g.CalculationComplete=1 AND g.SubmissionStatus IN ('Submitted','Revised') FOR UPDATE";
                BigDecimal[] old=new BigDecimal[8];
                try(PreparedStatement q=c.prepareStatement(lock)){
                    q.setInt(1,gradeId);q.setInt(2,teacherId);
                    try(ResultSet r=q.executeQuery()){
                        if(!r.next())throw new SQLException("Grade unavailable or you are not the assigned teacher.");
                        for(int i=0;i<8;i++)old[i]=r.getBigDecimal(i+1);
                    }
                }
                // OLA is assessment-derived and intentionally cannot be manually overridden here.
                proposed[4]=old[4];
                BigDecimal[] w={new BigDecimal("0.15"),new BigDecimal("0.15"),new BigDecimal("0.15"),new BigDecimal("0.40"),new BigDecimal("0.10"),new BigDecimal("0.05")};
                BigDecimal pct=BigDecimal.ZERO;
                for(int i=0;i<6;i++)pct=pct.add(proposed[i].multiply(w[i]));
                pct=pct.setScale(2,RoundingMode.HALF_UP);
                double v=pct.doubleValue();
                String numerical=v>=98?"1.00":v>=95?"1.25":v>=92?"1.50":v>=89?"1.75":v>=86?"2.00":v>=83?"2.25":v>=80?"2.50":v>=78?"2.75":v>=75?"3.00":"5.00";
                BigDecimal num=new BigDecimal(numerical);
                String hist="INSERT INTO Direct_Grade_Revision_History (GradeID,TeacherID,Reason,"+
                    "OldCO1,OldCO2,OldCO3,OldFinalExam,OldOLA,OldCoursera,OldFinalPercentage,OldNumericalGrade,"+
                    "NewCO1,NewCO2,NewCO3,NewFinalExam,NewOLA,NewCoursera,NewFinalPercentage,NewNumericalGrade) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try(PreparedStatement q=c.prepareStatement(hist)){
                    q.setInt(1,gradeId);q.setInt(2,teacherId);q.setString(3,reason.trim());
                    for(int i=0;i<8;i++)q.setBigDecimal(i+4,old[i]);
                    for(int i=0;i<6;i++)q.setBigDecimal(i+12,proposed[i]);
                    q.setBigDecimal(18,pct);q.setBigDecimal(19,num);
                    if(q.executeUpdate()!=1)throw new SQLException("Audit insert failed.");
                }
                String update="UPDATE Grades SET CO1=?,CO2=?,CO3=?,FinalExam=?,Coursera=?,FinalPercentage=?,NumericalGrade=?,SubmissionStatus='Revised' " +
                    "WHERE GradeID=? AND CalculationComplete=1 AND SubmissionStatus IN ('Submitted','Revised')";
                try(PreparedStatement q=c.prepareStatement(update)){
                    for(int i=0;i<4;i++)q.setBigDecimal(i+1,proposed[i]);
                    q.setBigDecimal(5,proposed[5]);q.setBigDecimal(6,pct);q.setBigDecimal(7,num);q.setInt(8,gradeId);
                    if(q.executeUpdate()!=1)throw new SQLException("Grade update failed; no audit committed.");
                }
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
            finally{c.setAutoCommit(true);}
        }
    }
}
