/**
 * GradeRepository.java
 * Purpose: Grade database operations.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.sql.Types;
import java.sql.Timestamp;

public final class GradeRepository {

    private GradeRepository() {
    }
    

/**
 * Retrieves the student numbers of students
 * actively enrolled in a particular subject.
 */
public static Set<String> loadEnrolledStudentNumbers(
        String courseCode
) throws SQLException {

    String sql =
        "SELECT DISTINCT u.StudentNumber " +
        "FROM Enrollments e " +
        "JOIN Users u ON e.StudentID = u.UserID " +
        "JOIN Subjects s ON e.SubjectID = s.SubjectID " +
        "WHERE s.SubjectCode = ? " +
        "AND e.Status = 'Enrolled' " +
        "AND u.Role = 'Student'";

    Set<String> enrolledStudents =
        new HashSet<>();

    try (
        Connection connection =
            DatabaseConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)
    ) {

        statement.setString(1, courseCode);

        try (
            ResultSet result =
                statement.executeQuery()
        ) {

            while (result.next()) {

                enrolledStudents.add(
                    result.getString("StudentNumber")
                );
            }
        }
    }

    return enrolledStudents;
}


    // Represents one grade retrieved from MySQL.
    public static final class GradeRecord {

        public final String studentNumber;
        public final String courseCode;

        public final double co1;
        public final double co2;
        public final double co3;
        public final double finalExam;
        public final double ola;
        public final double coursera;

        public final double finalPercentage;
        public final double numericalGrade;

        public final boolean calculationComplete;
        public final String submissionStatus;

        public GradeRecord(ResultSet result)
                throws SQLException {

            studentNumber =
                result.getString("StudentNumber");

            courseCode =
                result.getString("SubjectCode");

            co1 = result.getDouble("CO1");
            co2 = result.getDouble("CO2");
            co3 = result.getDouble("CO3");

            finalExam =
                result.getDouble("FinalExam");

            ola = result.getDouble("OLA");

            coursera =
                result.getDouble("Coursera");

            finalPercentage =
                result.getDouble("FinalPercentage");

            numericalGrade =
                result.getDouble("NumericalGrade");

            calculationComplete =
                result.getBoolean("CalculationComplete");

            submissionStatus =
                result.getString("SubmissionStatus");
        }

        public double calculatePercentage() {

            return co1 * 0.15
                 + co2 * 0.15
                 + co3 * 0.15
                 + finalExam * 0.40
                 + ola * 0.10
                 + coursera * 0.05;
        }
    }

    // Retrieve all grades stored in MySQL.
    public static List<GradeRecord> loadAllGrades()
            throws SQLException {

        String sql =
            "SELECT u.StudentNumber, " +
            "s.SubjectCode, " +
            "g.CO1, g.CO2, g.CO3, " +
            "g.FinalExam, g.OLA, g.Coursera, " +
            "g.FinalPercentage, g.NumericalGrade, " +
            "g.CalculationComplete, " +
            "g.SubmissionStatus " +
            "FROM Grades g " +
            "JOIN Users u ON g.StudentID = u.UserID " +
            "JOIN Subjects s ON g.SubjectID = s.SubjectID " +
            "ORDER BY u.StudentNumber, s.SubjectCode";

        List<GradeRecord> records =
            new ArrayList<>();

        try (
            Connection connection =
                DatabaseConnection.getConnection();

            PreparedStatement statement =
                connection.prepareStatement(sql);

            ResultSet result =
                statement.executeQuery()
        ) {

            while (result.next()) {

                records.add(
                    new GradeRecord(result)
                );
            }
        }

        return records;
    }

    // Retrieve grades for one student.
    public static List<GradeRecord> loadStudentGrades(
            String studentNumber
    ) throws SQLException {

        String sql =
            "SELECT u.StudentNumber, " +
            "s.SubjectCode, " +
            "g.CO1, g.CO2, g.CO3, " +
            "g.FinalExam, g.OLA, g.Coursera, " +
            "g.FinalPercentage, g.NumericalGrade, " +
            "g.CalculationComplete, " +
            "g.SubmissionStatus " +
            "FROM Grades g " +
            "JOIN Users u ON g.StudentID = u.UserID " +
            "JOIN Subjects s ON g.SubjectID = s.SubjectID " +
            "WHERE u.StudentNumber = ? " +
            "ORDER BY s.SubjectCode";

        List<GradeRecord> records =
            new ArrayList<>();

        try (
            Connection connection =
                DatabaseConnection.getConnection();

            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {

            statement.setString(
                1, studentNumber
            );

            try (
                ResultSet result =
                    statement.executeQuery()
            ) {

                while (result.next()) {

                    records.add(
                        new GradeRecord(result)
                    );
                }
            }
        }

        return records;
    }

/**
 * Saves a completed draft grade to MySQL.
 *
 * Existing Submitted or Revised records
 * cannot be overwritten by this method.
 *
 * Authorized OLA revisions will use a
 * separate transaction in a later phase.
 */
public static void saveDraftGrade(Grade grade)
        throws SQLException {

    if (grade == null) {
        throw new IllegalArgumentException(
            "Grade cannot be null."
        );
    }

    if (!"Draft".equals(grade.getStatus())) {
        throw new IllegalStateException(
            "Only draft grades can be saved "
            + "using this method."
        );
    }

    if (!grade.isCalculationComplete()) {
        throw new IllegalStateException(
            "Calculate the grade before saving."
        );
    }

    String studentNumber =
        grade.getStudent().getStudentNumber();

    String courseCode =
        grade.getCourse().getCourseCode();

    String findExisting =
        "SELECT g.GradeID, g.SubmissionStatus " +
        "FROM Grades g " +
        "JOIN Users u ON g.StudentID = u.UserID " +
        "JOIN Subjects s ON g.SubjectID = s.SubjectID " +
        "WHERE u.StudentNumber = ? " +
        "AND s.SubjectCode = ? " +
        "FOR UPDATE";

    String findEnrollment =
        "SELECT u.UserID, s.SubjectID " +
        "FROM Enrollments e " +
        "JOIN Users u ON e.StudentID = u.UserID " +
        "JOIN Subjects s ON e.SubjectID = s.SubjectID " +
        "WHERE u.StudentNumber = ? " +
        "AND s.SubjectCode = ? " +
        "AND e.Status = 'Enrolled'";

    String insertGrade =
        "INSERT INTO Grades (" +
        "StudentID, SubjectID, " +
        "CO1, CO2, CO3, FinalExam, " +
        "OLA, Coursera, FinalPercentage, " +
        "NumericalGrade, CalculationComplete, " +
        "SubmissionStatus) " +
        "VALUES (?, ?, ?, ?, ?, ?, " +
        "?, ?, ?, ?, TRUE, 'Draft')";

    String updateGrade =
        "UPDATE Grades SET " +
        "CO1 = ?, CO2 = ?, CO3 = ?, " +
        "FinalExam = ?, OLA = ?, Coursera = ?, " +
        "FinalPercentage = ?, NumericalGrade = ?, " +
        "CalculationComplete = TRUE " +
        "WHERE GradeID = ? " +
        "AND SubmissionStatus IN ('Draft','Returned')";

    try (Connection connection =
            DatabaseConnection.getConnection()) {

        connection.setAutoCommit(false);

        try {
            Integer existingGradeId = null;

            // Lock an existing grade before checking
            // whether it is safe to modify.
            try (PreparedStatement statement =
                    connection.prepareStatement(
                        findExisting
                    )) {

                statement.setString(
                    1, studentNumber
                );

                statement.setString(
                    2, courseCode
                );

                try (ResultSet result =
                        statement.executeQuery()) {

                    if (result.next()) {

                        String status =
                            result.getString(
                                "SubmissionStatus"
                            );

                        if (!("Draft".equals(status) || "Returned".equals(status))) {
                            throw new SQLException(
                                "This grade is already "
                                + status
                                + " in MySQL. "
                                + "Ordinary saving is blocked."
                            );
                        }

                        existingGradeId =
                            result.getInt("GradeID");
                    }
                }
            }

            int studentId;
            int subjectId;

            // A grade can only be saved for a
            // student currently enrolled in
            // the corresponding subject.
            try (PreparedStatement statement =
                    connection.prepareStatement(
                        findEnrollment
                    )) {

                statement.setString(
                    1, studentNumber
                );

                statement.setString(
                    2, courseCode
                );

                try (ResultSet result =
                        statement.executeQuery()) {

                    if (!result.next()) {
                        throw new SQLException(
                            "No active enrollment for "
                            + studentNumber
                            + " in "
                            + courseCode
                        );
                    }

                    studentId =
                        result.getInt("UserID");

                    subjectId =
                        result.getInt("SubjectID");
                }
            }

            if (existingGradeId == null) {

                try (PreparedStatement statement =
                        connection.prepareStatement(
                            insertGrade
                        )) {

                    statement.setInt(
                        1, studentId
                    );

                    statement.setInt(
                        2, subjectId
                    );

                    setGradeValues(
                        statement, grade, 3
                    );

                    statement.executeUpdate();
                }

            } else {

                try (PreparedStatement statement =
                        connection.prepareStatement(
                            updateGrade
                        )) {

                    setGradeValues(
                        statement, grade, 1
                    );

                    statement.setInt(
                        9, existingGradeId
                    );

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                            "Draft grade update failed."
                        );
                    }
                }
            }

            connection.commit();

        } catch (SQLException
                | RuntimeException exception) {

            connection.rollback();
            throw exception;
        }
    }
}

