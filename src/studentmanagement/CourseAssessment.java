/**
 * CourseAssessment.java
 * Purpose: Course assessment model.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

public class CourseAssessment {

    private final int assessmentId;
    private final String courseCode;
    private final Assessment assessment;

    public CourseAssessment(
            int assessmentId,
            String courseCode,
            Assessment assessment) {

        if (assessmentId <= 0) {
            throw new IllegalArgumentException(
                    "Assessment ID must be positive."
            );
        }

        if (courseCode == null
                || courseCode.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Course code is required."
            );
        }

        if (assessment == null) {
            throw new IllegalArgumentException(
                    "Assessment is required."
            );
        }

        this.assessmentId = assessmentId;
        this.courseCode = courseCode;
        this.assessment = assessment;
    }

    public int getAssessmentId() {
        return assessmentId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public Assessment getAssessment() {
        return assessment;
    }
}