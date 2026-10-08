/**
 * Student.java
 * Purpose: Student model.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

public class Student {

    private int studentId;
    private String studentNumber;
    private String name;
    private String section;

    public Student(int studentId, String studentNumber, String name, String section) {
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.name = name;
        this.section = section;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getName() {
        return name;
    }

    public String getSection() {
        return section;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Override
    public String toString() {
        return studentNumber + " - " + name;
    }
}