/**
 * Binds the eight grade values:
 * six components and two calculated results.
 */
private static void setGradeValues(
        PreparedStatement statement,
        Grade grade,
        int startIndex
) throws SQLException {

    statement.setDouble(
        startIndex,
        grade.getExam1()
    );

    statement.setDouble(
        startIndex + 1,
        grade.getExam2()
    );

    statement.setDouble(
        startIndex + 2,
        grade.getExam3()
    );

    statement.setDouble(
        startIndex + 3,
        grade.getFinalExam()
    );

    statement.setDouble(
        startIndex + 4,
        grade.getOLA()
    );

    statement.setDouble(
        startIndex + 5,
        grade.getCoursera()
    );

    statement.setDouble(
        startIndex + 6,
        grade.getFinalGrade()
    );

    statement.setDouble(
        startIndex + 7,
        grade.getNumericalGrade()
    );
}


/**
 * Atomically submits one or more completed draft grades.
 *
 * All grades must belong to actively enrolled students.
 * Submitted and Revised database records cannot be
 * overwritten by ordinary submission.
 */
public static void submitGrades(
        List<Grade> gradesToSubmit
) throws SQLException {

    if (gradesToSubmit == null
            || gradesToSubmit.isEmpty()) {
        throw new IllegalArgumentException(
            "No grades were selected."
        );
    }

    // Validate Java objects before opening a transaction.
    Set<String> uniqueGrades = new HashSet<>();

    for (Grade grade : gradesToSubmit) {

        if (grade == null
                || grade.getStudent() == null
                || grade.getCourse() == null) {
            throw new IllegalArgumentException(
                "Invalid grade record."
            );
        }

        if (!("Draft".equals(grade.getStatus())
                || "Returned".equals(grade.getStatus()))) {
            throw new IllegalStateException(
                "Only Draft or Returned grades can be submitted."
            );
        }

        if (!grade.isCalculationComplete()) {
            throw new IllegalStateException(
                "Incomplete grade: "
                + grade.getStudent().getStudentNumber()
            );
        }

        String key =
            grade.getStudent().getStudentNumber()
            + ":"
            + grade.getCourse().getCourseCode();

        if (!uniqueGrades.add(key)) {
            throw new IllegalArgumentException(
                "Duplicate grade: " + key
            );
        }
    }

    String findEnrollment =
        "SELECT u.UserID, s.SubjectID " +
        "FROM Enrollments e " +
        "JOIN Users u ON e.StudentID = u.UserID " +
        "JOIN Subjects s ON e.SubjectID = s.SubjectID " +
        "WHERE u.StudentNumber = ? " +
        "AND s.SubjectCode = ? " +
        "AND e.Status = 'Enrolled'";

    String findGrade =
        "SELECT GradeID, SubmissionStatus " +
        "FROM Grades " +
        "WHERE StudentID = ? AND SubjectID = ? " +
        "FOR UPDATE";

    String insertGrade =
        "INSERT INTO Grades (" +
        "StudentID, SubjectID, CO1, CO2, CO3, " +
        "FinalExam, OLA, Coursera, " +
        "FinalPercentage, NumericalGrade, " +
        "CalculationComplete, SubmissionStatus" +
        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, " +
        "?, ?, TRUE, 'Submitted')";

    String updateGrade =
        "UPDATE Grades SET " +
        "CO1 = ?, CO2 = ?, CO3 = ?, " +
        "FinalExam = ?, OLA = ?, Coursera = ?, " +
        "FinalPercentage = ?, NumericalGrade = ?, " +
        "CalculationComplete = TRUE, " +
        "SubmissionStatus = 'Submitted' " +
        "WHERE GradeID = ? " +
        "AND SubmissionStatus IN ('Draft','Returned')";

    try (Connection connection =
            DatabaseConnection.getConnection()) {

        connection.setAutoCommit(false);

        try {
            for (Grade grade : gradesToSubmit) {

                String studentNumber =
                    grade.getStudent()
                         .getStudentNumber();

                String courseCode =
                    grade.getCourse()
                         .getCourseCode();

                int studentId;
                int subjectId;

                // Verify active enrollment.
                try (PreparedStatement statement =
                        connection.prepareStatement(
                            findEnrollment
                        )) {

                    statement.setString(
                        1, studentNumber
                    );

                    statement.setString(
                        2, courseCode
                    );

                    try (ResultSet result =
                            statement.executeQuery()) {

                        if (!result.next()) {
                            throw new SQLException(
                                "Student "
                                + studentNumber
                                + " is not actively enrolled in "
                                + courseCode
                            );
                        }

                        studentId =
                            result.getInt("UserID");

                        subjectId =
                            result.getInt("SubjectID");
                    }
                }

                Integer existingId = null;

                // Lock and inspect the existing grade.
                try (PreparedStatement statement =
                        connection.prepareStatement(
                            findGrade
                        )) {

                    statement.setInt(
                        1, studentId
                    );

                    statement.setInt(
                        2, subjectId
                    );

                    try (ResultSet result =
                            statement.executeQuery()) {

                        if (result.next()) {

                            String status =
                                result.getString(
                                    "SubmissionStatus"
                                );

                            if (!("Draft".equals(status) || "Returned".equals(status))) {
                                throw new SQLException(
                                    "Protected grade: "
                                    + studentNumber
                                    + " / "
                                    + courseCode
                                    + " is already "
                                    + status
                                );
                            }

                            existingId =
                                result.getInt("GradeID");
                        }
                    }
                }

                if (existingId == null) {

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                insertGrade
                            )) {

                        statement.setInt(
                            1, studentId
                        );

                        statement.setInt(
                            2, subjectId
                        );

                        setGradeValues(
                            statement, grade, 3
                        );

                        if (statement.executeUpdate()
                                != 1) {
                            throw new SQLException(
                                "Grade insertion failed."
                            );
                        }
                    }

                } else {

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                updateGrade
                            )) {

                        setGradeValues(
                            statement, grade, 1
                        );

                        statement.setInt(
                            9, existingId
                        );

                        if (statement.executeUpdate()
                                != 1) {
                            throw new SQLException(
                                "Grade update failed."
                            );
                        }
                    }
                }
            }

            // Commit only after every grade succeeds.
            connection.commit();

        } catch (SQLException
                | RuntimeException exception) {

            connection.rollback();
            throw exception;
        }
    }
}




