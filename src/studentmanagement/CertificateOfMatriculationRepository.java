package studentmanagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Read-only database access for the student's Certificate of Matriculation. */
public final class CertificateOfMatriculationRepository {
    private CertificateOfMatriculationRepository() { }

    public static final class StudentInfo {
        public final String studentNumber, fullName, section, email;
        StudentInfo(String studentNumber, String fullName, String section, String email) {
            this.studentNumber = studentNumber;
            this.fullName = fullName;
            this.section = section;
            this.email = email;
        }
    }

    public static final class ScheduleRow {
        public final String subjectCode, subjectName, section, day, start, end, room;
        public final int units;
        ScheduleRow(String subjectCode, String subjectName, String section, int units,
                    String day, String start, String end, String room) {
            this.subjectCode = subjectCode;
            this.subjectName = subjectName;
            this.section = section;
            this.units = units;
            this.day = day;
            this.start = start;
            this.end = end;
            this.room = room;
        }
    }

    public static StudentInfo loadStudent(String studentNumber) throws SQLException {
        String sql = "SELECT StudentNumber,FirstName,LastName,Section,Email FROM Users " +
                     "WHERE StudentNumber=? AND Role='Student'";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, studentNumber);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Student account was not found.");
                return new StudentInfo(
                    r.getString("StudentNumber"),
                    (r.getString("FirstName") + " " + r.getString("LastName")).trim(),
                    r.getString("Section"),
                    r.getString("Email")
                );
            }
        }
    }

    public static List<ScheduleRow> loadOfficialEnrollment(String studentNumber) throws SQLException {
        List<ScheduleRow> rows = new ArrayList<>();
        String sql =
            "SELECT s.SubjectCode,s.SubjectName,s.Units,u.Section," +
            "tcs.DayOfWeek,tcs.StartTime,tcs.EndTime,tcs.Room " +
            "FROM Enrollments e " +
            "JOIN Users u ON u.UserID=e.StudentID AND u.Role='Student' " +
            "JOIN Subjects s ON s.SubjectID=e.SubjectID " +
            "LEFT JOIN Teacher_Class_Schedules tcs ON tcs.SubjectID=s.SubjectID " +
            "WHERE u.StudentNumber=? AND e.Status='Enrolled' " +
            "ORDER BY s.SubjectCode," +
            "FIELD(tcs.DayOfWeek,'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday')," +
            "tcs.StartTime";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, studentNumber);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Time from = r.getTime("StartTime");
                    Time to = r.getTime("EndTime");
                    rows.add(new ScheduleRow(
                        r.getString("SubjectCode"), r.getString("SubjectName"),
                        safe(r.getString("Section")), r.getInt("Units"),
                        safe(r.getString("DayOfWeek")),
                        from == null ? "" : from.toLocalTime().toString(),
                        to == null ? "" : to.toLocalTime().toString(),
                        safe(r.getString("Room"))
                    ));
                }
            }
        }
        return rows;
    }

    private static String safe(String value) { return value == null ? "" : value; }
}
