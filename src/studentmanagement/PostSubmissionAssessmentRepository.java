package studentmanagement;

import java.math.*;
import java.sql.*;
import java.util.*;

/** Teacher-authorized post-submission assessment creation and selective recalculation. */
public final class PostSubmissionAssessmentRepository {
    private PostSubmissionAssessmentRepository() {}
    public static final class Subject {
        public final int id; public final String code, name;
        Subject(ResultSet r) throws SQLException {id=r.getInt(1);code=r.getString(2);name=r.getString(3);}
        public String toString(){return code+" - "+name;}
    }
    public static final class Activity {
        public final int id; public final String name; public final BigDecimal max,weight;
        Activity(ResultSet r)throws SQLException{id=r.getInt(1);name=r.getString(2);max=r.getBigDecimal(3);weight=r.getBigDecimal(4);}
    }
    public static final class StudentRow {
        public final int id,gradeId; public final String number,name,status;
        StudentRow(ResultSet r)throws SQLException{id=r.getInt(1);gradeId=r.getInt(2);number=r.getString(3);name=r.getString(4);status=r.getString(5);}
        public String toString(){return number+" - "+name+" ["+status+"]";}
    }
    public static List<Subject> subjects(int teacher)throws SQLException{
        List<Subject> rows=new ArrayList<>();
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement q=c.prepareStatement("SELECT s.SubjectID,s.SubjectCode,s.SubjectName FROM Subjects s JOIN Users u ON u.UserID=s.TeacherID WHERE s.TeacherID=? AND u.Role='Teacher' ORDER BY s.SubjectCode")){
            q.setInt(1,teacher);try(ResultSet r=q.executeQuery()){while(r.next())rows.add(new Subject(r));}
        }return rows;
    }
    public static List<Activity> activities(int teacher,int subject)throws SQLException{
        List<Activity> rows=new ArrayList<>();
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement q=c.prepareStatement("SELECT a.AssessmentID,a.AssessmentName,a.MaximumScore,a.Weight FROM OLA_Assessments a JOIN Subjects s ON s.SubjectID=a.SubjectID JOIN Users u ON u.UserID=s.TeacherID WHERE s.TeacherID=? AND u.Role='Teacher' AND s.SubjectID=? ORDER BY a.AssessmentID")){
            q.setInt(1,teacher);q.setInt(2,subject);try(ResultSet r=q.executeQuery()){while(r.next())rows.add(new Activity(r));}
        }return rows;
    }
    public static List<StudentRow> students(int teacher,int subject)throws SQLException{
        List<StudentRow> rows=new ArrayList<>();String sql="SELECT u.UserID,g.GradeID,u.StudentNumber,CONCAT(u.FirstName,' ',u.LastName),g.SubmissionStatus FROM Grades g JOIN Users u ON u.UserID=g.StudentID JOIN Subjects s ON s.SubjectID=g.SubjectID JOIN Users t ON t.UserID=s.TeacherID WHERE s.SubjectID=? AND s.TeacherID=? AND t.Role='Teacher' AND g.CalculationComplete=1 AND g.SubmissionStatus IN ('Submitted','Revised') ORDER BY u.LastName,u.FirstName";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement q=c.prepareStatement(sql)){
            q.setInt(1,subject);q.setInt(2,teacher);try(ResultSet r=q.executeQuery()){while(r.next())rows.add(new StudentRow(r));}
        }return rows;
    }
    private static BigDecimal valid(String raw,BigDecimal min,BigDecimal max,String label){
        try{BigDecimal n=new BigDecimal(raw.trim()).setScale(2,RoundingMode.HALF_UP);if(n.compareTo(min)<0||n.compareTo(max)>0)throw new IllegalArgumentException(label+" must be between "+min+" and "+max);return n;}
        catch(NumberFormatException|NullPointerException e){throw new IllegalArgumentException("Invalid "+label);}
    }
    private static BigDecimal numerical(BigDecimal pct){double v=pct.doubleValue();return new BigDecimal(v>=98?"1.00":v>=95?"1.25":v>=92?"1.50":v>=89?"1.75":v>=86?"2.00":v>=83?"2.25":v>=80?"2.50":v>=78?"2.75":v>=75?"3.00":"5.00");}
    /** Creates the activity, updates explicitly supplied weights, stores selected scores, and recalculates only selected submitted grades in one transaction. */
    public static void apply(int teacher,int subject,String name,String maxText,String newWeightText,Map<Integer,String> existingWeightTexts,Map<Integer,String> selectedScores,String reason)throws SQLException{
        if(name==null||name.trim().isEmpty()||name.trim().length()>100)throw new IllegalArgumentException("Assessment name must contain 1–100 characters.");
        if(reason==null||reason.trim().length()<5||reason.trim().length()>1000)throw new IllegalArgumentException("Reason must contain 5–1000 characters.");
        if(selectedScores==null||selectedScores.isEmpty())throw new IllegalArgumentException("Select at least one student to recalculate.");
        BigDecimal maximum=valid(maxText,new BigDecimal("0.01"),new BigDecimal("9999.99"),"Maximum score");
        BigDecimal newWeight=valid(newWeightText,BigDecimal.ZERO,new BigDecimal("100"),"New weight");
        if(newWeight.signum()==0)throw new IllegalArgumentException("New assessment weight must be greater than zero.");
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                try(PreparedStatement q=c.prepareStatement("SELECT s.SubjectID FROM Subjects s JOIN Users t ON t.UserID=s.TeacherID WHERE s.SubjectID=? AND s.TeacherID=? AND t.Role='Teacher' FOR UPDATE")){
                    q.setInt(1,subject);q.setInt(2,teacher);try(ResultSet r=q.executeQuery()){if(!r.next())throw new SQLException("You are not the assigned teacher.");}
                }
                List<Activity> existing=new ArrayList<>();
                try(PreparedStatement q=c.prepareStatement("SELECT AssessmentID,AssessmentName,MaximumScore,Weight FROM OLA_Assessments WHERE SubjectID=? ORDER BY AssessmentID FOR UPDATE")){
                    q.setInt(1,subject);try(ResultSet r=q.executeQuery()){while(r.next())existing.add(new Activity(r));}
                }
                if(existingWeightTexts==null||existingWeightTexts.size()!=existing.size())throw new SQLException("Assessment list changed; refresh and try again.");
                Map<Integer,BigDecimal> weights=new LinkedHashMap<>();BigDecimal sum=newWeight;
                for(Activity a:existing){String raw=existingWeightTexts.get(a.id);if(raw==null)throw new SQLException("Missing weight for "+a.name);BigDecimal w=valid(raw,BigDecimal.ZERO,new BigDecimal("100"),"Weight for "+a.name);weights.put(a.id,w);sum=sum.add(w);}
                if(sum.compareTo(new BigDecimal("100.00"))!=0)throw new IllegalArgumentException("All assessment weights, including the new one, must total exactly 100%. Current total: "+sum);
                Map<Integer,StudentRow> allowed=new LinkedHashMap<>();
                String rows="SELECT u.UserID,g.GradeID,u.StudentNumber,CONCAT(u.FirstName,' ',u.LastName),g.SubmissionStatus FROM Grades g JOIN Users u ON u.UserID=g.StudentID WHERE g.SubjectID=? AND g.CalculationComplete=1 AND g.SubmissionStatus IN ('Submitted','Revised') FOR UPDATE";
                try(PreparedStatement q=c.prepareStatement(rows)){q.setInt(1,subject);try(ResultSet r=q.executeQuery()){while(r.next()){StudentRow s=new StudentRow(r);allowed.put(s.id,s);}}}
                Map<Integer,BigDecimal> scores=new LinkedHashMap<>();
                for(Map.Entry<Integer,String> entry:selectedScores.entrySet()){
                    if(!allowed.containsKey(entry.getKey()))throw new SQLException("Selected student has no submitted grade in this subject.");
                    scores.put(entry.getKey(),valid(entry.getValue(),BigDecimal.ZERO,maximum,"Score for student "+entry.getKey()));
                }
                // All changes are atomic; a failed calculation rolls back assessment, scores and weights.
                for(Activity a:existing){
                    try(PreparedStatement q=c.prepareStatement("UPDATE OLA_Assessments SET Weight=? WHERE AssessmentID=? AND SubjectID=?")){q.setBigDecimal(1,weights.get(a.id));q.setInt(2,a.id);q.setInt(3,subject);if(q.executeUpdate()!=1)throw new SQLException("Assessment update failed.");}
                }
                int newId;
                try(PreparedStatement q=c.prepareStatement("INSERT INTO OLA_Assessments(SubjectID,AssessmentName,MaximumScore,Weight) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                    q.setInt(1,subject);q.setString(2,name.trim());q.setBigDecimal(3,maximum);q.setBigDecimal(4,newWeight);q.executeUpdate();try(ResultSet r=q.getGeneratedKeys()){if(!r.next())throw new SQLException("Could not create assessment.");newId=r.getInt(1);}
                }
                for(Map.Entry<Integer,BigDecimal> entry:scores.entrySet()){
                    try(PreparedStatement q=c.prepareStatement("INSERT INTO OLA_Scores(AssessmentID,StudentID,Score) VALUES(?,?,?)")){
                        q.setInt(1,newId);q.setInt(2,entry.getKey());q.setBigDecimal(3,entry.getValue());q.executeUpdate();
                    }
                }
                String calc="SELECT a.AssessmentID,a.AssessmentName,a.MaximumScore,a.Weight,os.Score FROM OLA_Assessments a LEFT JOIN OLA_Scores os ON os.AssessmentID=a.AssessmentID AND os.StudentID=? WHERE a.SubjectID=? ORDER BY a.AssessmentID";
                String grade="SELECT CO1,CO2,CO3,FinalExam,OLA,Coursera,FinalPercentage,NumericalGrade FROM Grades WHERE GradeID=? FOR UPDATE";
                String audit="INSERT INTO Post_Submission_Assessment_Revisions(AssessmentID,GradeID,TeacherID,Reason,OldOLA,NewOLA,OldFinalPercentage,NewFinalPercentage,OldNumericalGrade,NewNumericalGrade) VALUES(?,?,?,?,?,?,?,?,?,?)";
                for(int studentId:scores.keySet()){
                    BigDecimal ola=BigDecimal.ZERO;
                    try(PreparedStatement q=c.prepareStatement(calc)){
                        q.setInt(1,studentId);q.setInt(2,subject);try(ResultSet r=q.executeQuery()){
                            while(r.next()){
                                BigDecimal score=r.getBigDecimal("Score");if(score==null)throw new SQLException("Student "+allowed.get(studentId).number+" is missing a score for "+r.getString("AssessmentName")+". No changes saved.");
                                BigDecimal max=r.getBigDecimal("MaximumScore");if(score.compareTo(max)>0)throw new SQLException("Score exceeds maximum for "+r.getString("AssessmentName"));
                                ola=ola.add(score.multiply(r.getBigDecimal("Weight")).divide(max,8,RoundingMode.HALF_UP));
                            }
                        }
                    }
                    ola=ola.setScale(2,RoundingMode.HALF_UP);
                    StudentRow s=allowed.get(studentId);BigDecimal[] old=new BigDecimal[8];
                    try(PreparedStatement q=c.prepareStatement(grade)){q.setInt(1,s.gradeId);try(ResultSet r=q.executeQuery()){if(!r.next())throw new SQLException("Grade disappeared.");for(int i=0;i<8;i++)old[i]=r.getBigDecimal(i+1);}}
                    BigDecimal pct=old[0].multiply(new BigDecimal("0.15")).add(old[1].multiply(new BigDecimal("0.15"))).add(old[2].multiply(new BigDecimal("0.15"))).add(old[3].multiply(new BigDecimal("0.40"))).add(ola.multiply(new BigDecimal("0.10"))).add(old[5].multiply(new BigDecimal("0.05"))).setScale(2,RoundingMode.HALF_UP);
                    BigDecimal num=numerical(pct);
                    try(PreparedStatement q=c.prepareStatement(audit)){
                        q.setInt(1,newId);q.setInt(2,s.gradeId);q.setInt(3,teacher);q.setString(4,reason.trim());q.setBigDecimal(5,old[4]);q.setBigDecimal(6,ola);q.setBigDecimal(7,old[6]);q.setBigDecimal(8,pct);q.setBigDecimal(9,old[7]);q.setBigDecimal(10,num);if(q.executeUpdate()!=1)throw new SQLException("Audit save failed.");
                    }
                    try(PreparedStatement q=c.prepareStatement("UPDATE Grades SET OLA=?,FinalPercentage=?,NumericalGrade=?,SubmissionStatus='Revised' WHERE GradeID=? AND SubmissionStatus IN ('Submitted','Revised')")){
                        q.setBigDecimal(1,ola);q.setBigDecimal(2,pct);q.setBigDecimal(3,num);q.setInt(4,s.gradeId);if(q.executeUpdate()!=1)throw new SQLException("Grade update failed.");
                    }
                }
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
            finally{c.setAutoCommit(true);}
        }
    }
}
