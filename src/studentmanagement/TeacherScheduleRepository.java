package studentmanagement;

import java.sql.*;
import java.time.LocalTime;
import java.util.*;

public final class TeacherScheduleRepository {
    private TeacherScheduleRepository() {}
    public static final class Entry {
        public final int id;public final String day,start,end,room;
        Entry(int id,String day,String start,String end,String room){this.id=id;this.day=day;this.start=start;this.end=end;this.room=room;}
    }
    public static List<Entry> list(int teacherId,int subjectId) throws SQLException {
        List<Entry> result=new ArrayList<>();
        try(Connection c=DatabaseConnection.getConnection()){
            TeacherClassReportRepository.requireAssignment(c,teacherId,subjectId);
            try(PreparedStatement p=c.prepareStatement("SELECT ScheduleID,DayOfWeek,StartTime,EndTime,Room FROM Teacher_Class_Schedules WHERE TeacherID=? AND SubjectID=? ORDER BY FIELD(DayOfWeek,'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'),StartTime")){
                p.setInt(1,teacherId);p.setInt(2,subjectId);
                try(ResultSet r=p.executeQuery()){while(r.next())result.add(new Entry(r.getInt(1),r.getString(2),r.getTime(3).toLocalTime().toString(),r.getTime(4).toLocalTime().toString(),r.getString(5)));}
            }
        }
        return result;
    }
    public static void add(int teacherId,int subjectId,String day,String start,String end,String room) throws SQLException {
        if(!Arrays.asList("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday").contains(day))throw new IllegalArgumentException("Choose a valid day.");
        LocalTime from=LocalTime.parse(start.trim()),to=LocalTime.parse(end.trim());
        if(!to.isAfter(from))throw new IllegalArgumentException("End time must be later than start time.");
        if(room==null)room="";
        if(room.length()>100)throw new IllegalArgumentException("Room must be 100 characters or fewer.");
        try(Connection c=DatabaseConnection.getConnection()){
            TeacherClassReportRepository.requireAssignment(c,teacherId,subjectId);
            try(PreparedStatement p=c.prepareStatement("INSERT INTO Teacher_Class_Schedules(TeacherID,SubjectID,DayOfWeek,StartTime,EndTime,Room) VALUES(?,?,?,?,?,?)")){
                p.setInt(1,teacherId);p.setInt(2,subjectId);p.setString(3,day);p.setTime(4,Time.valueOf(from));p.setTime(5,Time.valueOf(to));p.setString(6,room.trim());p.executeUpdate();
            }
        }
    }
    public static void delete(int teacherId,int subjectId,int scheduleId) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection()){
            TeacherClassReportRepository.requireAssignment(c,teacherId,subjectId);
            try(PreparedStatement p=c.prepareStatement("DELETE FROM Teacher_Class_Schedules WHERE ScheduleID=? AND TeacherID=? AND SubjectID=?")){
                p.setInt(1,scheduleId);p.setInt(2,teacherId);p.setInt(3,subjectId);
                if(p.executeUpdate()!=1)throw new SQLException("Schedule not found or not owned by this teacher.");
            }
        }
    }
}
