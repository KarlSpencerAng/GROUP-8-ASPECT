/**
 * TestSubmitAllRollback.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class TestSubmitAllRollback {

    // Replace these four values using your
    // MySQL query results.

private static final String VALID_STUDENT =
    "20260001";

private static final String VALID_COURSE =
    "ITS162-1L";

private static final String INVALID_STUDENT =
    "20260001";

private static final String INVALID_COURSE =
    "GED108";


    public static void main(String[] args) {

        try {

            Student validStudent =
                findStudent(VALID_STUDENT);

            Course validCourse =
                findCourse(VALID_COURSE);

            Student invalidStudent =
                findStudent(INVALID_STUDENT);

            Course invalidCourse =
                findCourse(INVALID_COURSE);

            Grade validGrade =
                createTestGrade(
                    validStudent,
                    validCourse
                );

            Grade invalidGrade =
                createTestGrade(
                    invalidStudent,
                    invalidCourse
                );

            System.out.println(
                "=== SUBMIT ALL ROLLBACK TEST ==="
            );

            // Confirm both records are absent
            // before beginning the test.
            assertNoDatabaseGrade(
                VALID_STUDENT,
                VALID_COURSE
            );

            assertNoDatabaseGrade(
                INVALID_STUDENT,
                INVALID_COURSE
            );

            List<Grade> batch =
                Arrays.asList(
                    validGrade,
                    invalidGrade
                );

            boolean rejected = false;

            try {

                GradeRepository.submitGrades(
                    batch
                );

            } catch (SQLException exception) {

                rejected = true;

                System.out.println(
                    "Expected rejection: "
                    + exception.getMessage()
                );
            }

            if (!rejected) {

                throw new IllegalStateException(
                    "TEST FAILED: Invalid batch "
                    + "was accepted."
                );
            }

            // Verify that the valid insertion
            // was rolled back.
            assertNoDatabaseGrade(
                VALID_STUDENT,
                VALID_COURSE
            );

            assertNoDatabaseGrade(
                INVALID_STUDENT,
                INVALID_COURSE
            );

            // Java statuses should also remain
            // unchanged after failure.
            if (!"Draft".equals(
                    validGrade.getStatus())
                    || !"Draft".equals(
                        invalidGrade.getStatus())) {

                throw new IllegalStateException(
                    "Java statuses changed "
                    + "despite rollback."
                );
            }

            System.out.println(
                "PASS: Invalid batch rejected."
            );

            System.out.println(
                "PASS: No partial database writes."
            );

            System.out.println(
                "PASS: Java statuses unchanged."
            );

            System.out.println(
                "ALL ROLLBACK TESTS PASSED"
            );

        } catch (
                SQLException
                | RuntimeException exception) {

            System.err.println(
                "TEST FAILED: "
                + exception.getMessage()
            );

            exception.printStackTrace();
        }
    }

    private static Student findStudent(
            String studentNumber
    ) throws SQLException {

        for (Student student :
                AcademicRepository.loadStudents()) {

            if (student.getStudentNumber()
                    .equals(studentNumber)) {

                return student;
            }
        }

        throw new IllegalArgumentException(
            "Student not found: "
            + studentNumber
        );
    }

    private static Course findCourse(
            String courseCode
    ) throws SQLException {

        for (Course course :
                AcademicRepository.loadCurrentCourses()) {

            if (course.getCourseCode()
                    .equals(courseCode)) {

                return course;
            }
        }

        throw new IllegalArgumentException(
            "Course not found: "
            + courseCode
        );
    }

    private static Grade createTestGrade(
            Student student,
            Course course
    ) {

        Grade grade =
            new Grade(student, course);

        grade.setExam1(90);
        grade.setExam2(88);
        grade.setExam3(92);

        grade.setFinalExam(89);
        grade.setOLA(95);
        grade.setCoursera(94);

        grade.calculateFinalGrade();

        grade.setStatus("Draft");

        return grade;
    }

    private static void assertNoDatabaseGrade(
            String studentNumber,
            String courseCode
    ) throws SQLException {

        for (
            GradeRepository.GradeRecord record :
                GradeRepository.loadStudentGrades(
                    studentNumber
                )
        ) {

            if (record.courseCode
                    .equals(courseCode)) {

                throw new IllegalStateException(
                    "Unexpected database grade: "
                    + studentNumber
                    + " / "
                    + courseCode
                );
            }
        }
    }
}
