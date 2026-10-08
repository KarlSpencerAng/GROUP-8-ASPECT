/**
 * TestGradeSaving.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.SQLException;
import java.util.List;

public class TestGradeSaving {

    public static void main(String[] args) {

        try {

            System.out.println(
                "=== PHASE 3B: GRADE SAVING TEST ==="
            );

            List<Student> students =
                AcademicRepository.loadStudents();

            List<Course> courses =
                AcademicRepository.loadCurrentCourses();

            Student marco = null;
            Course programming = null;

            for (Student student : students) {

                if (student.getStudentNumber()
                        .equals("20260001")) {

                    marco = student;
                    break;
                }
            }

            for (Course course : courses) {

                if (course.getCourseCode()
                        .equals("CSS123P")) {

                    programming = course;
                    break;
                }
            }

            if (marco == null
                    || programming == null) {

                throw new IllegalStateException(
                    "Required demonstration "
                    + "records were not found."
                );
            }

            Grade grade =
                new Grade(marco, programming);

            grade.setExam1(90);
            grade.setExam2(88);
            grade.setExam3(92);

            grade.setFinalExam(89);
            grade.setOLA(95);
            grade.setCoursera(94);

            grade.calculateFinalGrade();

            System.out.printf(
                "Calculated grade: %.2f%%%n",
                grade.getFinalGrade()
            );

            // Save the draft to MySQL.
            GradeRepository.saveDraftGrade(
                grade
            );

            System.out.println(
                "Draft saved successfully."
            );

            // Retrieve the saved record.
            List<GradeRepository.GradeRecord>
                databaseGrades =
                    GradeRepository.loadStudentGrades(
                        "20260001"
                    );

            boolean found = false;

            for (
                GradeRepository.GradeRecord record :
                    databaseGrades
            ) {

                if (record.courseCode
                        .equals("CSS123P")) {

                    found = true;

                    System.out.printf(
                        "Retrieved: %s | %.2f%% | %s%n",
                        record.courseCode,
                        record.finalPercentage,
                        record.submissionStatus
                    );

                    boolean matches =
                        Math.abs(
                            record.finalPercentage
                            - grade.getFinalGrade()
                        ) < 0.01;

                    if (!matches) {
                        throw new IllegalStateException(
                            "Stored grade does not "
                            + "match calculated grade."
                        );
                    }

                    if (!"Draft".equals(
                            record.submissionStatus)) {

                        throw new IllegalStateException(
                            "Unexpected submission status."
                        );
                    }
                }
            }

            if (!found) {
                throw new IllegalStateException(
                    "Saved grade was not retrieved."
                );
            }

            System.out.println(
                "GRADE SAVING TEST PASSED"
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
}
