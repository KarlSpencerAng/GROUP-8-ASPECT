/**
 * AcademicRepository.java
 * Purpose: Academic database operations.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public final class AcademicRepository {

    private AcademicRepository() {
    }

    // Retrieve student profiles from MySQL.
    public static List<Student> loadStudents()
            throws SQLException {

        List<Student> students = new ArrayList<>();

        String sql =
            "SELECT UserID, StudentNumber, " +
            "FirstName, LastName, Section " +
            "FROM Users " +
            "WHERE Role = 'Student' " +
            "ORDER BY StudentNumber";

        try (
            Connection connection =
                DatabaseConnection.getConnection();

            PreparedStatement statement =
                connection.prepareStatement(sql);

            ResultSet result =
                statement.executeQuery()
        ) {

            while (result.next()) {

                String fullName =
                    result.getString("FirstName") +
                    " " +
                    result.getString("LastName");

                Student student = new Student(
                    result.getInt("UserID"),
                    result.getString("StudentNumber"),
                    fullName,
                    result.getString("Section")
                );

                students.add(student);
            }
        }

        return students;
    }

    // Retrieve the seven subjects used by
    // the current Java demonstration.
    public static List<Course> loadCurrentCourses()
            throws SQLException {

        List<Course> courses = new ArrayList<>();

        String sql =
            "SELECT SubjectID, SubjectCode, SubjectName FROM Subjects ORDER BY SubjectCode";

        try (
            Connection connection =
                DatabaseConnection.getConnection();

            PreparedStatement statement =
                connection.prepareStatement(sql);

            ResultSet result =
                statement.executeQuery()
        ) {

            while (result.next()) {

                Course course = new Course(
                    result.getInt("SubjectID"),
                    result.getString("SubjectCode"),
                    result.getString("SubjectName")
                );

                courses.add(course);
            }
        }

        return courses;
    }

    // Retrieve the subjects in which a
    // particular student is enrolled.
    public static List<Course> loadEnrolledCourses(
            String studentNumber
    ) throws SQLException {

        List<Course> courses = new ArrayList<>();

        String sql =
            "SELECT s.SubjectID, s.SubjectCode, " +
            "s.SubjectName " +
            "FROM Enrollments e " +
            "JOIN Users u ON e.StudentID = u.UserID " +
            "JOIN Subjects s ON e.SubjectID = s.SubjectID " +
            "WHERE u.StudentNumber = ? " +
            "AND e.Status = 'Enrolled' " +
            "ORDER BY s.SubjectCode";

        try (
            Connection connection =
                DatabaseConnection.getConnection();

            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {

            statement.setString(1, studentNumber);

            try (
                ResultSet result =
                    statement.executeQuery()
            ) {

                while (result.next()) {

                    courses.add(new Course(
                        result.getInt("SubjectID"),
                        result.getString("SubjectCode"),
                        result.getString("SubjectName")
                    ));
                }
            }
        }

        return courses;
    }
}
