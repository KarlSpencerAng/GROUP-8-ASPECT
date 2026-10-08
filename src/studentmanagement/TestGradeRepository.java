/**
 * TestGradeRepository.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.SQLException;
import java.util.List;

public class TestGradeRepository {

    public static void main(String[] args) {

        try {

            System.out.println(
                "=== MYSQL GRADE RETRIEVAL ==="
            );

            List<GradeRepository.GradeRecord> grades =
                GradeRepository.loadAllGrades();

            int passed = 0;
            int failed = 0;

            for (
                GradeRepository.GradeRecord grade : grades
            ) {

                double calculated =
                    grade.calculatePercentage();

                boolean matches =
                    Math.abs(
                        calculated - grade.finalPercentage
                    ) < 0.005;

                System.out.printf(
                    "%s | %s | Stored: %.2f | "
                    + "Calculated: %.2f | %s | %s%n",
                    grade.studentNumber,
                    grade.courseCode,
                    grade.finalPercentage,
                    calculated,
                    grade.submissionStatus,
                    matches ? "PASS" : "FAIL"
                );

                if (matches) {
                    passed++;
                } else {
                    failed++;
                }
            }

            System.out.println(
                "\nTotal database grades: "
                + grades.size()
            );

            System.out.println(
                "Calculation checks passed: "
                + passed
            );

            System.out.println(
                "Calculation checks failed: "
                + failed
            );

            System.out.println(
                "\n=== MARCO'S DATABASE GRADES ==="
            );

            List<GradeRepository.GradeRecord> marcoGrades =
                GradeRepository.loadStudentGrades(
                    "20260001"
                );

            for (
                GradeRepository.GradeRecord grade :
                    marcoGrades
            ) {

                System.out.printf(
                    "%s | %.2f%% | %.2f | %s%n",
                    grade.courseCode,
                    grade.finalPercentage,
                    grade.numericalGrade,
                    grade.submissionStatus
                );
            }

        } catch (SQLException exception) {

            System.err.println(
                "DATABASE ERROR: "
                + exception.getMessage()
            );

            exception.printStackTrace();
        }
    }
}
