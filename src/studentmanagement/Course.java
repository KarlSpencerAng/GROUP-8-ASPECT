/**
 * Course.java
 * Purpose: Course model.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

public class Course {

    private int courseId;
    private String courseCode;
    private String courseName;

    public Course(int courseId, String courseCode, String courseName) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}