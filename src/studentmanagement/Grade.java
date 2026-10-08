/**
 * Grade.java
 * Purpose: Grade model and calculations.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

public class Grade {

    private Student student;
    private Course course;

    // Course Outcomes
    private double exam1;
    private double exam2;
    private double exam3;

    // Additional grading components
    private double finalExam;
    private double ola;
    private double coursera;

    // Calculated results
    private double finalGrade;
    private double numericalGrade;

    private boolean calculationComplete = false;

    // Grade submission workflow
    private String status;

    public Grade(Student student, Course course) {
        this.student = student;
        this.course = course;
        this.status = "Draft";
    }

    // =========================
    // STUDENT AND COURSE
    // =========================

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    // =========================
    // COURSE OUTCOMES
    // =========================

    public double getExam1() {
        return exam1;
    }

    public double getExam2() {
        return exam2;
    }

    public double getExam3() {
        return exam3;
    }

    public void setExam1(double score) {
        validateScore(score);
        exam1 = score;
        markCalculationIncomplete();
    }

    public void setExam2(double score) {
        validateScore(score);
        exam2 = score;
        markCalculationIncomplete();
    }

    public void setExam3(double score) {
        validateScore(score);
        exam3 = score;
        markCalculationIncomplete();
    }

    // =========================
    // FINAL EXAM
    // =========================

    public double getFinalExam() {
        return finalExam;
    }

    public void setFinalExam(double score) {
        validateScore(score);
        finalExam = score;
        markCalculationIncomplete();
    }

    // =========================
    // OLA
    // =========================

    public double getOLA() {
        return ola;
    }

    public void setOLA(double score) {
        validateScore(score);
        ola = score;
        markCalculationIncomplete();
    }

    // =========================
    // COURSERA
    // =========================

    public double getCoursera() {
        return coursera;
    }

    public void setCoursera(double score) {
        validateScore(score);
        coursera = score;
        markCalculationIncomplete();
    }

    // =========================
    // STATUS
    // =========================

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // =========================
    // CALCULATION STATUS
    // =========================

    public boolean isCalculationComplete() {
        return calculationComplete;
    }

    public void markCalculationIncomplete() {
        calculationComplete = false;
    }

    // =========================
    // VALIDATION
    // =========================

    private void validateScore(double score) {

        if (!Double.isFinite(score)
                || score < 0
                || score > 100) {

            throw new IllegalArgumentException(
                    "Scores must be between 0 and 100."
            );
        }
    }

    // =========================
    // FINAL GRADE CALCULATION
    // =========================

    public void calculateFinalGrade() {

        finalGrade =
                (exam1 * 0.15)
                + (exam2 * 0.15)
                + (exam3 * 0.15)
                + (finalExam * 0.40)
                + (ola * 0.10)
                + (coursera * 0.05);

        numericalGrade =
                convertToNumericalGrade(finalGrade);

        calculationComplete = true;
    }

    // =========================
    // NUMERICAL GRADE CONVERSION
    // =========================

    private double convertToNumericalGrade(double percentage) {

        if (percentage >= 98) return 1.00;
        if (percentage >= 95) return 1.25;
        if (percentage >= 92) return 1.50;
        if (percentage >= 89) return 1.75;
        if (percentage >= 86) return 2.00;
        if (percentage >= 83) return 2.25;
        if (percentage >= 80) return 2.50;
        if (percentage >= 78) return 2.75;
        if (percentage >= 75) return 3.00;

        return 5.00;
    }

    // =========================
    // RESULTS
    // =========================

    public double getFinalGrade() {
        return finalGrade;
    }

    public double getNumericalGrade() {
        return numericalGrade;
    }

    public String getAcademicResult() {

        if (numericalGrade <= 3.00) {
            return "PASSED";
        }

        return "FAILED";
    }
}