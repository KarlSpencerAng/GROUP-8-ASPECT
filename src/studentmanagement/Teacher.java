/**
 * Teacher.java
 * Purpose: Teacher model.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

public class Teacher {

    private int teacherId;
    private String teacherNumber;
    private String name;
    private String email;

    public Teacher(int teacherId, String teacherNumber, String name, String email) {
        this.teacherId = teacherId;
        this.teacherNumber = teacherNumber;
        this.name = name;
        this.email = email;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public String getTeacherNumber() {
        return teacherNumber;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return teacherNumber + " - " + name;
    }
}