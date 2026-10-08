/**
 * TeacherDashboard.java
 * Purpose: Teacher-facing interface.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

import javax.swing.*;

import javax.swing.table.DefaultTableModel;

import java.awt.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class TeacherDashboard extends JFrame {
	
	private final int loggedInTeacherId;

    private JComboBox<String> subjectComboBox;

    private JTable gradeTable;

    private DefaultTableModel tableModel;

    private JTable concernTable;

    private DefaultTableModel concernTableModel;
    
    private final Map<Integer, GradeConcern> databaseConcerns =
            new LinkedHashMap<>();

    private JTextField revisionExam1Field;

    private JTextField revisionExam2Field;

    private JTextField revisionExam3Field;

    private JTextField revisionFinalExamField;

    private JTextField revisionCourseraField;

    private JTextField revisionOLAField;

    private JLabel revisionStudentLabel;

    private JLabel revisionCourseLabel;

    private JLabel revisionFinalGradeLabel;

    private JLabel revisionStatusLabel;

    private double pendingOutcome1;

    private double pendingOutcome2;

    private double pendingOutcome3;

    private double pendingFinalExam;

    private double pendingCoursera;

    private double pendingOLA;

    private int pendingConcernId = -1;

    private boolean revisionCalculated = false;

    private int selectedConcernRow = -1;

    public TeacherDashboard(int teacherId) {
        if (teacherId <= 0) {
            throw new IllegalArgumentException(
                    "A valid authenticated teacher ID is required."
            );
        }

        this.loggedInTeacherId = teacherId;

        System.out.println(
        	    "Authenticated Teacher ID: " + loggedInTeacherId
        	);
        
        setTitle("Teacher Dashboard");

        setSize(1200, 740);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        // Main panel

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title

        JLabel titleLabel = new JLabel(

                "Teacher Dashboard",

                SwingConstants.CENTER

        );

        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Subject selection panel

        JPanel subjectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel subjectLabel = new JLabel("Subject:");

        subjectComboBox = new JComboBox<>();

        // Assigned subjects are loaded from MySQL, not from a fixed demo list.
        JButton refreshSubjectsButton = new JButton("Refresh Subjects / Roster");
        refreshSubjectsButton.addActionListener(e -> refreshGradeManagementSubjects());

        subjectPanel.add(subjectLabel);

        subjectPanel.add(subjectComboBox);
        subjectPanel.add(refreshSubjectsButton);

        // Table columns

String[] columns = {

        "Student Number",

        "Student Name",

        "CO1 (Quiz 1)",

        "CO2 (Quiz 2)",

        "CO3 (Quiz 3)",

        "Final Exam",

        "OLA",

        "Coursera",

        "Final Percentage",

        "Numerical Grade",

        "Academic Result",

        "Status"

};

tableModel = new DefaultTableModel(columns, 0) {

    @Override

    public boolean isCellEditable(int row, int column) {

        if ((column >= 2 && column <= 5)

                || column == 7) {

            Object status = getValueAt(row, 11);

            return status != null

                    && !status.toString().equals("Pending")

                    && !status.toString().equals("Posted")

                    && !status.toString().equals("Submitted")

                    && !status.toString().equals("Revised");

        }

        return false;

    }

};



        gradeTable = new JTable(tableModel);

        gradeTable.setRowHeight(30);

        gradeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < gradeTable.getColumnCount(); i++) {

            gradeTable.getColumnModel().getColumn(i).setPreferredWidth(135);

        }

        gradeTable.getTableHeader().setReorderingAllowed(false);

        subjectComboBox.addActionListener(

                e -> loadGradesForSelectedCourse()

        );

        JScrollPane scrollPane = new JScrollPane(gradeTable);

        // Center panel

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        centerPanel.add(subjectPanel, BorderLayout.NORTH);

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Buttons

        JPanel buttonPanel = new JPanel(

                new FlowLayout(FlowLayout.RIGHT)

        );

        JButton calculateButton = new JButton("Calculate All");

        JButton calculateSelectedButton = new JButton("Calculate Selected");

        JButton submitButton = new JButton("Submit Class to Registrar");

        JButton feedbackButton = new JButton("Registrar Feedback");

        JButton logoutButton = new JButton("Logout");

        JButton refreshButton = new JButton("Refresh Grades");

        buttonPanel.add(logoutButton);

        buttonPanel.add(calculateButton);

        buttonPanel.add(calculateSelectedButton);

        buttonPanel.add(submitButton);

        buttonPanel.add(feedbackButton);

        buttonPanel.add(refreshButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Calculate button

        calculateButton.addActionListener(e -> calculateGrades());

        calculateSelectedButton.addActionListener(e -> calculateSelectedGrade());

        // Submit button

        submitButton.addActionListener(e -> submitGrades());

        feedbackButton.addActionListener(e -> showRegistrarFeedback());

        logoutButton.addActionListener(e -> logout());

        refreshButton.addActionListener(e -> {

            int choice = JOptionPane.showConfirmDialog(

                    this,

                    "Refresh the table? Unsaved changes will be lost.",

                    "Confirm Refresh",

                    JOptionPane.YES_NO_OPTION

            );

            if (choice == JOptionPane.YES_OPTION) {

                refreshGradeManagementSubjects();

            }

        });

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab(

                "Grade Management",

                mainPanel

        );

        tabbedPane.addTab(

                "OLA Management",

                new OLAManagementPanel(loggedInTeacherId)

        );

        tabbedPane.addTab(

                "Grade Concerns",

                createGradeConcernPanel()

        );
        
        tabbedPane.addTab(
        	    "Historical Records",
        	    new HistoricalRecordsPanel()
        	);

        tabbedPane.addTab("Enrollment & Roster", new TeacherEnrollmentPanel(loggedInTeacherId));
        tabbedPane.addTab("Direct Grade Revision", new DirectGradeRevisionPanel(loggedInTeacherId));
        tabbedPane.addTab("Post-Submission Assessment", new PostSubmissionAssessmentPanel(loggedInTeacherId));
        tabbedPane.addTab("Class Insights & Schedule", new TeacherClassInsightsPanel(loggedInTeacherId));

        // Load the authenticated teacher's assigned subjects from MySQL.
        refreshGradeManagementSubjects();

        AspectDashboardShell.install(this, tabbedPane, "Teacher");

          }









private boolean calculateGrades() {

    return calculateGrades(-1);

}

private void calculateSelectedGrade() {

    int selectedRow = gradeTable.getSelectedRow();

    if (selectedRow < 0) {

        JOptionPane.showMessageDialog(this,

                "Select a student row first.", "No Student Selected",

                JOptionPane.WARNING_MESSAGE);

        return;

    }

    int modelRow = gradeTable.convertRowIndexToModel(selectedRow);

    calculateGrades(modelRow);

}

// selectedModelRow = -1 calculates all; otherwise only the chosen student.

private boolean calculateGrades(int selectedModelRow) {

    if (gradeTable.isEditing()) {

        if (!gradeTable.getCellEditor()

                .stopCellEditing()) {

            return false;

        }

    }

    Object selection =

            subjectComboBox.getSelectedItem();

    if (selection == null) {

        return false;

    }

    Course selectedCourse =

            getSelectedCourse(selection.toString());

    if (selectedCourse == null) {

        return false;

    }

    int rowCount = tableModel.getRowCount();

    double[][] scores =

            new double[rowCount][6];

    Grade[] grades =

            new Grade[rowCount];

    // STAGE 1: VALIDATE ALL STUDENTS

    try {

        for (int row = 0; row < rowCount; row++) {

            if (selectedModelRow >= 0 && row != selectedModelRow) continue;

            String studentNumber =

                    tableModel.getValueAt(row, 0)

                            .toString();

            Grade grade =

                    findGrade(

                            studentNumber,

                            selectedCourse.getCourseCode()

                    );

            if (grade == null) {

                throw new IllegalStateException(

                        "Grade record not found: "

                        + studentNumber

                );

            }

            grades[row] = grade;

            if (grade.getStatus().equals("Pending")

                    || grade.getStatus().equals("Posted")

                    || grade.getStatus().equals("Submitted")

                    || grade.getStatus().equals("Revised")) {

                continue;

            }

            int[] editableColumns = {

                    2, 3, 4, 5, 7

            };

            for (int i = 0;

                    i < editableColumns.length;

                    i++) {

                Object value =

                        tableModel.getValueAt(

                                row,

                                editableColumns[i]

                        );

                if (value == null

                        || value.toString()

                                .trim().isEmpty()) {

                    throw new IllegalArgumentException(

                            "Missing score for student "

                            + studentNumber

                    );

                }

                double score =

                        Double.parseDouble(

                                value.toString().trim()

                        );

                if (!Double.isFinite(score)

                        || score < 0

                        || score > 100) {

                    throw new IllegalArgumentException(

                            "Scores must be between "

                            + "0 and 100. Student: "

                            + studentNumber

                    );

                }

                scores[row][i] = score;

            }

            // OLA is calculated from saved assessments.

            scores[row][5] =

                    AcademicData.calculateStudentOLA(

                            studentNumber,

                            selectedCourse.getCourseCode()

                    );

        }

    } catch (NumberFormatException e) {

        JOptionPane.showMessageDialog(

                this,

                "Please enter valid numeric scores.",

                "Invalid Score",

                JOptionPane.ERROR_MESSAGE

        );

        return false;

    } catch (IllegalArgumentException

            | IllegalStateException e) {

        JOptionPane.showMessageDialog(

                this,

                e.getMessage(),

                "Cannot Calculate",

                JOptionPane.WARNING_MESSAGE

        );

        return false;

    }

    // STAGE 2: APPLY VALIDATED SCORES

    for (int row = 0; row < rowCount; row++) {

        if (selectedModelRow >= 0 && row != selectedModelRow) continue;

        Grade grade = grades[row];

        if (grade.getStatus().equals("Pending")

                || grade.getStatus().equals("Posted")

                || grade.getStatus().equals("Submitted")

                || grade.getStatus().equals("Revised")) {

            continue;

        }

        grade.setExam1(scores[row][0]);

        grade.setExam2(scores[row][1]);

        grade.setExam3(scores[row][2]);

        grade.setFinalExam(scores[row][3]);

        grade.setCoursera(scores[row][4]);

        grade.setOLA(scores[row][5]);

        grade.calculateFinalGrade();

        grade.setStatus("Draft");

        tableModel.setValueAt(

                String.format(

                        "%.2f",

                        grade.getFinalGrade()

                ),

                row,

                8

        );

        tableModel.setValueAt(

                String.format(

                        "%.2f",

                        grade.getNumericalGrade()

                ),

                row,

                9

        );

        tableModel.setValueAt(

                grade.getAcademicResult(),

                row,

                10

        );

        tableModel.setValueAt(

                String.format(

                        "%.2f",

                        grade.getOLA()

                ),

                row,

                6

        );

    }

    JOptionPane.showMessageDialog(

            this,

            selectedModelRow >= 0

                    ? "Selected student grade calculated successfully!"

                    : "All grades calculated successfully!"

    );

    return true;

}

    private Grade findGrade(

            String studentNumber,

            String courseCode) {

        for (Grade grade : AcademicData.getGrades()) {

            boolean sameStudent =

                    grade.getStudent()

                            .getStudentNumber()

                            .equals(studentNumber);

            boolean sameCourse =

                    grade.getCourse()

                            .getCourseCode()

                            .equals(courseCode);

            if (sameStudent && sameCourse) {

                return grade;

            }

        }

        return null;

    }



private void showRegistrarFeedback() {
    Object selection = subjectComboBox.getSelectedItem();
    if (selection == null) {
        JOptionPane.showMessageDialog(this, "Select a subject first.");
        return;
    }
    Course course = getSelectedCourse(selection.toString());
    if (course == null) return;

    try {
        String feedback = GradeRepository.loadLatestRegistrarFeedback(
                loggedInTeacherId, course.getCourseCode());
        if (feedback == null || feedback.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No Registrar return feedback is recorded for this class.",
                    "Registrar Feedback", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, feedback,
                    "Registrar Feedback - " + course.getCourseCode(),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (java.sql.SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Unable to load Registrar feedback:\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void submitGrades() {
    if (gradeTable.isEditing() && !gradeTable.getCellEditor().stopCellEditing()) return;

    Object selection = subjectComboBox.getSelectedItem();
    if (selection == null) return;
    Course course = getSelectedCourse(selection.toString());
    if (course == null) return;

    // A class submission is all-or-nothing. Every enrolled row must be complete.
    if (!calculateGrades(-1)) return;

    java.util.List<Grade> toSubmit = new java.util.ArrayList<>();
    for (int row = 0; row < tableModel.getRowCount(); row++) {
        String studentNumber = tableModel.getValueAt(row, 0).toString();
        Grade grade = findGrade(studentNumber, course.getCourseCode());
        if (grade == null) {
            JOptionPane.showMessageDialog(this, "Grade record missing: " + studentNumber);
            return;
        }
        String status = grade.getStatus();
        if ("Pending".equals(status)) {
            JOptionPane.showMessageDialog(this, "This class is already Pending Registrar review.");
            return;
        }
        if ("Posted".equals(status) || "Submitted".equals(status) || "Revised".equals(status)) {
            JOptionPane.showMessageDialog(this, "This class contains protected grades (" + status + ").");
            return;
        }
        if (!grade.isCalculationComplete()) {
            JOptionPane.showMessageDialog(this, "Cannot submit incomplete grades for " + studentNumber,
                    "Incomplete Grade", JOptionPane.WARNING_MESSAGE);
            return;
        }
        toSubmit.add(grade);
    }

    if (toSubmit.isEmpty()) {
        JOptionPane.showMessageDialog(this, "There are no class grades to submit.");
        return;
    }

    int confirmation = JOptionPane.showConfirmDialog(this,
            "Submit the COMPLETE class grade sheet for " + course.getCourseCode()
            + " to the Registrar?\n\n" + toSubmit.size() + " student grade(s) will be locked while Pending.",
            "Submit Class to Registrar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (confirmation != JOptionPane.YES_OPTION) return;

    try {
        GradeRepository.submitClassToRegistrar(
                toSubmit, loggedInTeacherId, course.getCourseCode());

        for (Grade grade : toSubmit) grade.setStatus("Pending");
        loadGradesForSelectedCourse();

        JOptionPane.showMessageDialog(this,
                "Class grade sheet submitted to the Registrar.\nStatus: Pending",
                "Submission Successful", JOptionPane.INFORMATION_MESSAGE);
    } catch (java.sql.SQLException | RuntimeException exception) {
        JOptionPane.showMessageDialog(this,
                "Submission failed.\n\n" + exception.getMessage()
                + "\n\nNo grades from this class were committed.",
                "Registrar Submission Error", JOptionPane.ERROR_MESSAGE);
        loadGradesForSelectedCourse();
    }
}

    private JPanel createGradeConcernPanel() {

        JPanel panel = new JPanel(

                new BorderLayout(10, 10)

        );

        panel.setBorder(

                BorderFactory.createEmptyBorder(

                        20, 20, 20, 20

                )

        );

        // =========================

        // TITLE

        // =========================

        JLabel titleLabel =

                new JLabel("Student Grade Concerns");

        titleLabel.setFont(

                new Font("Arial", Font.BOLD, 22)

        );

        panel.add(

                titleLabel,

                BorderLayout.NORTH

        );

        // =========================

        // CONCERN TABLE

        // =========================

        String[] columns = {

                "Concern ID",

                "Student Number",

                "Student Name",

                "Course",

                "Reason",

                "Status"

        };



        concernTableModel =

                new DefaultTableModel(

                        columns,

                        0

                ) {

                    @Override

                    public boolean isCellEditable(

                            int row,

                            int column) {

                        return false;

                    }

                };

        concernTable =

                new JTable(concernTableModel);

        concernTable.setRowHeight(30);

        concernTable.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                revisionCalculated = false;

                pendingConcernId = -1;

                selectedConcernRow = -1;

                revisionStatusLabel.setText("Select Review to load concern");

            }

        });

        concernTable.getTableHeader()

                .setReorderingAllowed(false);

        JScrollPane tableScrollPane =

                new JScrollPane(concernTable);

        // =========================

        // REVIEW BUTTON

        // =========================

        JButton reviewButton =

                new JButton("Review Selected Concern");

        JPanel tablePanel =

                new JPanel(

                        new BorderLayout(10, 10)

                );

        tablePanel.add(

                tableScrollPane,

                BorderLayout.CENTER

        );

        tablePanel.add(

                reviewButton,

                BorderLayout.SOUTH

        );

        // =========================

        // REVISION PANEL

        // =========================

        JPanel revisionPanel =

                new JPanel(new GridLayout(0, 2, 10, 10));

        revisionPanel.setBorder(

                BorderFactory.createTitledBorder(

                        "Grade Revision"

                )

        );

        revisionStudentLabel =

                new JLabel("-");

        revisionCourseLabel =

                new JLabel("-");

        revisionExam1Field =

                new JTextField();

        revisionExam2Field =

                new JTextField();

        revisionExam3Field =

                new JTextField();

        revisionFinalExamField = new JTextField();

        revisionCourseraField = new JTextField();

        revisionOLAField = new JTextField();

        revisionOLAField.setEditable(false);

        revisionFinalGradeLabel =

                new JLabel("-");

        revisionStatusLabel =

                new JLabel("-");

        revisionPanel.add(

                new JLabel("Student:")

        );

        revisionPanel.add(

                revisionStudentLabel

        );

        revisionPanel.add(

                new JLabel("Course:")

        );

        revisionPanel.add(

                revisionCourseLabel

        );

        revisionPanel.add(

                new JLabel("Exam 1:")

        );

        revisionPanel.add(

                revisionExam1Field

        );

        revisionPanel.add(

                new JLabel("Exam 2:")

        );

        revisionPanel.add(

                revisionExam2Field

        );

        revisionPanel.add(

                new JLabel("Exam 3:")

        );

        revisionPanel.add(

                revisionExam3Field

        );

        revisionPanel.add(new JLabel("Final Exam (40%):"));

        revisionPanel.add(revisionFinalExamField);

        revisionPanel.add(new JLabel("OLA (10%, read-only):"));

        revisionPanel.add(revisionOLAField);

        revisionPanel.add(new JLabel("Coursera (5%):"));

        revisionPanel.add(revisionCourseraField);

        revisionPanel.add(

                new JLabel("New Final Grade:")

        );

        revisionPanel.add(

                revisionFinalGradeLabel

        );

        revisionPanel.add(

                new JLabel("Status:")

        );

        revisionPanel.add(

                revisionStatusLabel

        );

        // =========================

        // REVISION BUTTONS

        // =========================

        JButton recalculateButton =

                new JButton("Recalculate");

        JButton olaRevisionButton = new JButton("Revise OLA Activities");

        JButton submitRevisionButton =

                new JButton("Submit Revision");

        JPanel revisionButtonPanel =

                new JPanel(

                        new FlowLayout(FlowLayout.RIGHT)

                );

        revisionButtonPanel.add(

                recalculateButton

        );

        revisionButtonPanel.add(olaRevisionButton);

        revisionButtonPanel.add(

                submitRevisionButton

        );

        JPanel bottomPanel =

                new JPanel(

                        new BorderLayout(10, 10)

                );

        bottomPanel.add(

                revisionPanel,

                BorderLayout.CENTER

        );

        bottomPanel.add(

                revisionButtonPanel,

                BorderLayout.SOUTH

        );

        // =========================

        // ADD COMPONENTS

        // =========================

        panel.add(

                tablePanel,

                BorderLayout.CENTER

        );

        panel.add(

                bottomPanel,

                BorderLayout.SOUTH

        );

        // =========================

        // BUTTON ACTIONS

        // =========================

        reviewButton.addActionListener(

                e -> reviewConcern()

        );

        recalculateButton.addActionListener(

                e -> recalculateRevision()

        );

        olaRevisionButton.addActionListener(e -> reviseOLAActivities());

        submitRevisionButton.addActionListener(

                e -> submitRevision()

        );

     // Load concerns from AcademicData

        loadGradeConcerns();

        return panel;

    }


private void reviewConcern() {

    int row = concernTable.getSelectedRow();

    if (row < 0) {
        JOptionPane.showMessageDialog(
                this, "Please select a concern first.");
        return;
    }

    int modelRow =
            concernTable.convertRowIndexToModel(row);

    int id = Integer.parseInt(
            concernTableModel.getValueAt(modelRow, 0).toString());

    try {
        // Retrieve the latest status from MySQL.
        List<GradeConcern> latestConcerns =
                GradeConcernRepository.loadAllConcerns();

        GradeConcern concern = null;

        for (GradeConcern item : latestConcerns) {
            if (item.getConcernId() == id) {
                concern = item;
                break;
            }
        }

        if (concern == null) {
            throw new IllegalStateException(
                    "Concern no longer exists. Refresh the list.");
        }

        if ("Revised".equals(concern.getStatus())) {
            JOptionPane.showMessageDialog(
                    this, "This concern has already been revised.");
            return;
        }

        String studentNumber =
                concern.getStudent().getStudentNumber();

        String studentName =
                concern.getStudent().getName();

        String courseCode =
                concern.getCourse().getCourseCode();

        Grade grade = findGrade(studentNumber, courseCode);

        if (grade == null
                || !grade.isCalculationComplete()
                || !("Submitted".equals(grade.getStatus())
                    || "Revised".equals(grade.getStatus()))) {

            JOptionPane.showMessageDialog(
                    this,
                    "The original grade must be submitted first.");
            return;
        }

        // Permanently update Pending concerns.
        // Existing Under Review concerns can be reopened.
        if ("Pending".equals(concern.getStatus())) {
            GradeConcernRepository.markUnderReview(id);
            concern.setStatus("Under Review");

        } else if (!"Under Review".equals(concern.getStatus())) {
            throw new IllegalStateException(
                    "Unexpected concern status.");
        }

        // Keep AcademicData's OLA validation in sync with the reviewed MySQL concern.
        // Replace the matching cached object rather than adding a duplicate.
        List<GradeConcern> localConcerns = AcademicData.getConcerns();
        localConcerns.removeIf(item -> item.getConcernId() == id);
        localConcerns.add(concern);
        databaseConcerns.put(id, concern);

        // Reset the previous revision preview.
        revisionCalculated = false;
        pendingConcernId = -1;
        selectedConcernRow = modelRow;

        revisionStudentLabel.setText(
                studentNumber + " - " + studentName);

        revisionCourseLabel.setText(courseCode);

        revisionExam1Field.setText(
                String.valueOf(grade.getExam1()));

        revisionExam2Field.setText(
                String.valueOf(grade.getExam2()));

        revisionExam3Field.setText(
                String.valueOf(grade.getExam3()));

        revisionFinalExamField.setText(
                String.valueOf(grade.getFinalExam()));

        revisionCourseraField.setText(
                String.valueOf(grade.getCoursera()));

        revisionOLAField.setText(
                String.format("%.2f", grade.getOLA()));

        revisionFinalGradeLabel.setText(
                String.format("%.2f%% (%.2f)",
                        grade.getFinalGrade(),
                        grade.getNumericalGrade()));

        concernTableModel.setValueAt(
                "Under Review", modelRow, 5);

        revisionStatusLabel.setText(
                "Under Review - Not Calculated");

    } catch (java.sql.SQLException
            | IllegalStateException exception) {

        revisionCalculated = false;
        pendingConcernId = -1;
        selectedConcernRow = -1;

        JOptionPane.showMessageDialog(
                this,
                "Unable to review concern:\n"
                        + exception.getMessage(),
                "Review Error",
                JOptionPane.ERROR_MESSAGE);
    }
}


    private double parseRevisionScore(JTextField field, String label) {

        double value;

        try {

            value = Double.parseDouble(field.getText().trim());

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(label + " must be a number.");

        }

        if (!Double.isFinite(value) || value < 0 || value > 100) {

            throw new IllegalArgumentException(label + " must be between 0 and 100.");

        }

        return value;

    }

    private static double numericalGradeFor(double percentage) {

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

    private void recalculateRevision() {

        revisionCalculated = false;

        pendingConcernId = -1;

        if (selectedConcernRow < 0) {

            JOptionPane.showMessageDialog(this, "Please review a concern first.");

            return;

        }

        try {

            int id = Integer.parseInt(concernTableModel.getValueAt(selectedConcernRow, 0).toString());

            String studentNumber = concernTableModel.getValueAt(selectedConcernRow, 1).toString();

            String courseCode = concernTableModel.getValueAt(selectedConcernRow, 3).toString();

            Grade grade = findGrade(studentNumber, courseCode);

            GradeConcern concern = findConcernById(id);

            if (grade == null || concern == null || "Revised".equals(concern.getStatus())) {

                throw new IllegalStateException("Concern or grade is no longer available for revision.");

            }

            pendingOutcome1 = parseRevisionScore(revisionExam1Field, "CO1");

            pendingOutcome2 = parseRevisionScore(revisionExam2Field, "CO2");

            pendingOutcome3 = parseRevisionScore(revisionExam3Field, "CO3");

            pendingFinalExam = parseRevisionScore(revisionFinalExamField, "Final Exam");

            pendingCoursera = parseRevisionScore(revisionCourseraField, "Coursera");

            // OLA is retained from the original grade; individual activity corrections

            // require a separate authorized workflow.

            pendingOLA = grade.getOLA();

            double preview = pendingOutcome1 * .15 + pendingOutcome2 * .15

                    + pendingOutcome3 * .15 + pendingFinalExam * .40

                    + pendingOLA * .10 + pendingCoursera * .05;

            double numerical = numericalGradeFor(preview);

            revisionFinalGradeLabel.setText(String.format("%.2f%% | %.2f | %s",

                    preview, numerical, numerical <= 3.00 ? "PASSED" : "FAILED"));

            revisionStatusLabel.setText("Under Review - Preview");

            pendingConcernId = id;

            revisionCalculated = true;

        } catch (IllegalArgumentException | IllegalStateException ex) {

            revisionFinalGradeLabel.setText("-");

            revisionStatusLabel.setText("Under Review - Invalid Preview");

            JOptionPane.showMessageDialog(this, ex.getMessage(), "Revision Error",

                    JOptionPane.WARNING_MESSAGE);

        }

    }

    private Course getSelectedCourseForRevision() {
        String code = revisionCourseLabel.getText().trim();
        for (Course c : AcademicData.getCourses()) {
            if (c.getCourseCode().equals(code)) return c;
        }
        return null;
    }

private void submitRevision() {

        if (!revisionCalculated || selectedConcernRow < 0 || pendingConcernId < 0) {

            JOptionPane.showMessageDialog(this, "Review and recalculate a concern first.");

            return;

        }

        try {

            if (parseRevisionScore(revisionExam1Field, "CO1") != pendingOutcome1

                    || parseRevisionScore(revisionExam2Field, "CO2") != pendingOutcome2

                    || parseRevisionScore(revisionExam3Field, "CO3") != pendingOutcome3

                    || parseRevisionScore(revisionFinalExamField, "Final Exam") != pendingFinalExam

                    || parseRevisionScore(revisionCourseraField, "Coursera") != pendingCoursera) {

                revisionCalculated = false;

                JOptionPane.showMessageDialog(this, "Scores changed. Recalculate first.");

                return;

            }

        } catch (IllegalArgumentException ex) {

            revisionCalculated = false;

            JOptionPane.showMessageDialog(this, ex.getMessage());

            return;

        }

        int currentId = Integer.parseInt(concernTableModel.getValueAt(selectedConcernRow, 0).toString());

        if (currentId != pendingConcernId) {

            revisionCalculated = false;

            JOptionPane.showMessageDialog(this, "Concern selection changed. Review again.");

            return;

        }

        String studentNumber = concernTableModel.getValueAt(selectedConcernRow, 1).toString();

        String courseCode = concernTableModel.getValueAt(selectedConcernRow, 3).toString();

        Grade grade = findGrade(studentNumber, courseCode);

        GradeConcern concern = findConcernById(pendingConcernId);

        if (grade == null || concern == null || "Revised".equals(concern.getStatus())

                || grade.getOLA() != pendingOLA
                || !("Submitted".equals(grade.getStatus()) || "Revised".equals(grade.getStatus()))) {

            revisionCalculated = false;

            JOptionPane.showMessageDialog(this, "Grade or OLA changed. Review again.");

            return;

        }

        int confirmation = JOptionPane.showConfirmDialog(this,

                "Confirm this weighted grade revision? OLA will remain unchanged.",

                "Confirm Revision", JOptionPane.YES_NO_OPTION);


if (confirmation != JOptionPane.YES_OPTION) return;

try {
    // Teacher ID 6 is for the current isolated
    // integration test only.
    // Replace with the authenticated teacher ID
    // before production use.
	int teacherId = loggedInTeacherId;

    GradeRevisionRepository.submitRevision(
            teacherId,
            pendingConcernId,
            pendingOutcome1,
            pendingOutcome2,
            pendingOutcome3,
            pendingFinalExam,
            pendingCoursera
    );

    // MySQL has committed successfully.
    // Update the local objects only afterward.
    grade.setExam1(pendingOutcome1);
    grade.setExam2(pendingOutcome2);
    grade.setExam3(pendingOutcome3);
    grade.setFinalExam(pendingFinalExam);
    grade.setCoursera(pendingCoursera);
    grade.calculateFinalGrade();
    grade.setStatus("Revised");

    concern.setStatus("Revised");

    revisionCalculated = false;
    selectedConcernRow = -1;
    pendingConcernId = -1;

    loadGradeConcerns();
    loadGradesForSelectedCourse();

    revisionFinalGradeLabel.setText(
            String.format(
                    "%.2f%% | %.2f | %s",
                    grade.getFinalGrade(),
                    grade.getNumericalGrade(),
                    grade.getAcademicResult()
            )
    );

    revisionStatusLabel.setText("Revised");

    JOptionPane.showMessageDialog(
            this,
            "Grade revision saved successfully to MySQL!"
    );

} catch (java.sql.SQLException | RuntimeException exception) {

    revisionCalculated = false;
    pendingConcernId = -1;

    JOptionPane.showMessageDialog(
            this,
            "Grade revision failed:\n"
                    + exception.getMessage()
                    + "\n\nRefresh and review the concern again.",
            "Revision Error",
            JOptionPane.ERROR_MESSAGE
    );}
}



    // Authorized activity-level OLA correction through a reviewed grade concern.
    private void reviseOLAActivities() {
        if (selectedConcernRow < 0) {
            JOptionPane.showMessageDialog(this, "Review a grade concern first.");
            return;
        }
        int id = Integer.parseInt(concernTableModel.getValueAt(selectedConcernRow, 0).toString());
        GradeConcern concern = findConcernById(id);
        if (concern == null || !"Under Review".equals(concern.getStatus())) {
            JOptionPane.showMessageDialog(this, "An active reviewed concern is required.");
            return;
        }
        String studentNumber = concern.getStudent().getStudentNumber();
        String courseCode = concern.getCourse().getCourseCode();
        Grade grade = findGrade(studentNumber, courseCode);
        if (grade == null || !grade.isCalculationComplete()
                || !("Submitted".equals(grade.getStatus()) || "Revised".equals(grade.getStatus()))) {
            JOptionPane.showMessageDialog(this, "A submitted grade is required.");
            return;
        }
        List<CourseAssessment> records = AcademicData.getOLAAssessments(courseCode);
        if (records.isEmpty()) {
            JOptionPane.showMessageDialog(this, "This course has no OLA activities.");
            return;
        }
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Activity", "Maximum", "Weight (%)", "Revised Score"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 4; }
        };
        for (CourseAssessment record : records) {
            Assessment a = record.getAssessment();
            Double score = AcademicData.getAssessmentScore(record.getAssessmentId(), studentNumber);
            model.addRow(new Object[]{record.getAssessmentId(), a.getName(),
                    a.getMaximumScore(), a.getWeight(), score == null ? "" : score});
        }
        JTable activityTable = new JTable(model);
        activityTable.setRowHeight(28);
        JLabel previewLabel = new JLabel("Edit activity scores, then click Preview.");
        JButton previewButton = new JButton("Preview Revised Grade");
        JButton applyButton = new JButton("Confirm OLA Revision");
        applyButton.setEnabled(false);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(previewButton);
        buttons.add(applyButton);
        JPanel dialogPanel = new JPanel(new BorderLayout(8, 8));
        dialogPanel.add(new JLabel(studentNumber + " | " + courseCode
                + " | Original OLA: " + String.format("%.2f", grade.getOLA())), BorderLayout.NORTH);
        dialogPanel.add(new JScrollPane(activityTable), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(previewLabel, BorderLayout.NORTH);
        footer.add(buttons, BorderLayout.SOUTH);
        dialogPanel.add(footer, BorderLayout.SOUTH);
        JDialog dialog = new JDialog(this, "Revise OLA Activities", true);
        dialog.setContentPane(dialogPanel);
        dialog.setSize(780, 380);
        dialog.setLocationRelativeTo(this);
        final Map<Integer, Double>[] pending = new Map[]{null};
        // Any table edit invalidates the preview and requires recalculation.
        model.addTableModelListener(e -> {
            pending[0] = null;
            applyButton.setEnabled(false);
            previewLabel.setText("Scores changed. Preview again.");
        });
        previewButton.addActionListener(e -> {
            if (activityTable.isEditing() && !activityTable.getCellEditor().stopCellEditing()) return;
            try {
                Map<Integer, Double> proposed = new LinkedHashMap<>();
                boolean changed = false;
                for (int row = 0; row < model.getRowCount(); row++) {
                    int assessmentId = ((Number) model.getValueAt(row, 0)).intValue();
                    Object value = model.getValueAt(row, 4);
                    if (value == null || value.toString().trim().isEmpty()) {
                        throw new IllegalArgumentException("Every activity needs a score.");
                    }
                    double score = Double.parseDouble(value.toString().trim());
                    Double original = AcademicData.getAssessmentScore(assessmentId, studentNumber);
                    if (original == null || Double.compare(original, score) != 0) {
                        proposed.put(assessmentId, score);
                        changed = true;
                    }
                }
                if (!changed) throw new IllegalArgumentException("Change at least one activity score.");
                double ola = AcademicData.previewOLARevision(id, proposed);
                double finalPercent = grade.getExam1() * .15 + grade.getExam2() * .15
                        + grade.getExam3() * .15 + grade.getFinalExam() * .40
                        + ola * .10 + grade.getCoursera() * .05;
                double numerical = numericalGradeFor(finalPercent);
                previewLabel.setText(String.format("OLA: %.2f%% | Final: %.2f%% | %.2f | %s",
                        ola, finalPercent, numerical, numerical <= 3.00 ? "PASSED" : "FAILED"));
                pending[0] = proposed;
                applyButton.setEnabled(true);
            } catch (NumberFormatException ex) {
                pending[0] = null;
                applyButton.setEnabled(false);
                JOptionPane.showMessageDialog(dialog, "Enter valid numeric activity scores.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                pending[0] = null;
                applyButton.setEnabled(false);
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });
        applyButton.addActionListener(e -> {
            if (activityTable.isEditing() && !activityTable.getCellEditor().stopCellEditing()) return;
            if (pending[0] == null) {
                JOptionPane.showMessageDialog(dialog, "Preview the revised scores first.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Apply the previewed OLA correction to this student only?",
                    "Confirm OLA Revision", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                OLARevisionRepository.submitRevision(loggedInTeacherId, id, pending[0]);
                // Update local objects only after the database transaction commits.
                AcademicData.submitOLARevision(id, pending[0]);
                revisionCalculated = false;
                pendingConcernId = -1;
                selectedConcernRow = -1;
                revisionStatusLabel.setText("Revised (OLA activities)");
                revisionOLAField.setText(String.format("%.2f", grade.getOLA()));
                revisionFinalGradeLabel.setText(String.format("%.2f%% (%.2f)",
                        grade.getFinalGrade(), grade.getNumericalGrade()));
                loadGradeConcerns();
                loadGradesForSelectedCourse();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "OLA revision submitted successfully.");
            } catch (IllegalArgumentException | IllegalStateException | java.sql.SQLException ex) {
                pending[0] = null;
                applyButton.setEnabled(false);
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    private void logout() {

        int choice =

                JOptionPane.showConfirmDialog(

                        this,

                        "Are you sure you want to logout?",

                        "Logout",

                        JOptionPane.YES_NO_OPTION

                );

        if (choice == JOptionPane.YES_OPTION) {

            new Login().setVisible(true);

            dispose();

        }

    }


private void loadGradesForSelectedCourse() {

    tableModel.setRowCount(0);

    Object selection = subjectComboBox.getSelectedItem();

    if (selection == null) {
        return;
    }

    Course selectedCourse =
            getSelectedCourse(selection.toString());

    if (selectedCourse == null) {
        return;
    }

    // Synchronize grade statuses from MySQL so Registrar decisions are visible
    // even when the teacher remained logged in while the Registrar reviewed a class.
    try {
        for (GradeRepository.GradeRecord record : GradeRepository.loadAllGrades()) {
            if (!selectedCourse.getCourseCode().equals(record.courseCode)) continue;
            Grade local = findGrade(record.studentNumber, record.courseCode);
            if (local != null) local.setStatus(record.submissionStatus);
        }
    } catch (java.sql.SQLException exception) {
        JOptionPane.showMessageDialog(this,
                "Unable to refresh Registrar grade status from MySQL:\n" + exception.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Load active enrollments from MySQL.
    java.util.Set<String> enrolledStudents;

    try {
        enrolledStudents =
                GradeRepository.loadEnrolledStudentNumbers(
                        selectedCourse.getCourseCode()
                );

    } catch (java.sql.SQLException exception) {

        JOptionPane.showMessageDialog(
                this,
                "Unable to load active enrollments from MySQL:\n"
                        + exception.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );

        return;
    }

    // Create in-memory Draft records for newly enrolled students who have no grade yet.
    // Existing submitted/revised grades are never replaced.
    for (String number : enrolledStudents) {
        if (findGrade(number, selectedCourse.getCourseCode()) != null) continue;
        for (Student st : AcademicData.getStudents()) {
            if (number.equals(st.getStudentNumber())) {
                AcademicData.addGrade(new Grade(st, selectedCourse));
                break;
            }
        }
    }

    // Add only grades for the selected subject
    // and students actively enrolled in it.
    for (Grade grade : AcademicData.getGrades()) {

        if (!selectedCourse.getCourseCode().equals(
                grade.getCourse().getCourseCode())) {
            continue;
        }

        if (!enrolledStudents.contains(
                grade.getStudent().getStudentNumber())) {
            continue;
        }

        String studentNumber =
                grade.getStudent().getStudentNumber();

        String studentName =
                grade.getStudent().getName();

        String finalPercentage =
                grade.isCalculationComplete()
                        ? String.format("%.2f",
                                grade.getFinalGrade())
                        : "";

        String numericalGrade =
                grade.isCalculationComplete()
                        ? String.format("%.2f",
                                grade.getNumericalGrade())
                        : "";

        String academicResult =
                grade.isCalculationComplete()
                        ? grade.getAcademicResult()
                        : "";

        String ola =
                grade.isCalculationComplete()
                        ? String.format("%.2f",
                                grade.getOLA())
                        : "";

        tableModel.addRow(new Object[] {
                studentNumber,
                studentName,
                grade.getExam1(),
                grade.getExam2(),
                grade.getExam3(),
                grade.getFinalExam(),
                ola,
                grade.getCoursera(),
                finalPercentage,
                numericalGrade,
                academicResult,
                grade.getStatus()
        });
    }

    System.out.println(
            "Selected course: "
                    + selectedCourse.getCourseCode()
    );

    System.out.println(
            "Enrolled students: "
                    + enrolledStudents
    );

    System.out.println(
            "Displayed grade rows: "
                    + tableModel.getRowCount()
    );
}


    private Course getSelectedCourse(String selectedSubject) {
        for (Course course : AcademicData.getCourses()) {
            if (course.toString().equals(selectedSubject)) return course;
        }
        return null;
    }

    /** Reload assigned subjects and newly registered students without resetting existing grades. */
    private void refreshGradeManagementSubjects() {
        try {
            java.util.List<Course> databaseCourses = AcademicRepository.loadCurrentCourses();
            for (Course c : databaseCourses) {
                boolean exists = false;
                for (Course old : AcademicData.getCourses()) {
                    if (old.getCourseCode().equals(c.getCourseCode())) { exists = true; break; }
                }
                if (!exists) AcademicData.addCourse(c);
            }
            for (Student st : AcademicRepository.loadStudents()) {
                boolean exists = false;
                for (Student old : AcademicData.getStudents()) {
                    if (old.getStudentNumber().equals(st.getStudentNumber())) { exists = true; break; }
                }
                if (!exists) AcademicData.addStudent(st);
            }
            String previous = (String) subjectComboBox.getSelectedItem();
            java.util.List<TeacherClassReportRepository.Subject> assigned =
                    TeacherClassReportRepository.subjects(loggedInTeacherId);
            subjectComboBox.removeAllItems();
            for (TeacherClassReportRepository.Subject subject : assigned) {
                String label = subject.name;
                if (getSelectedCourse(label) != null) subjectComboBox.addItem(label);
            }
            if (previous != null) subjectComboBox.setSelectedItem(previous);
            if (subjectComboBox.getSelectedIndex() < 0 && subjectComboBox.getItemCount() > 0)
                subjectComboBox.setSelectedIndex(0);
            loadGradesForSelectedCourse();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Unable to refresh assigned subjects: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

private void loadGradeConcerns() {

    if (concernTableModel == null) {
        return;
    }

    try {
        List<GradeConcern> concerns =
                GradeConcernRepository.loadAllConcerns();

        // Only replace the displayed data after
        // the database query succeeds.
        databaseConcerns.clear();
        concernTableModel.setRowCount(0);

        for (GradeConcern concern : concerns) {

            databaseConcerns.put(
                    concern.getConcernId(),
                    concern
            );

            concernTableModel.addRow(new Object[] {
                    concern.getConcernId(),
                    concern.getStudent().getStudentNumber(),
                    concern.getStudent().getName(),
                    concern.getCourse().getCourseCode(),
                    concern.getReason(),
                    concern.getStatus()
            });
        }

        System.out.println(
                "Loaded " + concerns.size()
                + " teacher concerns from MySQL.");

    } catch (java.sql.SQLException
            | RuntimeException exception) {

        JOptionPane.showMessageDialog(
                this,
                "Unable to load teacher concerns:\n"
                        + exception.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}


private GradeConcern findConcernById(int concernId) {
    return databaseConcerns.get(concernId);
}

}