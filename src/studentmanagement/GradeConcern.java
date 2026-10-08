/**
 * GradeConcern.java
 * Purpose: Grade concern model.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

public class GradeConcern {

    private int concernId;
    private Student student;
    private Course course;
    private String reason;
    private String status;

    public GradeConcern(
            int concernId,
            Student student,
            Course course,
            String reason) {

        this.concernId = concernId;
        this.student = student;
        this.course = course;
        this.reason = reason;
        this.status = "Pending";
    }

    public int getConcernId() {
        return concernId;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}