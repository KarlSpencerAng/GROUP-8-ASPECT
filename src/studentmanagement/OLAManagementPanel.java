/**
 * OLAManagementPanel.java
 * Purpose: OLA management interface.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class OLAManagementPanel extends JPanel {

    private JComboBox<Course> courseComboBox;
    private JComboBox<Student> studentComboBox;

    private JTable assessmentTable;
    private DefaultTableModel assessmentModel;

    private JTable scoreTable;
    private DefaultTableModel scoreModel;

    private JLabel weightLabel;
    private JLabel olaGradeLabel;
    private final int teacherId;

    public OLAManagementPanel(int teacherId) {
        this.teacherId = teacherId;

        setLayout(new BorderLayout(10, 10));

        setBorder(BorderFactory.createEmptyBorder(
                15, 15, 15, 15
        ));

        // COURSE AND STUDENT SELECTION

        JPanel selectionPanel =
                new JPanel(new FlowLayout(FlowLayout.LEFT));

        courseComboBox = new JComboBox<>();
        studentComboBox = new JComboBox<>();

        refreshSubjectsAndStudents();

        selectionPanel.add(new JLabel("Course:"));
        selectionPanel.add(courseComboBox);

        selectionPanel.add(new JLabel("Student:"));
        selectionPanel.add(studentComboBox);
        JButton refreshDataButton = new JButton("Refresh Data");
        refreshDataButton.addActionListener(e -> { refreshSubjectsAndStudents(); refreshAll(); });
        selectionPanel.add(refreshDataButton);

        add(selectionPanel, BorderLayout.NORTH);

        // ASSESSMENT TABLE

        assessmentModel = new DefaultTableModel(
                new String[]{
                        "ID",
                        "Assessment",
                        "Maximum Score",
                        "Weight (%)"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(
                    int row, int column) {
                return false;
            }
        };

        assessmentTable = new JTable(assessmentModel);
        assessmentTable.setRowHeight(28);

        JPanel assessmentPanel =
                new JPanel(new BorderLayout(5, 5));

        assessmentPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "OLA Assessments"
                )
        );

        assessmentPanel.add(
                new JScrollPane(assessmentTable),
                BorderLayout.CENTER
        );

        JPanel assessmentButtons =
                new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addButton =
                new JButton("Add Assessment");

        JButton editButton =
                new JButton("Edit Assessment");

        JButton deleteButton =
                new JButton("Delete Assessment");

        assessmentButtons.add(addButton);
        assessmentButtons.add(editButton);
        assessmentButtons.add(deleteButton);

        weightLabel = new JLabel("Total Weight: 0%");

        assessmentButtons.add(weightLabel);

        assessmentPanel.add(
                assessmentButtons,
                BorderLayout.SOUTH
        );

        // STUDENT SCORE TABLE

        scoreModel = new DefaultTableModel(
                new String[]{
                        "ID",
                        "Assessment",
                        "Maximum",
                        "Score",
                        "Weight (%)"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(
                    int row, int column) {
                return column == 3;
            }
        };

        scoreTable = new JTable(scoreModel);
        scoreTable.setRowHeight(28);

        JPanel scorePanel =
                new JPanel(new BorderLayout(5, 5));

        scorePanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Student OLA Scores"
                )
        );

        scorePanel.add(
                new JScrollPane(scoreTable),
                BorderLayout.CENTER
        );

        JButton saveScoresButton =
                new JButton("Save Scores");

        JButton calculateButton =
                new JButton("Calculate OLA");

        olaGradeLabel =
                new JLabel("OLA Grade: Not calculated");

        JPanel scoreButtons =
                new JPanel(new FlowLayout(FlowLayout.RIGHT));

        scoreButtons.add(olaGradeLabel);
        scoreButtons.add(saveScoresButton);
        scoreButtons.add(calculateButton);

        scorePanel.add(
                scoreButtons,
                BorderLayout.SOUTH
        );

        JSplitPane splitPane =
                new JSplitPane(
                        JSplitPane.VERTICAL_SPLIT,
                        assessmentPanel,
                        scorePanel
                );

        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);

        // EVENTS

        courseComboBox.addActionListener(e -> { refreshStudentsForSelectedCourse(); refreshAll(); });

        studentComboBox.addActionListener(
                e -> loadStudentScores()
        );

        addButton.addActionListener(
                e -> addAssessment()
        );

        editButton.addActionListener(
                e -> editAssessment()
        );

        deleteButton.addActionListener(
                e -> deleteAssessment()
        );

        saveScoresButton.addActionListener(
                e -> saveScores()
        );

        calculateButton.addActionListener(
                e -> calculateOLA()
        );

        refreshAll();
    }


    /** Reloads teacher subjects and the selected subject's live enrolled roster from MySQL. */
    public void refreshSubjectsAndStudents() {
        Course previous = selectedCourse();
        courseComboBox.removeAllItems();
        try {
            java.util.List<Course> dbCourses = AcademicRepository.loadCurrentCourses();
            java.util.List<TeacherClassReportRepository.Subject> assigned =
                    TeacherClassReportRepository.subjects(teacherId);
            java.util.Set<Integer> assignedIds = new java.util.HashSet<>();
            for (TeacherClassReportRepository.Subject subject : assigned) assignedIds.add(subject.id);
            for (Course course : dbCourses) {
                if (assignedIds.contains(course.getCourseId())) courseComboBox.addItem(course);
            }
            if (previous != null) {
                for (int i=0;i<courseComboBox.getItemCount();i++) {
                    if (courseComboBox.getItemAt(i).getCourseCode().equals(previous.getCourseCode())) {
                        courseComboBox.setSelectedIndex(i); break;
                    }
                }
            }
            if (courseComboBox.getSelectedIndex()<0 && courseComboBox.getItemCount()>0) courseComboBox.setSelectedIndex(0);
            refreshStudentsForSelectedCourse();
        } catch (SQLException ex) {
            showError("Unable to refresh OLA subjects/students from MySQL: " + ex.getMessage());
        }
    }

    private void refreshStudentsForSelectedCourse() {
        Course course = selectedCourse();
        String previousNumber = selectedStudent()==null ? null : selectedStudent().getStudentNumber();
        studentComboBox.removeAllItems();
        if (course == null) return;
        try {
            java.util.Set<String> enrolled = GradeRepository.loadEnrolledStudentNumbers(course.getCourseCode());
            for (Student student : AcademicRepository.loadStudents()) {
                if (enrolled.contains(student.getStudentNumber())) studentComboBox.addItem(student);
            }
            if (previousNumber != null) {
                for (int i=0;i<studentComboBox.getItemCount();i++) {
                    if (studentComboBox.getItemAt(i).getStudentNumber().equals(previousNumber)) {
                        studentComboBox.setSelectedIndex(i); break;
                    }
                }
            }
        } catch (SQLException ex) {
            showError("Unable to refresh enrolled students from MySQL: " + ex.getMessage());
        }
    }

    private Course selectedCourse() {
        return (Course) courseComboBox.getSelectedItem();
    }

    private Student selectedStudent() {
        return (Student) studentComboBox.getSelectedItem();
    }

    private void refreshAll() {
        loadAssessments();
        loadStudentScores();
        olaGradeLabel.setText(
                "OLA Grade: Not calculated"
        );
    }

    private void loadAssessments() {

        assessmentModel.setRowCount(0);

        Course course = selectedCourse();

        if (course == null) {
            return;
        }

        double totalWeight = 0;

        for (CourseAssessment record :
                AcademicData.getOLAAssessments(
                        course.getCourseCode())) {

            Assessment assessment =
                    record.getAssessment();

            assessmentModel.addRow(new Object[]{
                    record.getAssessmentId(),
                    assessment.getName(),
                    assessment.getMaximumScore(),
                    assessment.getWeight()
            });

            totalWeight += assessment.getWeight();
        }

        weightLabel.setText(
                String.format(
                        "Total Weight: %.2f%%",
                        totalWeight
                )
        );
    }

    private void loadStudentScores() {

        scoreModel.setRowCount(0);

        Course course = selectedCourse();
        Student student = selectedStudent();

        if (course == null || student == null) {
            return;
        }

        for (CourseAssessment record :
                AcademicData.getOLAAssessments(
                        course.getCourseCode())) {

            Double score =
                    AcademicData.getAssessmentScore(
                            record.getAssessmentId(),
                            student.getStudentNumber()
                    );

            scoreModel.addRow(new Object[]{
                    record.getAssessmentId(),
                    record.getAssessment().getName(),
                    record.getAssessment()
                            .getMaximumScore(),
                    score == null ? "" : score,
                    record.getAssessment().getWeight()
            });
        }

        olaGradeLabel.setText(
                "OLA Grade: Not calculated"
        );
    }

    private String[] assessmentInput(
            String title,
            CourseAssessment existing) {

        JTextField nameField = new JTextField();

        JTextField maximumField = new JTextField();

        JTextField weightField = new JTextField();

        if (existing != null) {

            Assessment assessment =
                    existing.getAssessment();

            nameField.setText(assessment.getName());

            maximumField.setText(String.valueOf(
                    assessment.getMaximumScore()
            ));

            weightField.setText(String.valueOf(
                    assessment.getWeight()
            ));
        }

        JPanel panel =
                new JPanel(new GridLayout(3, 2, 5, 5));

        panel.add(new JLabel("Assessment Name:"));
        panel.add(nameField);

        panel.add(new JLabel("Maximum Score:"));
        panel.add(maximumField);

        panel.add(new JLabel("Weight (%):"));
        panel.add(weightField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        return new String[]{
                nameField.getText(),
                maximumField.getText(),
                weightField.getText()
        };
    }

    private void reloadOLAFromDatabase()
            throws SQLException {

        OLARepository.OLAData data =
                OLARepository.loadAllOLA();

        AcademicData.loadDatabaseOLA(
                data.assessments,
                data.scores
        );

        refreshAll();
    }
    

private void addAssessment() {

    Course course = selectedCourse();

    if (course == null) {
        showError("Please select a course.");
        return;
    }

    String[] input = assessmentInput(
            "Add OLA Assessment",
            null
    );

    if (input == null) {
        return;
    }

    try {

        String name = input[0].trim();

        double maximumScore =
                Double.parseDouble(
                        input[1].trim()
                );

        double weight =
                Double.parseDouble(
                        input[2].trim()
                );

        // Validate the assessment before
        // attempting any database operation.

        new Assessment(
                name,
                maximumScore,
                weight
        );

        // Save permanently to MySQL.

        OLARepository.createAssessment(
                course.getCourseCode(),
                name,
                maximumScore,
                weight
        );

        // Reload the latest assessment IDs,
        // definitions and student scores.

        reloadOLAFromDatabase();

        JOptionPane.showMessageDialog(
                this,
                "Assessment successfully saved "
                + "to the database.",
                "OLA Management",
                JOptionPane.INFORMATION_MESSAGE
        );

    } catch (NumberFormatException e) {

        showError(
                "Maximum Score and Weight "
                + "must be valid numbers."
        );

    } catch (IllegalArgumentException |
             IllegalStateException |
             SQLException e) {

        showError(e.getMessage());
    }
}



private void editAssessment() {

    int selectedRow = assessmentTable.getSelectedRow();

    if (selectedRow == -1) {
        showError("Select an assessment first.");
        return;
    }

    // Convert the selected table row to its model index.
    int row = assessmentTable.convertRowIndexToModel(
            selectedRow
    );

    int id = Integer.parseInt(
            assessmentModel.getValueAt(row, 0).toString()
    );

    CourseAssessment record =
            AcademicData.findOLAAssessment(id);

    if (record == null) {
        showError("Assessment not found. Refresh and try again.");
        return;
    }

    String[] input = assessmentInput(
            "Edit OLA Assessment",
            record
    );

    if (input == null) {
        return;
    }

    try {

        String name = input[0].trim();

        double maximumScore =
                Double.parseDouble(input[1].trim());

        double weight =
                Double.parseDouble(input[2].trim());

        // Validate before attempting the database update.
        new Assessment(
                name,
                maximumScore,
                weight
        );

        // Save the changes permanently to MySQL.
        OLARepository.updateAssessment(
                id,
                name,
                maximumScore,
                weight
        );

        // Reload the latest data from MySQL.
        reloadOLAFromDatabase();

        JOptionPane.showMessageDialog(
                this,
                "Assessment successfully updated "
                + "in the database.",
                "OLA Management",
                JOptionPane.INFORMATION_MESSAGE
        );

    } catch (NumberFormatException e) {

        showError(
                "Maximum Score and Weight "
                + "must be valid numbers."
        );

    } catch (IllegalArgumentException |
             IllegalStateException |
             java.sql.SQLException e) {

        showError(e.getMessage());
    }
}



private void deleteAssessment() {

    int selectedRow = assessmentTable.getSelectedRow();

    if (selectedRow == -1) {
        showError("Select an assessment first.");
        return;
    }

    int row = assessmentTable.convertRowIndexToModel(
            selectedRow
    );

    int id = Integer.parseInt(
            assessmentModel.getValueAt(row, 0).toString()
    );

    String assessmentName =
            assessmentModel.getValueAt(row, 1).toString();

    int confirmation = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete \""
                    + assessmentName + "\"?\n\n"
                    + "This will permanently delete "
                    + "the assessment and all associated "
                    + "student scores.",
            "Confirm Assessment Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
    );

    if (confirmation != JOptionPane.YES_OPTION) {
        return;
    }

    try {

        // Delete permanently through the
        // repository's existing transaction.

        OLARepository.deleteAssessment(id);

        // Reload assessment definitions and
        // student scores from MySQL.

        reloadOLAFromDatabase();

        JOptionPane.showMessageDialog(
                this,
                "Assessment successfully deleted "
                        + "from the database.",
                "OLA Management",
                JOptionPane.INFORMATION_MESSAGE
        );

    } catch (java.sql.SQLException |
             IllegalArgumentException |
             IllegalStateException e) {

        showError(e.getMessage());
    }
}



