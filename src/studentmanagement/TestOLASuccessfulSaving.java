/**
 * TestOLASuccessfulSaving.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestOLASuccessfulSaving {

    private static final String TEST_DATABASE =
            "GradingSystem_Test";

    private static final String STUDENT =
            "20260001";

    private static final String SUBJECT =
            "CSS123P";

    private static final int ACTIVITY_1 = 5;
    private static final int ACTIVITY_2 = 6;

    public static void main(String[] args) {

        System.out.println(
            "=== PHASE 4B SUCCESSFUL SAVING TEST ==="
        );

        try {

            verifyTestDatabase();

            testInsert();

            testUpdate();

            testRepositoryReload();

            System.out.println(
                "\nALL SUCCESSFUL SAVING TESTS PASSED"
            );

        } catch (Exception exception) {

            System.err.println(
                "\nTEST FAILED: "
                + exception.getMessage()
            );

            exception.printStackTrace();
        }
    }

    // TEST 0: Verify the test database.

    private static void verifyTestDatabase()
            throws SQLException {

        System.out.println(
            "\nTEST 0: Database safety verification"
        );

        try (Connection connection =
                DatabaseConnection.getConnection()) {

            String actualDatabase =
                    connection.getCatalog();

            if (!TEST_DATABASE.equalsIgnoreCase(
                    actualDatabase)) {

                throw new SQLException(
                    "SAFETY STOP: Connected to "
                    + actualDatabase
                    + " instead of "
                    + TEST_DATABASE
                );
            }

            System.out.println(
                "Connected to: " + actualDatabase
            );

            String sql =
                "SELECT e.Status, " +
                "g.SubmissionStatus " +
                "FROM Enrollments e " +
                "JOIN Users u " +
                "ON e.StudentID = u.UserID " +
                "JOIN Subjects s " +
                "ON e.SubjectID = s.SubjectID " +
                "LEFT JOIN Grades g " +
                "ON g.StudentID = e.StudentID " +
                "AND g.SubjectID = e.SubjectID " +
                "WHERE u.StudentNumber = ? " +
                "AND s.SubjectCode = ?";

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setString(1, STUDENT);
                statement.setString(2, SUBJECT);

                try (ResultSet result =
                        statement.executeQuery()) {

                    if (!result.next()) {

                        throw new SQLException(
                            "Test enrollment not found."
                        );
                    }

                    if (!"Enrolled".equals(
                            result.getString("Status"))) {

                        throw new SQLException(
                            "Student is not actively enrolled."
                        );
                    }

                    String gradeStatus =
                            result.getString(
                                "SubmissionStatus"
                            );

                    if (gradeStatus != null
                            && !"Draft".equals(
                                gradeStatus)) {

                        throw new SQLException(
                            "Test grade is protected."
                        );
                    }

                    if (result.next()) {

                        throw new SQLException(
                            "Duplicate test enrollments."
                        );
                    }
                }
            }

            verifyAssessment(
                connection,
                ACTIVITY_1
            );

            verifyAssessment(
                connection,
                ACTIVITY_2
            );
        }

        if (countScoreRecords() != 0) {

            throw new SQLException(
                "Test scores already exist. "
                + "Use a fresh test database."
            );
        }

        System.out.println(
            "PASS: Safe test environment."
        );
    }

    // Verify the assessment IDs and maximum scores.

    private static void verifyAssessment(
            Connection connection,
            int assessmentId
    ) throws SQLException {

        String sql =
            "SELECT a.MaximumScore " +
            "FROM OLA_Assessments a " +
            "JOIN Subjects s " +
            "ON a.SubjectID = s.SubjectID " +
            "WHERE a.AssessmentID = ? " +
            "AND s.SubjectCode = ?";

        try (PreparedStatement statement =
                connection.prepareStatement(sql)) {

            statement.setInt(
                1,
                assessmentId
            );

            statement.setString(
                2,
                SUBJECT
            );

            try (ResultSet result =
                    statement.executeQuery()) {

                if (!result.next()) {

                    throw new SQLException(
                        "Missing assessment: "
                        + assessmentId
                    );
                }

                if (result.getDouble(
                        "MaximumScore") != 100.0) {

                    throw new SQLException(
                        "Unexpected maximum score "
                        + "for assessment "
                        + assessmentId
                    );
                }
            }
        }
    }

    // TEST 1: Insert two valid scores.

    private static void testInsert()
            throws SQLException {

        System.out.println(
            "\nTEST 1: Permanent score insertion"
        );

        Map<Integer, Double> scores =
                new LinkedHashMap<>();

        scores.put(ACTIVITY_1, 70.0);
        scores.put(ACTIVITY_2, 80.0);

        OLARepository.saveStudentScores(
            STUDENT,
            SUBJECT,
            scores
        );

        verifyStoredScores(
            70.0,
            80.0
        );

        System.out.println(
            "PASS: Both scores inserted."
        );

        System.out.println(
            "PASS: Fresh connection confirms scores."
        );
    }

    // TEST 2: Update existing scores.

    private static void testUpdate()
            throws SQLException {

        System.out.println(
            "\nTEST 2: Permanent score updating"
        );

        Map<Integer, Double> scores =
                new LinkedHashMap<>();

        scores.put(ACTIVITY_1, 85.0);
        scores.put(ACTIVITY_2, 90.0);

        OLARepository.saveStudentScores(
            STUDENT,
            SUBJECT,
            scores
        );

        verifyStoredScores(
            85.0,
            90.0
        );

        System.out.println(
            "PASS: Existing scores updated."
        );

        System.out.println(
            "PASS: No duplicate score records."
        );
    }

    // TEST 3: Reload scores through OLARepository.

    private static void testRepositoryReload()
            throws SQLException {

        System.out.println(
            "\nTEST 3: Repository reload"
        );

        OLARepository.OLAData data =
                OLARepository.loadAllOLA();

        Map<String, Double> first =
                data.scores.get(ACTIVITY_1);

        Map<String, Double> second =
                data.scores.get(ACTIVITY_2);

        if (first == null || second == null) {

            throw new AssertionError(
                "Assessment data missing."
            );
        }

        Double firstScore =
                first.get(STUDENT);

        Double secondScore =
                second.get(STUDENT);

        if (firstScore == null
                || secondScore == null
                || firstScore != 85.0
                || secondScore != 90.0) {

            throw new AssertionError(
                "Repository reload returned "
                + "incorrect scores."
            );
        }

        System.out.println(
            "PASS: Scores retrieved after reload."
        );
    }

    // Verify saved scores using a fresh connection.

    private static void verifyStoredScores(
            double expectedFirst,
            double expectedSecond
    ) throws SQLException {

        try (Connection connection =
                DatabaseConnection.getConnection()) {

            if (!TEST_DATABASE.equalsIgnoreCase(
                    connection.getCatalog())) {

                throw new SQLException(
                    "SAFETY STOP: Wrong database."
                );
            }

            String sql =
                "SELECT os.AssessmentID, os.Score " +
                "FROM OLA_Scores os " +
                "JOIN Users u " +
                "ON os.StudentID = u.UserID " +
                "WHERE u.StudentNumber = ? " +
                "AND os.AssessmentID IN (?, ?)";

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setString(
                    1,
                    STUDENT
                );

                statement.setInt(
                    2,
                    ACTIVITY_1
                );

                statement.setInt(
                    3,
                    ACTIVITY_2
                );

                boolean foundFirst = false;
                boolean foundSecond = false;

                int rowCount = 0;

                try (ResultSet result =
                        statement.executeQuery()) {

                    while (result.next()) {

                        rowCount++;

                        int assessmentId =
                                result.getInt(
                                    "AssessmentID"
                                );

                        double score =
                                result.getDouble(
                                    "Score"
                                );

                        if (assessmentId == ACTIVITY_1) {

                            foundFirst = true;

                            if (score != expectedFirst) {

                                throw new AssertionError(
                                    "Activity 1 score mismatch."
                                );
                            }

                        } else if (
                                assessmentId == ACTIVITY_2) {

                            foundSecond = true;

                            if (score != expectedSecond) {

                                throw new AssertionError(
                                    "Activity 2 score mismatch."
                                );
                            }
                        }
                    }
                }

                if (!foundFirst
                        || !foundSecond
                        || rowCount != 2) {

                    throw new AssertionError(
                        "Expected exactly two "
                        + "saved score records."
                    );
                }
            }
        }
    }

    // Count existing test score records.

    private static int countScoreRecords()
            throws SQLException {

        String sql =
            "SELECT COUNT(*) " +
            "FROM OLA_Scores os " +
            "JOIN Users u " +
            "ON os.StudentID = u.UserID " +
            "WHERE u.StudentNumber = ? " +
            "AND os.AssessmentID IN (?, ?)";

        try (Connection connection =
                DatabaseConnection.getConnection();

             PreparedStatement statement =
                connection.prepareStatement(sql)) {

            if (!TEST_DATABASE.equalsIgnoreCase(
                    connection.getCatalog())) {

                throw new SQLException(
                    "SAFETY STOP: Wrong database."
                );
            }

            statement.setString(
                1,
                STUDENT
            );

            statement.setInt(
                2,
                ACTIVITY_1
            );

            statement.setInt(
                3,
                ACTIVITY_2
            );

            try (ResultSet result =
                    statement.executeQuery()) {

                result.next();

                return result.getInt(1);
            }
        }
    }
}
