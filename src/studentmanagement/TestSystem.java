/**
 * TestSystem.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

import java.util.ArrayList;
import java.util.List;

public class TestSystem {

    public static void main(String[] args) {

        // Create a student
        Student student = new Student(
                1,
                "20260001",
                "Marco Villones",
                "BSIT-1"
        );

        // Create courses
        Course calculus = new Course(
                1,
                "MATH174",
                "DIFFERENTIAL AND INTEGRAL CALCULUS"
        );

        Course networking = new Course(
                2,
                "ITS162-1L",
                "DATA COMMUNICATION AND NETWORKING ESSENTIALS"
        );

        Course programming3 = new Course(
                3,
                "CSS123P",
                "COMPUTER PROGRAMMING 3"
        );

        Course artAppreciation = new Course(
                4,
                "GED108",
                "ART APPRECIATION"
        );

        Course informationSystems = new Course(
                5,
                "ISS120",
                "INFORMATION SYSTEMS AND BUSINESS PROCESSES"
        );

        Course professionalCommunications = new Course(
                6,
                "PCC150",
                "PROFESSIONAL COMMUNICATIONS COURSE"
        );

        Course quantitativeMethods = new Course(
                7,
                "MATH181",
                "QUANTITATIVE METHODS"
        );

        // Create grades for each course

        Grade calculusGrade = new Grade(student, calculus);
        calculusGrade.setExam1(90);
        calculusGrade.setExam2(85);
        calculusGrade.setExam3(92);
        calculusGrade.calculateFinalGrade();

        Grade networkingGrade = new Grade(student, networking);
        networkingGrade.setExam1(88);
        networkingGrade.setExam2(91);
        networkingGrade.setExam3(86);
        networkingGrade.calculateFinalGrade();

        Grade programmingGrade = new Grade(student, programming3);
        programmingGrade.setExam1(95);
        programmingGrade.setExam2(93);
        programmingGrade.setExam3(94);
        programmingGrade.calculateFinalGrade();

        Grade artGrade = new Grade(student, artAppreciation);
        artGrade.setExam1(89);
        artGrade.setExam2(90);
        artGrade.setExam3(92);
        artGrade.calculateFinalGrade();

        Grade informationSystemsGrade =
                new Grade(student, informationSystems);

        informationSystemsGrade.setExam1(85);
        informationSystemsGrade.setExam2(88);
        informationSystemsGrade.setExam3(87);
        informationSystemsGrade.calculateFinalGrade();

        Grade communicationsGrade =
                new Grade(student, professionalCommunications);

        communicationsGrade.setExam1(91);
        communicationsGrade.setExam2(89);
        communicationsGrade.setExam3(94);
        communicationsGrade.calculateFinalGrade();

        Grade quantitativeGrade =
                new Grade(student, quantitativeMethods);

        quantitativeGrade.setExam1(84);
        quantitativeGrade.setExam2(86);
        quantitativeGrade.setExam3(88);
        quantitativeGrade.calculateFinalGrade();

        // Store all grades in a list
        List<Grade> grades = new ArrayList<>();

        grades.add(calculusGrade);
        grades.add(networkingGrade);
        grades.add(programmingGrade);
        grades.add(artGrade);
        grades.add(informationSystemsGrade);
        grades.add(communicationsGrade);
        grades.add(quantitativeGrade);

        // Create GradeModule
        GradeModule module = new GradeModule();

        // Calculate academic performance
        double overallAverage =
                module.calculateOverallAverage(grades);

        double highestGrade =
                module.getHighestGrade(grades);

        double lowestGrade =
                module.getLowestGrade(grades);

        // Display student information
        System.out.println("==============================================");
        System.out.println("       STUDENT ACADEMIC SYSTEM");
        System.out.println("==============================================");

        System.out.println("Student: " + student.getName());
        System.out.println("Student Number: " +
                student.getStudentNumber());
        System.out.println("Section: " + student.getSection());

        System.out.println();

        // Display subject grades
        System.out.println("SUBJECT GRADES");
        System.out.println("----------------------------------------------");

        System.out.printf(
                "%-12s %-45s %.2f%n",
                calculus.getCourseCode(),
                calculus.getCourseName(),
                calculusGrade.getFinalGrade()
        );

        System.out.printf(
                "%-12s %-45s %.2f%n",
                networking.getCourseCode(),
                networking.getCourseName(),
                networkingGrade.getFinalGrade()
        );

        System.out.printf(
                "%-12s %-45s %.2f%n",
                programming3.getCourseCode(),
                programming3.getCourseName(),
                programmingGrade.getFinalGrade()
        );

        System.out.printf(
                "%-12s %-45s %.2f%n",
                artAppreciation.getCourseCode(),
                artAppreciation.getCourseName(),
                artGrade.getFinalGrade()
        );

        System.out.printf(
                "%-12s %-45s %.2f%n",
                informationSystems.getCourseCode(),
                informationSystems.getCourseName(),
                informationSystemsGrade.getFinalGrade()
        );

        System.out.printf(
                "%-12s %-45s %.2f%n",
                professionalCommunications.getCourseCode(),
                professionalCommunications.getCourseName(),
                communicationsGrade.getFinalGrade()
        );

        System.out.printf(
                "%-12s %-45s %.2f%n",
                quantitativeMethods.getCourseCode(),
                quantitativeMethods.getCourseName(),
                quantitativeGrade.getFinalGrade()
        );

        System.out.println();

        // Display academic performance
        System.out.println("ACADEMIC PERFORMANCE");
        System.out.println("----------------------------------------------");

        System.out.printf(
                "Overall Average: %.2f%n",
                overallAverage
        );

        System.out.printf(
                "Highest Grade: %.2f%n",
                highestGrade
        );

        System.out.printf(
                "Lowest Grade: %.2f%n",
                lowestGrade
        );

        System.out.println("==============================================");
    }
}