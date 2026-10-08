/**
 * TestAcademicRepository.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.SQLException;
import java.util.List;

public class TestAcademicRepository {

    public static void main(String[] args) {

        try {

            System.out.println(
                "=== MYSQL STUDENT TEST ==="
            );

            List<Student> students =
                AcademicRepository.loadStudents();

            for (Student student : students) {

                System.out.println(
                    student.getStudentNumber() +
                    " | " +
                    student.getName() +
                    " | " +
                    student.getSection()
                );
            }

            System.out.println(
                "\nTotal students: " +
                students.size()
            );

            System.out.println(
                "\n=== MYSQL SUBJECT TEST ==="
            );

            List<Course> courses =
                AcademicRepository.loadCurrentCourses();

            for (Course course : courses) {

                System.out.println(
                    course.getCourseCode() +
                    " | " +
                    course.getCourseName()
                );
            }

            System.out.println(
                "\nTotal current subjects: " +
                courses.size()
            );

            System.out.println(
                "\n=== STUDENT ENROLLMENT TEST ==="
            );

            String studentNumber = "20260001";

            List<Course> enrolledCourses =
                AcademicRepository.loadEnrolledCourses(
                    studentNumber
                );

            System.out.println(
                "Student: " + studentNumber
            );

            for (Course course : enrolledCourses) {

                System.out.println(
                    course.getCourseCode() +
                    " | " +
                    course.getCourseName()
                );
            }

            System.out.println(
                "\nTotal enrollments: " +
                enrolledCourses.size()
            );

        } catch (SQLException exception) {

            System.err.println(
                "DATABASE ERROR: " +
                exception.getMessage()
            );

            exception.printStackTrace();
        }
    }
}
