/**
 * StudentDashboard.java
 * Purpose: Student-facing interface.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentDashboard extends JFrame {

    private JTabbedPane tabbedPane;
    private DefaultTableModel enrollmentTableModel;
    
    private JComboBox<String> concernCourseComboBox;
    private JTextArea concernReasonArea;
    private DefaultTableModel concernTableModel;

    private Student currentStudent;
    private DefaultTableModel gradesTableModel;
    private JPanel performancePanel;
    private CertificateOfMatriculationPanel cmPanel;

    public StudentDashboard(Student student) {

        this.currentStudent = student;

        setTitle("Student Dashboard");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // =========================
        // HEADER
        // =========================

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        );

        JLabel titleLabel = new JLabel("Student Dashboard");
        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 24)
        );

        JLabel welcomeLabel =
                new JLabel(
                        "Welcome, " + currentStudent.getName()
                );
        
        JButton logoutButton =
                new JButton("Logout");

        logoutButton.addActionListener(
                e -> logout()
        );
        
        JPanel userPanel =
                new JPanel(new FlowLayout(FlowLayout.RIGHT));

        userPanel.add(welcomeLabel);
        userPanel.add(logoutButton);

        headerPanel.add(
                titleLabel,
                BorderLayout.WEST
        );

        headerPanel.add(
                userPanel,
                BorderLayout.EAST
        );

        mainPanel.add(
                headerPanel,
                BorderLayout.NORTH
        );

        // =========================
        // TABS
        // =========================

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab(
                "Dashboard",
                createDashboardPanel()
        );

        tabbedPane.addTab(
                "My Grades",
                createGradesPanel()
        );

        tabbedPane.addTab("Advanced Grades",
                new AdvancedStudentGradesPanel(currentStudent.getStudentNumber()));

        tabbedPane.addTab(
                "Academic Performance",
                createPerformancePanel()
        );

        tabbedPane.addTab(
                "Schedule",
                createSchedulePanel()
        );

        cmPanel = new CertificateOfMatriculationPanel(currentStudent.getStudentNumber());
        tabbedPane.addTab(
                "Certificate of Matriculation",
                cmPanel
        );
        
        tabbedPane.addTab(
        	    "Curriculum",
        	    new CurriculumPanel(currentStudent)
        	);

        tabbedPane.addTab(
                "Grade Concerns",
                createConcernPanel()
        );
        
        tabbedPane.addTab(
        	    "My Enrollments",
        	    createEnrollmentsPanel()
        	);

        tabbedPane.addTab("Request Enrollment", new StudentEnrollmentPanel(currentStudent.getStudentId()));

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) loadStudentGrades();
            if (tabbedPane.getSelectedIndex() == 3) refreshPerformancePanel();
            if (tabbedPane.getSelectedIndex() == 5 && cmPanel != null) cmPanel.refresh();
            if (tabbedPane.getSelectedIndex() == 7) {
                refreshConcernCourses();
                loadGradeConcerns();
            }

            if (tabbedPane.getSelectedIndex() == 8) {
                loadStudentEnrollments();
            }
        });

        mainPanel.add(
                tabbedPane,
                BorderLayout.CENTER
        );

        AspectDashboardShell.install(this, tabbedPane, "Student");
    }

    // =========================
    // DASHBOARD
    // =========================

    private JPanel createDashboardPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        30, 30, 30, 30
                )
        );

        JLabel welcomeLabel =
                new JLabel("Welcome to your Academic Dashboard");

        welcomeLabel.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        JLabel informationLabel =
                new JLabel(
                        "View your grades, academic performance, schedule, and grade concerns."
                );

        panel.add(welcomeLabel);

        panel.add(Box.createVerticalStrut(15));

        panel.add(informationLabel);

        return panel;
    }

    // =========================
    // MY GRADES
    // =========================

    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel note = new JLabel(
                "Only Registrar-posted grades are official and visible. Pending, returned, and draft grades are hidden.");
        panel.add(note, BorderLayout.NORTH);

        String[] columns = {
                "Course Code", "Course Name", "CO1 (15%)", "CO2 (15%)",
                "CO3 (15%)", "Final Exam (40%)", "OLA (10%)",
                "Coursera (5%)", "Final %", "Numerical Grade",
                "Academic Result", "Status"
        };
        gradesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(gradesTableModel);
        table.setRowHeight(30);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(
                    i == 1 ? 290 : 135);
        }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh My Grades");
        refresh.addActionListener(e -> loadStudentGrades());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(refresh);
        panel.add(actions, BorderLayout.SOUTH);
        loadStudentGrades();
        return panel;
    }

    private void loadStudentGrades() {
        if (gradesTableModel == null) return;
        gradesTableModel.setRowCount(0);
        try {
            for (StudentAcademicGradesRepository.Row row :
                    StudentAcademicGradesRepository.load(currentStudent.getStudentNumber())) {
                gradesTableModel.addRow(new Object[] {
                        row.code, row.name,
                        formatScore(row.co1), formatScore(row.co2), formatScore(row.co3),
                        formatScore(row.finalExam), formatScore(row.ola), formatScore(row.coursera),
                        formatScore(row.percent), formatScore(row.universityGrade),
                        row.percent >= 70.0 ? "PASS" : "FAIL", "Posted"
                });
            }
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to refresh posted grades from MySQL:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatScore(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    // =========================
    // ACADEMIC PERFORMANCE
    // =========================

private JPanel createPerformancePanel() {
    performancePanel = new JPanel();
    performancePanel.setLayout(new BoxLayout(performancePanel, BoxLayout.Y_AXIS));
    performancePanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
    refreshPerformancePanel();
    return performancePanel;
}

private void refreshPerformancePanel() {
    if (performancePanel == null) return;
    performancePanel.removeAll();
    JLabel title = new JLabel("Academic Performance");
    title.setFont(new Font("Arial", Font.BOLD, 22));
    performancePanel.add(title);
    performancePanel.add(Box.createVerticalStrut(25));

    try {
        java.util.List<StudentAcademicGradesRepository.Row> rows =
                StudentAcademicGradesRepository.load(currentStudent.getStudentNumber());
        if (rows.isEmpty()) {
            performancePanel.add(new JLabel("No posted grades yet."));
        } else {
            double sum = 0.0, highest = -Double.MAX_VALUE, lowest = Double.MAX_VALUE;
            for (StudentAcademicGradesRepository.Row row : rows) {
                sum += row.percent;
                highest = Math.max(highest, row.percent);
                lowest = Math.min(lowest, row.percent);
            }
            performancePanel.add(new JLabel(String.format(java.util.Locale.US,
                    "Overall Running Average: %.2f%%", sum / rows.size())));
            performancePanel.add(Box.createVerticalStrut(10));
            performancePanel.add(new JLabel(String.format(java.util.Locale.US,
                    "Highest Grade: %.2f%%", highest)));
            performancePanel.add(Box.createVerticalStrut(10));
            performancePanel.add(new JLabel(String.format(java.util.Locale.US,
                    "Lowest Grade: %.2f%%", lowest)));
        }
    } catch (java.sql.SQLException ex) {
        performancePanel.add(new JLabel("Unable to load posted grades from MySQL."));
        JOptionPane.showMessageDialog(this,
                "Unable to refresh academic performance from MySQL:\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
    performancePanel.revalidate();
    performancePanel.repaint();
}

    // =========================
    // SCHEDULE
    // =========================

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel titleLabel = new JLabel("Class Schedule");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(titleLabel, BorderLayout.NORTH);

        DefaultTableModel scheduleModel = new DefaultTableModel(
                new String[]{"Course Code","Course","Day","Start Time","End Time","Room"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable scheduleTable = new JTable(scheduleModel);
        scheduleTable.setRowHeight(30);
        scheduleTable.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        try {
            for (CertificateOfMatriculationRepository.ScheduleRow r :
                    CertificateOfMatriculationRepository.loadOfficialEnrollment(currentStudent.getStudentNumber())) {
                scheduleModel.addRow(new Object[]{r.subjectCode, r.subjectName, r.day,
                        r.start, r.end, r.room.isEmpty() ? "TBA" : r.room});
            }
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Unable to load class schedule from MySQL:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return panel;
    }

    // =========================
    // GRADE CONCERNS
    // =========================

    private JPanel createConcernPanel() {

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
                new JLabel("Grade Concerns");

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 22)
        );

        panel.add(
                titleLabel,
                BorderLayout.NORTH
        );

        // =========================
        // CONCERN FORM
        // =========================

        JPanel formPanel = new JPanel();

        formPanel.setLayout(
                new BoxLayout(
                        formPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel courseLabel =
                new JLabel("Select Course:");

        concernCourseComboBox =
                new JComboBox<>();

        refreshConcernCourses();

        JLabel reasonLabel =
                new JLabel("Reason for Concern:");

        concernReasonArea =
                new JTextArea(5, 40);

        concernReasonArea.setLineWrap(true);
        concernReasonArea.setWrapStyleWord(true);

        JScrollPane reasonScrollPane =
                new JScrollPane(concernReasonArea);

        JButton submitConcernButton =
                new JButton("Submit Concern");

        formPanel.add(courseLabel);

        formPanel.add(
                Box.createVerticalStrut(5)
        );

        formPanel.add(concernCourseComboBox);

        formPanel.add(
                Box.createVerticalStrut(15)
        );

        formPanel.add(reasonLabel);

        formPanel.add(
                Box.createVerticalStrut(5)
        );

        formPanel.add(reasonScrollPane);

        formPanel.add(
                Box.createVerticalStrut(10)
        );

        formPanel.add(submitConcernButton);

        // =========================
        // CONCERN HISTORY
        // =========================

        String[] columns = {
                "Concern ID",
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

        JTable concernTable =
                new JTable(concernTableModel);

        concernTable.setRowHeight(30);

        concernTable.getTableHeader()
                .setReorderingAllowed(false);

        JScrollPane tableScrollPane =
                new JScrollPane(concernTable);

        // =========================
        // CENTER
        // =========================

        JPanel centerPanel =
                new JPanel(
                        new BorderLayout(10, 20)
                );

        centerPanel.add(
                formPanel,
                BorderLayout.NORTH
        );

        centerPanel.add(
                tableScrollPane,
                BorderLayout.CENTER
        );

        panel.add(
                centerPanel,
                BorderLayout.CENTER
        );

        // Submit action
        submitConcernButton.addActionListener(
                e -> submitGradeConcern()
        );
        
        loadGradeConcerns();
        return panel;
    }

    // =========================
    // MAIN
    // =========================

    public static void main(String[] args) {

        DataInitializer.initialize();

        SwingUtilities.invokeLater(() -> {

            Student student =
                    AcademicData.getStudents().get(0);

            new StudentDashboard(student).setVisible(true);
        });
    }

private void refreshConcernCourses() {
    if (concernCourseComboBox == null) return;
    Object old = concernCourseComboBox.getSelectedItem();
    concernCourseComboBox.removeAllItems();
    try {
        for (StudentAcademicGradesRepository.Row row :
                StudentAcademicGradesRepository.load(currentStudent.getStudentNumber())) {
            concernCourseComboBox.addItem(row.code + " - " + row.name);
        }
        if (old != null) concernCourseComboBox.setSelectedItem(old);
    } catch (java.sql.SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Unable to refresh posted courses from MySQL:\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void submitGradeConcern() {

    Object selection = concernCourseComboBox.getSelectedItem();

    if (selection == null) {
        JOptionPane.showMessageDialog(
                this, "Please select a course.");
        return;
    }

    String selectedCourse = selection.toString();
    String subjectCode = selectedCourse.split(" - ", 2)[0].trim();
    String reason = concernReasonArea.getText().trim();

    if (reason.isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "Please enter a reason for your grade concern.",
                "Missing Reason",
                JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        int concernId = GradeConcernRepository.submitConcern(
                currentStudent.getStudentNumber(),
                subjectCode,
                reason);

        loadGradeConcerns();
        concernReasonArea.setText("");

        JOptionPane.showMessageDialog(
                this,
                "Grade concern submitted successfully!\n"
                        + "Concern ID: " + concernId);

    } catch (java.sql.SQLException exception) {
        JOptionPane.showMessageDialog(
                this,
                "Unable to save concern to MySQL:\n"
                        + exception.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);

    } catch (IllegalArgumentException
            | IllegalStateException exception) {

        JOptionPane.showMessageDialog(
                this,
                exception.getMessage(),
                "Cannot Submit Concern",
                JOptionPane.WARNING_MESSAGE);
    }
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

private void loadGradeConcerns() {

    if (concernTableModel == null) {
        return;
    }

    try {
        java.util.List<GradeConcern> concerns =
                GradeConcernRepository.loadStudentConcerns(
                        currentStudent.getStudentNumber());

        concernTableModel.setRowCount(0);

        for (GradeConcern concern : concerns) {
            concernTableModel.addRow(new Object[] {
                    concern.getConcernId(),
                    concern.getCourse().getCourseCode(),
                    concern.getReason(),
                    concern.getStatus()
            });
        }

    } catch (java.sql.SQLException
            | RuntimeException exception) {

        JOptionPane.showMessageDialog(
                this,
                "Unable to load grade concerns:\n"
                        + exception.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}


private JPanel createEnrollmentsPanel() {

    JPanel panel = new JPanel(new BorderLayout(10, 10));

    panel.setBorder(
        BorderFactory.createEmptyBorder(20, 20, 20, 20)
    );

    JLabel title = new JLabel("My Current Enrollments");
    title.setFont(new Font("Arial", Font.BOLD, 22));

    JButton refreshButton = new JButton("Refresh from MySQL");

    refreshButton.addActionListener(
        e -> loadStudentEnrollments()
    );

    JPanel header = new JPanel(new BorderLayout());
    header.add(title, BorderLayout.WEST);
    header.add(refreshButton, BorderLayout.EAST);

    panel.add(header, BorderLayout.NORTH);

    String[] columns = {
        "Course Code",
        "Subject Name"
    };

    enrollmentTableModel = new DefaultTableModel(
        columns, 0
    ) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(
                int row, int column) {
            return false;
        }
    };

    JTable enrollmentTable = new JTable(
        enrollmentTableModel
    );

    enrollmentTable.setRowHeight(28);
    enrollmentTable.setFillsViewportHeight(true);

    panel.add(
        new JScrollPane(enrollmentTable),
        BorderLayout.CENTER
    );

    return panel;
}

private void loadStudentEnrollments() {

    if (enrollmentTableModel == null) {
        return;
    }

    try {

        // Read the current enrollment records from MySQL.
        List<Course> enrolledCourses =
            AcademicRepository.loadEnrolledCourses(
                currentStudent.getStudentNumber()
            );

        // Only replace displayed data after
        // the database query succeeds.
        enrollmentTableModel.setRowCount(0);

        for (Course course : enrolledCourses) {

            enrollmentTableModel.addRow(
                new Object[] {
                    course.getCourseCode(),
                    course.getCourseName()
                }
            );
        }

        System.out.println(
            "Loaded " + enrolledCourses.size()
            + " enrollments for "
            + currentStudent.getStudentNumber()
        );

    } catch (java.sql.SQLException exception) {

        JOptionPane.showMessageDialog(
            this,
            "Unable to load enrollments from MySQL:\n"
                + exception.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}

}