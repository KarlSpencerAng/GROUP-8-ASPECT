/**
 * TestOLATransactions.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestOLATransactions {

    private static final String TEST_COURSE = "MATH181";
    private static final String SCORE_COURSE = "CSS123P";
    private static final String STUDENT = "20260001";

    private static final int ASSESSMENT_1 = 5;
    private static final int ASSESSMENT_2 = 6;

    public static void main(String[] args) {

        System.out.println(
            "=== PHASE 4B TRANSACTION TESTS ==="
        );

        try {
            verifyTestEnvironment();

            testAssessmentTransactions();
            testInvalidScoreBatch();
            testProtectedAssessment();

            System.out.println(
                "\nALL STAGE 2 TESTS PASSED"
            );

        } catch (Exception exception) {

            System.err.println(
                "\nTEST FAILED: "
                + exception.getMessage()
            );

            exception.printStackTrace();
        }
    }

    // TEST 0: Confirm the environment is safe.

    private static void verifyTestEnvironment()
            throws SQLException {

        System.out.println(
            "\nTEST 0: Environment verification"
        );

        String subjectSQL =
            "SELECT s.SubjectID, " +
            "(SELECT COUNT(*) FROM Grades g " +
            "WHERE g.SubjectID = s.SubjectID " +
            "AND g.SubmissionStatus " +
            "IN ('Submitted', 'Revised')) " +
            "AS ProtectedCount " +
            "FROM Subjects s " +
            "WHERE s.SubjectCode = ?";

        try (Connection connection =
                DatabaseConnection.getConnection();

             PreparedStatement statement =
                connection.prepareStatement(
                    subjectSQL
                )) {

            statement.setString(1, TEST_COURSE);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (!result.next()) {
                    throw new SQLException(
                        "MATH181 does not exist."
                    );
                }

                if (result.getInt(
                        "ProtectedCount") != 0) {

                    throw new SQLException(
                        "MATH181 contains protected grades."
                    );
                }
            }
        }

        if (countTemporaryAssessments() != 0) {

            throw new SQLException(
                "Existing temporary assessments " +
                "found. Inspect them before testing."
            );
        }

        if (countStudentScore(ASSESSMENT_1) != 0
                || countStudentScore(
                    ASSESSMENT_2) != 0) {

            throw new SQLException(
                "Student already has CSS123P " +
                "scores. Stop to protect existing data."
            );
        }

        System.out.println(
            "PASS: Test environment verified."
        );
    }

    // TEST 1: Permanent assessment CRUD.

    private static void testAssessmentTransactions()
            throws Exception {

        System.out.println(
            "\nTEST 1: Assessment transactions"
        );

        int createdId = -1;

        try {

            createdId =
                OLARepository.createAssessment(
                    TEST_COURSE,
                    "PHASE4B_TEMP_TEST",
                    100.0,
                    10.0
                );

            if (!assessmentMatches(
                    createdId,
                    "PHASE4B_TEMP_TEST",
                    10.0)) {

                throw new AssertionError(
                    "Created assessment not found."
                );
            }

            System.out.println(
                "PASS: Assessment creation."
            );

            OLARepository.updateAssessment(
                createdId,
                "PHASE4B_TEMP_UPDATED",
                100.0,
                15.0
            );

            if (!assessmentMatches(
                    createdId,
                    "PHASE4B_TEMP_UPDATED",
                    15.0)) {

                throw new AssertionError(
                    "Assessment update not persisted."
                );
            }

            System.out.println(
                "PASS: Assessment update."
            );

        } finally {

            if (createdId > 0) {

                // Delete only the record this
                // particular test created.

                OLARepository.deleteAssessment(
                    createdId
                );

                if (assessmentExists(createdId)) {

                    throw new AssertionError(
                        "Assessment deletion failed."
                    );
                }

                System.out.println(
                    "PASS: Assessment deletion."
                );
            }
        }
    }

    // TEST 2: Invalid batch must not save scores.

    private static void testInvalidScoreBatch()
            throws Exception {

        System.out.println(
            "\nTEST 2: Invalid score batch"
        );

        Map<Integer, Double> scores =
            new LinkedHashMap<>();

        scores.put(ASSESSMENT_1, 50.0);

        // Deliberately invalid: maximum is 100.
        scores.put(ASSESSMENT_2, 101.0);

        boolean rejected = false;

        try {

            OLARepository.saveStudentScores(
                STUDENT,
                SCORE_COURSE,
                scores
            );

        } catch (SQLException
                | IllegalArgumentException exception) {

            rejected = true;

            System.out.println(
                "Expected rejection: "
                + exception.getMessage()
            );
        }

        if (!rejected) {

            throw new AssertionError(
                "Invalid batch was accepted."
            );
        }

        if (countStudentScore(ASSESSMENT_1) != 0
                || countStudentScore(
                    ASSESSMENT_2) != 0) {

            throw new AssertionError(
                "Unexpected partial score writes."
            );
        }

        System.out.println(
            "PASS: Invalid batch rejected."
        );

        System.out.println(
            "PASS: No partial score writes."
        );
    }

    // TEST 3: Submitted grades must block
    // ordinary assessment creation.

    private static void testProtectedAssessment()
            throws Exception {

        System.out.println(
            "\nTEST 3: Submitted-grade protection"
        );

        boolean rejected = false;
        int unexpectedId = -1;

        try {

            unexpectedId =
                OLARepository.createAssessment(
                    SCORE_COURSE,
                    "PHASE4B_PROTECTION_TEST",
                    100.0,
                    10.0
                );

        } catch (SQLException exception) {

            rejected = true;

            System.out.println(
                "Expected rejection: "
                + exception.getMessage()
            );

        } finally {

            if (unexpectedId > 0) {

                // Emergency cleanup for a failed
                // protection test. Never delete
                // existing assessment records.

                try (Connection connection =
                        DatabaseConnection.getConnection();

                     PreparedStatement statement =
                        connection.prepareStatement(
                            "DELETE FROM OLA_Assessments "
                            + "WHERE AssessmentID = ? "
                            + "AND AssessmentName = ?"
                        )) {

                    statement.setInt(
                        1,
                        unexpectedId
                    );

                    statement.setString(
                        2,
                        "PHASE4B_PROTECTION_TEST"
                    );

                    statement.executeUpdate();
                }
            }
        }

        if (!rejected) {

            throw new AssertionError(
                "Submitted-grade protection failed."
            );
        }

        System.out.println(
            "PASS: Submitted-grade protection."
        );
    }

    // DATABASE VERIFICATION HELPERS

    private static boolean assessmentExists(
            int assessmentId
    ) throws SQLException {

        String sql =
            "SELECT COUNT(*) " +
            "FROM OLA_Assessments " +
            "WHERE AssessmentID = ?";

        try (Connection connection =
                DatabaseConnection.getConnection();

             PreparedStatement statement =
                connection.prepareStatement(sql)) {

            statement.setInt(
                1,
                assessmentId
            );

            try (ResultSet result =
                    statement.executeQuery()) {

                result.next();

                return result.getInt(1) > 0;
            }
        }
    }

    private static boolean assessmentMatches(
            int assessmentId,
            String expectedName,
            double expectedWeight
    ) throws SQLException {

        String sql =
            "SELECT AssessmentName, Weight " +
            "FROM OLA_Assessments " +
            "WHERE AssessmentID = ?";

        try (Connection connection =
                DatabaseConnection.getConnection();

             PreparedStatement statement =
                connection.prepareStatement(sql)) {

            statement.setInt(
                1,
                assessmentId
            );

            try (ResultSet result =
                    statement.executeQuery()) {

                if (!result.next()) {
                    return false;
                }

                return expectedName.equals(
                    result.getString(
                        "AssessmentName"
                    )
                ) && Math.abs(
                    result.getDouble("Weight")
                    - expectedWeight
                ) < 0.001;
            }
        }
    }

    private static int countStudentScore(
            int assessmentId
    ) throws SQLException {

        String sql =
            "SELECT COUNT(*) " +
            "FROM OLA_Scores os " +
            "JOIN Users u " +
            "ON os.StudentID = u.UserID " +
            "WHERE os.AssessmentID = ? " +
            "AND u.StudentNumber = ?";

        try (Connection connection =
                DatabaseConnection.getConnection();

             PreparedStatement statement =
                connection.prepareStatement(sql)) {

            statement.setInt(
                1,
                assessmentId
            );

            statement.setString(
                2,
                STUDENT
            );

            try (ResultSet result =
                    statement.executeQuery()) {

                result.next();

                return result.getInt(1);
            }
        }
    }

    private static int countTemporaryAssessments()
            throws SQLException {

        String sql =
            "SELECT COUNT(*) " +
            "FROM OLA_Assessments a " +
            "JOIN Subjects s " +
            "ON a.SubjectID = s.SubjectID " +
            "WHERE s.SubjectCode = ? " +
            "AND a.AssessmentName IN (?, ?)";

        try (Connection connection =
                DatabaseConnection.getConnection();

             PreparedStatement statement =
                connection.prepareStatement(sql)) {

            statement.setString(
                1,
                TEST_COURSE
            );

            statement.setString(
                2,
                "PHASE4B_TEMP_TEST"
            );

            statement.setString(
                3,
                "PHASE4B_TEMP_UPDATED"
            );

            try (ResultSet result =
                    statement.executeQuery()) {

                result.next();

                return result.getInt(1);
            }
        }
    }
}