private void saveScores() {

    // Finish editing the current table cell first.

    if (scoreTable.isEditing()
            && !scoreTable.getCellEditor()
                    .stopCellEditing()) {
        return;
    }

    Course course = selectedCourse();
    Student student = selectedStudent();

    if (course == null || student == null) {
        showError("Select a course and student first.");
        return;
    }

    try {

        Map<Integer, Double> scores =
                new LinkedHashMap<>();

        // Validate ALL scores before writing anything.

        for (int row = 0;
                row < scoreModel.getRowCount();
                row++) {

            int id = Integer.parseInt(
                    scoreModel.getValueAt(row, 0)
                            .toString()
            );

            CourseAssessment record =
                    AcademicData.findOLAAssessment(id);

            if (record == null) {
                throw new IllegalStateException(
                        "Assessment " + id
                        + " was not found."
                );
            }

            if (!record.getCourseCode().equals(
                    course.getCourseCode())) {

                throw new IllegalStateException(
                        "Assessment does not belong "
                        + "to the selected course."
                );
            }

            Object value =
                    scoreModel.getValueAt(row, 3);

            if (value == null
                    || value.toString()
                            .trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Every assessment requires "
                        + "a student score."
                );
            }

            double score = Double.parseDouble(
                    value.toString().trim()
            );

            // Preserve the existing validation rules.

            record.getAssessment()
                    .calculatePercentage(score);

            if (scores.put(id, score) != null) {
                throw new IllegalStateException(
                        "Duplicate assessment ID: " + id
                );
            }
        }

        if (scores.isEmpty()) {
            showError(
                    "This course has no assessments "
                    + "to save."
            );
            return;
        }

        // Save every score in ONE MySQL transaction.
        // The repository also checks enrollment,
        // assessment ownership and grade protection.

        OLARepository.saveStudentScores(
                student.getStudentNumber(),
                course.getCourseCode(),
                scores
        );

        // Refresh memory and the interface using
        // the permanently stored MySQL records.

        reloadOLAFromDatabase();

        olaGradeLabel.setText(
                "OLA Grade: Not calculated"
        );

        JOptionPane.showMessageDialog(
                this,
                "Student scores successfully "
                + "saved to the database.",
                "OLA Management",
                JOptionPane.INFORMATION_MESSAGE
        );

    } catch (NumberFormatException e) {

        showError(
                "Every score must be a valid number."
        );

    } catch (IllegalArgumentException |
             IllegalStateException |
             java.sql.SQLException e) {

        showError(e.getMessage());
    }
}


    private void calculateOLA() {

        Course course = selectedCourse();
        Student student = selectedStudent();

        if (course == null || student == null) {
            return;
        }

        try {

            double ola =
                    AcademicData.calculateStudentOLA(
                            student.getStudentNumber(),
                            course.getCourseCode()
                    );

            olaGradeLabel.setText(
                    String.format(
                            "OLA Grade: %.2f%%",
                            ola
                    )
            );

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            showError(e.getMessage());
        }
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "OLA Management",
                JOptionPane.WARNING_MESSAGE
        );
    }
}