/**
 * TestOLAStorage.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

public class TestOLAStorage {

    public static void main(String[] args) {

        DataInitializer.initialize();

        CourseAssessment worksheet =
                AcademicData.addOLAAssessment(
                        "MATH174",
                        "Worksheet",
                        20,
                        30
                );

        CourseAssessment assignment =
                AcademicData.addOLAAssessment(
                        "MATH174",
                        "Assignment",
                        50,
                        70
                );

        AcademicData.setAssessmentScore(
                worksheet.getAssessmentId(),
                "20260001",
                18
        );

        AcademicData.setAssessmentScore(
                assignment.getAssessmentId(),
                "20260001",
                45
        );

        AcademicData.setAssessmentScore(
                worksheet.getAssessmentId(),
                "20260002",
                15
        );

        AcademicData.setAssessmentScore(
                assignment.getAssessmentId(),
                "20260002",
                40
        );

        System.out.printf(
                "Marco OLA: %.2f%%%n",
                AcademicData.calculateStudentOLA(
                        "20260001",
                        "MATH174"
                )
        );

        System.out.printf(
                "Juan OLA: %.2f%%%n",
                AcademicData.calculateStudentOLA(
                        "20260002",
                        "MATH174"
                )
        );
    }
}