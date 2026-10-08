/**
 * OLARepository.java
 * Purpose: OLA database operations.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OLARepository {

    private OLARepository() {
    }

    public static final class OLAData {

        public final List<CourseAssessment> assessments;

        public final Map<Integer, Map<String, Double>> scores;

        public OLAData(
                List<CourseAssessment> assessments,
                Map<Integer, Map<String, Double>> scores) {

            this.assessments = assessments;
            this.scores = scores;
        }
    }

    /**
     * Loads assessment definitions and existing
     * student scores from MySQL.
     *
     * This method does not modify the database.
     */
    public static OLAData loadAllOLA()
            throws SQLException {

        String assessmentSQL =
            "SELECT " +
            "a.AssessmentID, " +
            "s.SubjectCode, " +
            "a.AssessmentName, " +
            "a.MaximumScore, " +
            "a.Weight " +
            "FROM OLA_Assessments a " +
            "JOIN Subjects s " +
            "ON a.SubjectID = s.SubjectID " +
            "ORDER BY a.AssessmentID";

        String scoreSQL =
            "SELECT " +
            "os.AssessmentID, " +
            "u.StudentNumber, " +
            "os.Score " +
            "FROM OLA_Scores os " +
            "JOIN Users u " +
            "ON os.StudentID = u.UserID " +
            "ORDER BY os.AssessmentID, " +
            "u.StudentNumber";

        List<CourseAssessment> assessments =
            new ArrayList<>();

        Map<Integer, Map<String, Double>> scores =
            new HashMap<>();

        try (Connection connection =
                DatabaseConnection.getConnection()) {

            // Read both tables using one
            // consistent database snapshot.
            connection.setTransactionIsolation(
                Connection.TRANSACTION_REPEATABLE_READ
            );

            connection.setReadOnly(true);
            connection.setAutoCommit(false);

            try {

                try (PreparedStatement statement =
                        connection.prepareStatement(
                            assessmentSQL
                        );

                     ResultSet result =
                        statement.executeQuery()) {

                    while (result.next()) {

                        int id =
                            result.getInt("AssessmentID");

                        String courseCode =
                            result.getString("SubjectCode");

                        Assessment assessment =
                            new Assessment(
                                result.getString(
                                    "AssessmentName"
                                ),
                                result.getDouble(
                                    "MaximumScore"
                                ),
                                result.getDouble(
                                    "Weight"
                                )
                            );

                        CourseAssessment record =
                            new CourseAssessment(
                                id,
                                courseCode,
                                assessment
                            );

                        assessments.add(record);

                        scores.put(
                            id,
                            new HashMap<>()
                        );
                    }
                }

                try (PreparedStatement statement =
                        connection.prepareStatement(
                            scoreSQL
                        );

                     ResultSet result =
                        statement.executeQuery()) {

                    while (result.next()) {

                        int assessmentId =
                            result.getInt("AssessmentID");

                        String studentNumber =
                            result.getString(
                                "StudentNumber"
                            );

                        double score =
                            result.getDouble("Score");

                        Map<String, Double> studentScores =
                            scores.get(assessmentId);

                        if (studentScores == null) {
                            throw new SQLException(
                                "Score references an "
                                + "unknown assessment: "
                                + assessmentId
                            );
                        }

                        if (studentScores.putIfAbsent(
                                studentNumber,
                                score
                            ) != null) {

                            throw new SQLException(
                                "Duplicate OLA score: "
                                + assessmentId
                                + " / "
                                + studentNumber
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

        return new OLAData(
            assessments,
            scores
        );
    }
    

private static void checkCourseUnlocked(
        Connection connection,
        int subjectId
) throws SQLException {

    String sql =
        "SELECT GradeID " +
        "FROM Grades " +
        "WHERE SubjectID = ? " +
        "AND SubmissionStatus IN ('Submitted', 'Revised', 'Pending', 'Posted') " +
        "LIMIT 1 FOR UPDATE";

    try (PreparedStatement statement =
            connection.prepareStatement(sql)) {

        statement.setInt(1, subjectId);

        try (ResultSet result =
                statement.executeQuery()) {

            if (result.next()) {
                throw new SQLException(
                    "This subject has protected grades. Ordinary OLA "
                    + "assessment changes are blocked."
                );
            }
        }
    }
}

private static int findSubjectId(
        Connection connection,
        String courseCode
) throws SQLException {

    String sql =
        "SELECT SubjectID FROM Subjects " +
        "WHERE SubjectCode = ? FOR UPDATE";

    try (PreparedStatement statement =
            connection.prepareStatement(sql)) {

        statement.setString(1, courseCode);

        try (ResultSet result =
                statement.executeQuery()) {

            if (!result.next()) {
                throw new SQLException(
                    "Unknown subject: " + courseCode
                );
            }

            return result.getInt("SubjectID");
        }
    }
}


public static int createAssessment(
        String courseCode,
        String name,
        double maximumScore,
        double weight
) throws SQLException {

    // Reuse existing Java validation.
    Assessment validated =
        new Assessment(
            name,
            maximumScore,
            weight
        );

    String sql =
        "INSERT INTO OLA_Assessments " +
        "(SubjectID, AssessmentName, " +
        "MaximumScore, Weight) " +
        "VALUES (?, ?, ?, ?)";

    try (Connection connection =
            DatabaseConnection.getConnection()) {

        connection.setAutoCommit(false);

        try {

            int subjectId =
                findSubjectId(
                    connection,
                    courseCode
                );

            checkCourseUnlocked(
                connection,
                subjectId
            );

            int assessmentId;

            try (PreparedStatement statement =
                    connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                    )) {

                statement.setInt(1, subjectId);

                statement.setString(
                    2,
                    validated.getName()
                );

                statement.setDouble(
                    3,
                    validated.getMaximumScore()
                );

                statement.setDouble(
                    4,
                    validated.getWeight()
                );

                if (statement.executeUpdate() != 1) {
                    throw new SQLException(
                        "Assessment creation failed."
                    );
                }

                try (ResultSet keys =
                        statement.getGeneratedKeys()) {

                    if (!keys.next()) {
                        throw new SQLException(
                            "No assessment ID returned."
                        );
                    }

                    assessmentId = keys.getInt(1);
                }
            }

            connection.commit();

            return assessmentId;

        } catch (SQLException
                | RuntimeException exception) {

            connection.rollback();
            throw exception;
        }
    }
}


public static void updateAssessment(
        int assessmentId,
        String name,
        double maximumScore,
        double weight
) throws SQLException {

    Assessment validated =
        new Assessment(
            name,
            maximumScore,
            weight
        );

    String findSQL =
        "SELECT SubjectID " +
        "FROM OLA_Assessments " +
        "WHERE AssessmentID = ?";

    String updateSQL =
        "UPDATE OLA_Assessments SET " +
        "AssessmentName = ?, " +
        "MaximumScore = ?, " +
        "Weight = ? " +
        "WHERE AssessmentID = ?";

    try (Connection connection =
            DatabaseConnection.getConnection()) {

        connection.setAutoCommit(false);

        try {

            int subjectId;

            try (PreparedStatement statement =
                    connection.prepareStatement(findSQL)) {

                statement.setInt(1, assessmentId);

                try (ResultSet result =
                        statement.executeQuery()) {

                    if (!result.next()) {
                        throw new SQLException(
                            "Assessment not found."
                        );
                    }

                    subjectId =
                        result.getInt("SubjectID");
                }
            }

            // Lock the subject before changing
            // any assessment belonging to it.
            try (PreparedStatement statement =
                    connection.prepareStatement(
                        "SELECT SubjectID FROM Subjects "
                        + "WHERE SubjectID = ? FOR UPDATE"
                    )) {

                statement.setInt(1, subjectId);

                try (ResultSet result =
                        statement.executeQuery()) {

                    if (!result.next()) {
                        throw new SQLException(
                            "Subject not found."
                        );
                    }
                }
            }

            checkCourseUnlocked(
                connection,
                subjectId
            );

            // Existing scores must not exceed
            // the proposed maximum.
            try (PreparedStatement statement =
                    connection.prepareStatement(
                        "SELECT Score FROM OLA_Scores "
                        + "WHERE AssessmentID = ? FOR UPDATE"
                    )) {

                statement.setInt(1, assessmentId);

                try (ResultSet result =
                        statement.executeQuery()) {

                    while (result.next()) {
                        validated.calculatePercentage(
                            result.getDouble("Score")
                        );
                    }
                }
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(updateSQL)) {

                statement.setString(
                    1,
                    validated.getName()
                );

                statement.setDouble(
                    2,
                    validated.getMaximumScore()
                );

                statement.setDouble(
                    3,
                    validated.getWeight()
                );

                statement.setInt(4, assessmentId);

                if (statement.executeUpdate() != 1) {
                    throw new SQLException(
                        "Assessment update failed."
                    );
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


public static void deleteAssessment(
        int assessmentId
) throws SQLException {

    try (Connection connection =
            DatabaseConnection.getConnection()) {

        connection.setAutoCommit(false);

        try {

            int subjectId;

            try (PreparedStatement statement =
                    connection.prepareStatement(
                        "SELECT SubjectID "
                        + "FROM OLA_Assessments "
                        + "WHERE AssessmentID = ?"
                    )) {

                statement.setInt(1, assessmentId);

                try (ResultSet result =
                        statement.executeQuery()) {

                    if (!result.next()) {
                        throw new SQLException(
                            "Assessment not found."
                        );
                    }

                    subjectId =
                        result.getInt("SubjectID");
                }
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(
                        "SELECT SubjectID FROM Subjects "
                        + "WHERE SubjectID = ? FOR UPDATE"
                    )) {

                statement.setInt(1, subjectId);

                try (ResultSet result =
                        statement.executeQuery()) {

                    if (!result.next()) {
                        throw new SQLException(
                            "Subject not found."
                        );
                    }
                }
            }

            checkCourseUnlocked(
                connection,
                subjectId
            );

            // Delete associated scores first.
            try (PreparedStatement statement =
                    connection.prepareStatement(
                        "DELETE FROM OLA_Scores "
                        + "WHERE AssessmentID = ?"
                    )) {

                statement.setInt(1, assessmentId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(
                        "DELETE FROM OLA_Assessments "
                        + "WHERE AssessmentID = ?"
                    )) {

                statement.setInt(1, assessmentId);

                if (statement.executeUpdate() != 1) {
                    throw new SQLException(
                        "Assessment deletion failed."
                    );
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


public static void saveStudentScores(
        String studentNumber,
        String courseCode,
        Map<Integer, Double> scores
) throws SQLException {

    if (scores == null || scores.isEmpty()) {
        throw new IllegalArgumentException(
            "No OLA scores supplied."
        );
    }

    String findStudent =
        "SELECT UserID FROM Users " +
        "WHERE StudentNumber = ? " +
        "AND Role = 'Student'";

    String checkEnrollment =
        "SELECT EnrollmentID FROM Enrollments " +
        "WHERE StudentID = ? " +
        "AND SubjectID = ? " +
        "AND Status = 'Enrolled'";

    String findGrade =
        "SELECT SubmissionStatus FROM Grades " +
        "WHERE StudentID = ? " +
        "AND SubjectID = ? FOR UPDATE";

    String findAssessment =
        "SELECT SubjectID, MaximumScore " +
        "FROM OLA_Assessments " +
        "WHERE AssessmentID = ? FOR UPDATE";

    String saveSQL =
        "INSERT INTO OLA_Scores " +
        "(AssessmentID, StudentID, Score) " +
        "VALUES (?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE Score = VALUES(Score)";

    try (Connection connection =
            DatabaseConnection.getConnection()) {

        connection.setAutoCommit(false);

        try {

            int subjectId =
                findSubjectId(
                    connection,
                    courseCode
                );

            int studentId;

            try (PreparedStatement statement =
                    connection.prepareStatement(
                        findStudent
                    )) {

                statement.setString(
                    1,
                    studentNumber
                );

                try (ResultSet result =
                        statement.executeQuery()) {

                    if (!result.next()) {
                        throw new SQLException(
                            "Student not found."
                        );
                    }

                    studentId =
                        result.getInt("UserID");
                }
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(
                        checkEnrollment
                    )) {

                statement.setInt(
                    1,
                    studentId
                );

                statement.setInt(
                    2,
                    subjectId
                );

                try (ResultSet result =
                        statement.executeQuery()) {

                    if (!result.next()) {
                        throw new SQLException(
                            "Student is not actively "
                            + "enrolled in this subject."
                        );
                    }
                }
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(
                        findGrade
                    )) {

                statement.setInt(
                    1,
                    studentId
                );

                statement.setInt(
                    2,
                    subjectId
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
                                "Cannot edit a "
                                + status
                                + " grade's OLA scores."
                            );
                        }
                    }
                }
            }

            // Validate the complete batch
            // before saving any score.
            Set<Integer> validIds =
                new HashSet<>();

            for (Map.Entry<Integer, Double> entry :
                    scores.entrySet()) {

                Integer assessmentId =
                    entry.getKey();

                Double score =
                    entry.getValue();

                if (assessmentId == null
                        || score == null) {

                    throw new IllegalArgumentException(
                        "Invalid OLA score entry."
                    );
                }

                try (PreparedStatement statement =
                        connection.prepareStatement(
                            findAssessment
                        )) {

                    statement.setInt(
                        1,
                        assessmentId
                    );

                    try (ResultSet result =
                            statement.executeQuery()) {

                        if (!result.next()) {
                            throw new SQLException(
                                "Assessment not found: "
                                + assessmentId
                            );
                        }

                        if (result.getInt("SubjectID")
                                != subjectId) {

                            throw new SQLException(
                                "Assessment belongs "
                                + "to another subject."
                            );
                        }

                        double maximum =
                            result.getDouble(
                                "MaximumScore"
                            );

                        if (!Double.isFinite(score)
                                || score < 0
                                || score > maximum) {

                            throw new IllegalArgumentException(
                                "Invalid score for "
                                + "assessment "
                                + assessmentId
                            );
                        }
                    }
                }

                validIds.add(assessmentId);
            }

            for (Integer assessmentId : validIds) {

                try (PreparedStatement statement =
                        connection.prepareStatement(
                            saveSQL
                        )) {

                    statement.setInt(
                        1,
                        assessmentId
                    );

                    statement.setInt(
                        2,
                        studentId
                    );

                    statement.setDouble(
                        3,
                        scores.get(assessmentId)
                    );

                    statement.executeUpdate();
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

}
