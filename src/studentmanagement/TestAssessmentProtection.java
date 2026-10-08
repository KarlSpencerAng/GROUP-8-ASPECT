/**
 * TestAssessmentProtection.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

public class TestAssessmentProtection {

    public static void main(String[] args) {

        DataInitializer.initialize();

        CourseAssessment assessment =
                AcademicData.addOLAAssessment(
                        "MATH174",
                        "Worksheet",
                        20,
                        100
                );

        System.out.println(
                "Assessment created successfully."
        );

        Grade marcoGrade = null;

        for (Grade grade : AcademicData.getGrades()) {

            if (grade.getStudent()
                    .getStudentNumber()
                    .equals("20260001")
                    && grade.getCourse()
                    .getCourseCode()
                    .equals("MATH174")) {

                marcoGrade = grade;
                break;
            }
        }

        if (marcoGrade == null) {
            throw new IllegalStateException(
                    "Marco's grade was not found."
            );
        }

        // Simulate an already-submitted grade.
        marcoGrade.setStatus("Submitted");

        try {

            AcademicData.deleteOLAAssessment(
                    assessment.getAssessmentId()
            );

            System.out.println(
                    "ERROR: Protection did not work."
            );

        } catch (IllegalStateException e) {

            System.out.println(
                    "SUCCESS: Submitted grades protected."
            );
        }

        try {

            AcademicData.addOLAAssessment(
                    "MATH174",
                    "New Quiz",
                    50,
                    20
            );

            System.out.println(
                    "ERROR: Creation was not blocked."
            );

        } catch (IllegalStateException e) {

            System.out.println(
                    "SUCCESS: New assessments blocked."
            );
        }
    }
}