/**
 * TestWeightedGrade.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

public class TestWeightedGrade {

    public static void main(String[] args) {

        Student student = new Student(
                1,
                "20260001",
                "Marco Villones",
                "SECTION A"
        );

        Course course = new Course(
                1,
                "MATH174",
                "Differential and Integral Calculus"
        );

        Grade grade = new Grade(student, course);

        grade.setExam1(96);
        grade.setExam2(94);
        grade.setExam3(97);

        grade.setFinalExam(93);
        grade.setOLA(100);
        grade.setCoursera(100);

        grade.calculateFinalGrade();

        System.out.printf(
                "Final Percentage: %.2f%%%n",
                grade.getFinalGrade()
        );

        System.out.printf(
                "Numerical Grade: %.2f%n",
                grade.getNumericalGrade()
        );

        System.out.println(
                "Academic Result: "
                        + grade.getAcademicResult()
        );
    }
}