/** Whole-class submission to Registrar. */
public static void submitClassToRegistrar(
        List<Grade> gradesToSubmit,
        int teacherId,
        String subjectCode
) throws SQLException {

    if (teacherId <= 0) throw new IllegalArgumentException("Valid teacher ID is required.");
    if (subjectCode == null || subjectCode.trim().isEmpty()) throw new IllegalArgumentException("Subject code is required.");

    if (gradesToSubmit == null
            || gradesToSubmit.isEmpty()) {
        throw new IllegalArgumentException(
            "No grades were selected."
        );
    }

    // Validate Java objects before opening a transaction.
    Set<String> uniqueGrades = new HashSet<>();

    for (Grade grade : gradesToSubmit) {

        if (grade == null
                || grade.getStudent() == null
                || grade.getCourse() == null) {
            throw new IllegalArgumentException(
                "Invalid grade record."
            );
        }

        if (!("Draft".equals(grade.getStatus())
                || "Returned".equals(grade.getStatus()))) {
            throw new IllegalStateException(
                "Only Draft or Returned grades can be submitted."
            );
        }

        if (!grade.isCalculationComplete()) {
            throw new IllegalStateException(
                "Incomplete grade: "
                + grade.getStudent().getStudentNumber()
            );
        }

        String key =
            grade.getStudent().getStudentNumber()
            + ":"
            + grade.getCourse().getCourseCode();

        if (!uniqueGrades.add(key)) {
            throw new IllegalArgumentException(
                "Duplicate grade: " + key
            );
        }
    }

    String findEnrollment =
        "SELECT u.UserID, s.SubjectID " +
        "FROM Enrollments e " +
        "JOIN Users u ON e.StudentID = u.UserID " +
        "JOIN Subjects s ON e.SubjectID = s.SubjectID " +
        "WHERE u.StudentNumber = ? " +
        "AND s.SubjectCode = ? " +
        "AND e.Status = 'Enrolled'";

    String findGrade =
        "SELECT GradeID, SubmissionStatus " +
        "FROM Grades " +
        "WHERE StudentID = ? AND SubjectID = ? " +
        "FOR UPDATE";

    String insertGrade =
        "INSERT INTO Grades (" +
        "StudentID, SubjectID, CO1, CO2, CO3, " +
        "FinalExam, OLA, Coursera, " +
        "FinalPercentage, NumericalGrade, " +
        "CalculationComplete, SubmissionStatus" +
        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, " +
        "?, ?, TRUE, 'Pending')";

    String updateGrade =
        "UPDATE Grades SET " +
        "CO1 = ?, CO2 = ?, CO3 = ?, " +
        "FinalExam = ?, OLA = ?, Coursera = ?, " +
        "FinalPercentage = ?, NumericalGrade = ?, " +
        "CalculationComplete = TRUE, " +
        "SubmissionStatus = 'Pending' " +
        "WHERE GradeID = ? " +
        "AND SubmissionStatus IN ('Draft','Returned')";

    try (Connection connection =
            DatabaseConnection.getConnection()) {

        connection.setAutoCommit(false);

        try {
            for (Grade grade : gradesToSubmit) {

                String studentNumber =
                    grade.getStudent()
                         .getStudentNumber();

                String courseCode =
                    grade.getCourse()
                         .getCourseCode();

                int studentId;
                int subjectId;

                // Verify active enrollment.
                try (PreparedStatement statement =
                        connection.prepareStatement(
                            findEnrollment
                        )) {

                    statement.setString(
                        1, studentNumber
                    );

                    statement.setString(
                        2, courseCode
                    );

                    try (ResultSet result =
                            statement.executeQuery()) {

                        if (!result.next()) {
                            throw new SQLException(
                                "Student "
                                + studentNumber
                                + " is not actively enrolled in "
                                + courseCode
                            );
                        }

                        studentId =
                            result.getInt("UserID");

                        subjectId =
                            result.getInt("SubjectID");
                    }
                }

                Integer existingId = null;

                // Lock and inspect the existing grade.
                try (PreparedStatement statement =
                        connection.prepareStatement(
                            findGrade
                        )) {

                    statement.setInt(
                        1, studentId
                    );

                    statement.setInt(
                        2, subjectId
                    );

                    try (ResultSet result =
                            statement.executeQuery()) {

                        if (result.next()) {

                            String status =
                                result.getString(
                                    "SubmissionStatus"
                                );

                            if (!("Draft".equals(status) || "Returned".equals(status))) {
                                throw new SQLException(
                                    "Protected grade: "
                                    + studentNumber
                                    + " / "
                                    + courseCode
                                    + " is already "
                                    + status
                                );
                            }

                            existingId =
                                result.getInt("GradeID");
                        }
                    }
                }

                if (existingId == null) {

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                insertGrade
                            )) {

                        statement.setInt(
                            1, studentId
                        );

                        statement.setInt(
                            2, subjectId
                        );

                        setGradeValues(
                            statement, grade, 3
                        );

                        if (statement.executeUpdate()
                                != 1) {
                            throw new SQLException(
                                "Grade insertion failed."
                            );
                        }
                    }

                } else {

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                updateGrade
                            )) {

                        setGradeValues(
                            statement, grade, 1
                        );

                        statement.setInt(
                            9, existingId
                        );

                        if (statement.executeUpdate()
                                != 1) {
                            throw new SQLException(
                                "Grade update failed."
                            );
                        }
                    }
                }
            }

            // Prevent duplicate active class submissions.
            try (PreparedStatement check = connection.prepareStatement(
                    "SELECT SubmissionID FROM Grade_Submissions WHERE TeacherID=? AND SubjectCode=? AND Status='Pending' FOR UPDATE")) {
                check.setInt(1, teacherId);
                check.setString(2, subjectCode);
                try (ResultSet r = check.executeQuery()) {
                    if (r.next()) throw new SQLException("This class already has a Pending submission.");
                }
            }

            // Create one submission record for the entire class.
            try (PreparedStatement create = connection.prepareStatement(
                    "INSERT INTO Grade_Submissions (TeacherID, SubjectCode, Status, SubmittedAt) VALUES (?, ?, 'Pending', NOW())")) {
                create.setInt(1, teacherId);
                create.setString(2, subjectCode);
                if (create.executeUpdate() != 1) throw new SQLException("Class submission record was not created.");
            }

            // Commit grades and class submission together.
            connection.commit();

        } catch (SQLException
                | RuntimeException exception) {

            connection.rollback();
            throw exception;
        }
    }
}

/** Returns the latest Registrar return reason for a teacher and subject. */
public static String loadLatestRegistrarFeedback(int teacherId, String subjectCode)
        throws SQLException {
    String sql =
        "SELECT RegistrarComment, ReviewedAt FROM Grade_Submissions " +
        "WHERE TeacherID=? AND SubjectCode=? AND Status='Returned' " +
        "ORDER BY ReviewedAt DESC, SubmissionID DESC LIMIT 1";

    try (Connection c = DatabaseConnection.getConnection();
         PreparedStatement p = c.prepareStatement(sql)) {
        p.setInt(1, teacherId);
        p.setString(2, subjectCode);
        try (ResultSet r = p.executeQuery()) {
            if (!r.next()) return null;
            String comment = r.getString("RegistrarComment");
            Timestamp reviewed = r.getTimestamp("ReviewedAt");
            return "Status: Returned for Revision\n" +
                   "Reviewed: " + (reviewed == null ? "N/A" : reviewed.toString()) + "\n\n" +
                   "Registrar reason:\n" + (comment == null ? "" : comment);
        }
    }
}

}
