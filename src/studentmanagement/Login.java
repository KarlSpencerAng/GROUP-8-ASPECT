/**
 * Login.java
 * Purpose: Application entry point and login UI.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class Login extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    public Login() {

        setTitle("Student Academic Management System");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(
                new BorderLayout(10, 20)
        );

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        30, 40, 30, 40
                )
        );

        // =========================
        // TITLE
        // =========================

        JPanel titlePanel = new JPanel();

        titlePanel.setLayout(
                new BoxLayout(
                        titlePanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel titleLabel =
                new JLabel(
                        "Student Academic Management System"
                );

        titleLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        19
                )
        );

        titleLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        JLabel loginLabel =
                new JLabel("Login");

        loginLabel.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        16
                )
        );

        loginLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        titlePanel.add(titleLabel);

        titlePanel.add(
                Box.createVerticalStrut(10)
        );

        titlePanel.add(loginLabel);

        mainPanel.add(
                titlePanel,
                BorderLayout.NORTH
        );

        // =========================
        // LOGIN FORM
        // =========================

        JPanel formPanel =
                new JPanel(
                        new GridLayout(
                                4,
                                2,
                                10,
                                15
                        )
                );

        JLabel usernameLabel =
                new JLabel("Username:");

        usernameField =
                new JTextField();

        JLabel passwordLabel =
                new JLabel("Password:");

        passwordField =
                new JPasswordField();

        JLabel roleLabel =
                new JLabel("Login As:");

        roleComboBox =
                new JComboBox<>();

        roleComboBox.addItem("Student");
        roleComboBox.addItem("Teacher");
        roleComboBox.addItem("Registrar");

        JButton loginButton =
                new JButton("Login");

        JButton clearButton =
                new JButton("Clear");

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);

        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        formPanel.add(roleLabel);
        formPanel.add(roleComboBox);

        formPanel.add(clearButton);
        formPanel.add(loginButton);

        mainPanel.add(
                formPanel,
                BorderLayout.CENTER
        );

        // =========================
        // BUTTON ACTIONS
        // =========================

        loginButton.addActionListener(
                e -> login()
        );

        clearButton.addActionListener(
                e -> clearFields()
        );

        // Press ENTER to login
        getRootPane().setDefaultButton(
                loginButton
        );

        add(mainPanel);
    }

    // =========================
    // LOGIN METHOD
    // =========================


private void login() {
    System.out.println("MAIN LOGIN BUTTON CLICKED");

    String username = usernameField.getText().trim().toLowerCase(java.util.Locale.ROOT);
    String password = new String(passwordField.getPassword());
    String role = String.valueOf(roleComboBox.getSelectedItem());

    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter your username and password.",
                "Missing Information", JOptionPane.WARNING_MESSAGE);
        return;
    }

    final String authenticatedIdentity;
    try {
        System.out.println("MAIN LOGIN: Checking database account for " + username + " (" + role + ")");
        authenticatedIdentity = LoginRepository.authenticate(username, password, role);
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Could not connect to MySQL: " + ex.getMessage(),
                "Database Connection Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    if (authenticatedIdentity == null) {
        System.out.println("Authentication returned null for username: " + username + ", role: " + role);
        showLoginError();
        return;
    }
    System.out.println("Authenticated identity: " + authenticatedIdentity);

    if ("Student".equals(role)) {
        try {
            if (StudentAccountRepository.mustChangePassword(username)) {
                JPasswordField replacement = new JPasswordField();
                JPasswordField confirmation = new JPasswordField();
                JPanel prompt = new JPanel(new GridLayout(2, 2, 8, 8));
                prompt.add(new JLabel("New password (8+ characters):"));
                prompt.add(replacement);
                prompt.add(new JLabel("Confirm new password:"));
                prompt.add(confirmation);
                int result = JOptionPane.showConfirmDialog(this, prompt,
                        "Required First Login Password Change", JOptionPane.OK_CANCEL_OPTION);
                if (result != JOptionPane.OK_OPTION) return;
                char[] newPassword = replacement.getPassword();
                char[] confirmed = confirmation.getPassword();
                boolean same = java.util.Arrays.equals(newPassword, confirmed);
                java.util.Arrays.fill(confirmed, '\0');
                if (!same) {
                    java.util.Arrays.fill(newPassword, '\0');
                    JOptionPane.showMessageDialog(this, "Passwords do not match.");
                    return;
                }
                try {
                    StudentAccountRepository.changeInitialPassword(username, password, newPassword);
                } finally {
                    java.util.Arrays.fill(newPassword, '\0');
                }
                JOptionPane.showMessageDialog(this, "Password updated. Continue to your dashboard.");
            }
        } catch (SQLException | RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Password change failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Student loggedInStudent = null;
        for (Student student : AcademicData.getStudents()) {
            if (authenticatedIdentity.equals(student.getStudentNumber())) {
                loggedInStudent = student;
                break;
            }
        }
        if (loggedInStudent == null) {
            try {
                for (Student student : AcademicRepository.loadStudents()) {
                    if (authenticatedIdentity.equals(student.getStudentNumber())) {
                        AcademicData.addStudent(student);
                        loggedInStudent = student;
                        break;
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Unable to retrieve student information:\n" + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (loggedInStudent == null) {
            JOptionPane.showMessageDialog(this,
                    "Authenticated in MySQL, but student could not be loaded.",
                    "Student Data Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("Opening student dashboard for " + authenticatedIdentity);
        JOptionPane.showMessageDialog(this, "Welcome, " + loggedInStudent.getName() + "!");
        new StudentDashboard(loggedInStudent).setVisible(true);
        dispose();
        return;
    }

    if ("Registrar".equals(role)) {
        final int registrarId;
        try {
            registrarId = Integer.parseInt(authenticatedIdentity);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid registrar identity returned by MySQL.",
                    "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Registrar login successful!");
        new RegistrarDashboard(registrarId).setVisible(true);
        dispose();
        return;
    }

    if ("Teacher".equals(role)) {
        final int teacherId;
        try {
            teacherId = Integer.parseInt(authenticatedIdentity);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid teacher identity returned by MySQL.",
                    "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        System.out.println("Opening teacher dashboard for teacher ID " + teacherId);
        JOptionPane.showMessageDialog(this, "Teacher login successful!");
        new TeacherDashboard(teacherId).setVisible(true);
        dispose();
        return;
    }

    showLoginError();
}


    // =========================
    // LOGIN ERROR
    // =========================

    private void showLoginError() {

        JOptionPane.showMessageDialog(
                this,
                "Invalid username, password, or role.",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // =========================
    // CLEAR
    // =========================

    private void clearFields() {

        usernameField.setText("");
        passwordField.setText("");

        usernameField.requestFocus();
    }

    // =========================
    // MAIN
    // =========================

    public static void main(String[] args) {

        DataInitializer.initialize();

        SwingUtilities.invokeLater(() -> {

            Login login =
                    new Login();

            login.setVisible(true);
        });
    }
}
