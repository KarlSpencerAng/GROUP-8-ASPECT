package studentmanagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database operations for the Registrar grade-approval workflow.
 */
public final class RegistrarRepository {

    private RegistrarRepository() { }

    public static final class SubmissionRow {

        public final int submissionId;
        public final int subjectId;
        public final String subjectCode;
        public final String subjectName;
        public final String teacherName;
        public final String status;
        public final Timestamp submittedAt;
        public final String comment;

        SubmissionRow(ResultSet r) throws SQLException {

            submissionId = r.getInt("SubmissionID");
            subjectId = r.getInt("SubjectID");
            subjectCode = r.getString("SubjectCode");
            subjectName = r.getString("SubjectName");
            teacherName = r.getString("TeacherName");
            status = r.getString("Status");
            submittedAt = r.getTimestamp("SubmittedAt");
            comment = r.getString("RegistrarComment");
        }
    }

    public static final class GradeRow {

        public final String studentNumber;
        public final String studentName;

        public final double co1;
        public final double co2;
        public final double co3;
        public final double finalExam;
        public final double ola;
        public final double coursera;
        public final double finalPercentage;

        public final String result;

        GradeRow(ResultSet r) throws SQLException {

            studentNumber = r.getString("StudentNumber");
            studentName = r.getString("StudentName");

            co1 = r.getDouble("CO1");
            co2 = r.getDouble("CO2");
            co3 = r.getDouble("CO3");

            finalExam = r.getDouble("FinalExam");
            ola = r.getDouble("OLA");
            coursera = r.getDouble("Coursera");

            finalPercentage =
                    r.getDouble("FinalPercentage");

            // Approved project rule:
            // 70% and above = PASS
            result =
                    finalPercentage >= 70.0
                            ? "PASS"
                            : "FAIL";
        }
    }

    // =========================================================
    // LOAD SUBMISSIONS
    // =========================================================

    public static List<SubmissionRow> loadSubmissions(
            String status
    ) throws SQLException {

        List<SubmissionRow> rows =
                new ArrayList<>();

        /*
         * Grade_Submissions stores SubjectCode.
         *
         * Subjects contains:
         * SubjectID
         * SubjectCode
         * SubjectName
         *
         * Therefore we join using SubjectCode.
         */
        String sql =
                "SELECT " +
                "gs.SubmissionID, " +
                "s.SubjectID, " +
                "gs.SubjectCode, " +
                "s.SubjectName, " +
                "CONCAT(t.FirstName, ' ', t.LastName) " +
                "AS TeacherName, " +
                "gs.Status, " +
                "gs.SubmittedAt, " +
                "gs.RegistrarComment " +

                "FROM Grade_Submissions gs " +

                "JOIN Subjects s " +
                "ON s.SubjectCode = gs.SubjectCode " +

                "JOIN Users t " +
                "ON t.UserID = gs.TeacherID " +
                "AND t.Role = 'Teacher' " +

                (status == null
                        ? ""
                        : "WHERE gs.Status = ? ") +

                "ORDER BY " +
                "gs.SubmittedAt DESC, " +
                "gs.SubmissionID DESC";

        try (
            Connection c =
                    DatabaseConnection.getConnection();

            PreparedStatement p =
                    c.prepareStatement(sql)
        ) {

            if (status != null) {
                p.setString(1, status);
            }

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {
                    rows.add(
                            new SubmissionRow(r)
                    );
                }
            }
        }

        return rows;
    }

    // =========================================================
    // LOAD COMPLETE CLASS GRADE SHEET
    // =========================================================

    public static List<GradeRow> loadGradeSheet(
            int submissionId
    ) throws SQLException {

        List<GradeRow> rows =
                new ArrayList<>();

        /*
         * Resolve:
         *
         * Grade_Submissions.SubjectCode
         *          ->
         * Subjects.SubjectCode
         *          ->
         * Subjects.SubjectID
         *          ->
         * Grades.SubjectID
         */

        String sql =
                "SELECT " +
                "u.StudentNumber, " +
                "CONCAT(u.LastName, ', ', u.FirstName) " +
                "AS StudentName, " +

                "g.CO1, " +
                "g.CO2, " +
                "g.CO3, " +
                "g.FinalExam, " +
                "g.OLA, " +
                "g.Coursera, " +
                "g.FinalPercentage " +

                "FROM Grade_Submissions gs " +

                "JOIN Subjects s " +
                "ON s.SubjectCode = gs.SubjectCode " +

                "JOIN Grades g " +
                "ON g.SubjectID = s.SubjectID " +

                "JOIN Users u " +
                "ON u.UserID = g.StudentID " +
                "AND u.Role = 'Student' " +

                "WHERE gs.SubmissionID = ? " +

                "ORDER BY " +
                "u.LastName, " +
                "u.FirstName";

        try (
            Connection c =
                    DatabaseConnection.getConnection();

            PreparedStatement p =
                    c.prepareStatement(sql)
        ) {

            p.setInt(1, submissionId);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {
                    rows.add(
                            new GradeRow(r)
                    );
                }
            }
        }

        return rows;
    }

    // =========================================================
    // POST
    // =========================================================

    public static void post(
            int submissionId,
            int registrarId
    ) throws SQLException {

        review(
                submissionId,
                registrarId,
                "Posted",
                null
        );
    }

    // =========================================================
    // RETURN FOR REVISION
    // =========================================================

    public static void returnForRevision(
            int submissionId,
            int registrarId,
            String comment
    ) throws SQLException {

        if (comment == null
                || comment.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "A return reason is required."
            );
        }

        review(
                submissionId,
                registrarId,
                "Returned",
                comment.trim()
        );
    }

    // =========================================================
    // REVIEW TRANSACTION
    // =========================================================

    private static void review(
            int submissionId,
            int registrarId,
            String newStatus,
            String comment
    ) throws SQLException {

        try (
            Connection c =
                    DatabaseConnection.getConnection()
        ) {

            c.setAutoCommit(false);

            try {

                // ---------------------------------------------
                // 1. Verify Registrar
                // ---------------------------------------------

                try (
                    PreparedStatement p =
                            c.prepareStatement(
                                "SELECT 1 " +
                                "FROM Users " +
                                "WHERE UserID = ? " +
                                "AND Role = 'Registrar'"
                            )
                ) {

                    p.setInt(
                            1,
                            registrarId
                    );

                    try (
                        ResultSet r =
                                p.executeQuery()
                    ) {

                        if (!r.next()) {
                            throw new SQLException(
                                    "Registrar authorization failed."
                            );
                        }
                    }
                }

                // ---------------------------------------------
                // 2. Lock submission and obtain SubjectCode
                // ---------------------------------------------

                String subjectCode;

                try (
                    PreparedStatement p =
                            c.prepareStatement(
                                "SELECT SubjectCode " +
                                "FROM Grade_Submissions " +
                                "WHERE SubmissionID = ? " +
                                "AND Status = 'Pending' " +
                                "FOR UPDATE"
                            )
                ) {

                    p.setInt(
                            1,
                            submissionId
                    );

                    try (
                        ResultSet r =
                                p.executeQuery()
                    ) {

                        if (!r.next()) {

                            throw new SQLException(
                                    "This submission is no longer Pending."
                            );
                        }

                        subjectCode =
                                r.getString(
                                        "SubjectCode"
                                );
                    }
                }

                // ---------------------------------------------
                // 3. Resolve SubjectID
                // ---------------------------------------------

                int subjectId;

                try (
                    PreparedStatement p =
                            c.prepareStatement(
                                "SELECT SubjectID " +
                                "FROM Subjects " +
                                "WHERE SubjectCode = ?"
                            )
                ) {

                    p.setString(
                            1,
                            subjectCode
                    );

                    try (
                        ResultSet r =
                                p.executeQuery()
                    ) {

                        if (!r.next()) {

                            throw new SQLException(
                                    "Subject not found for code: "
                                    + subjectCode
                            );
                        }

                        subjectId =
                                r.getInt(
                                        "SubjectID"
                                );
                    }
                }

                // ---------------------------------------------
                // 4. Update grades
                // ---------------------------------------------

                /*
                 * Batch 2 will make the Teacher submission
                 * explicitly change the class grades to Pending.
                 *
                 * For now this repository expects Pending grades.
                 */

                try (
                    PreparedStatement p =
                            c.prepareStatement(
                                "UPDATE Grades " +
                                "SET SubmissionStatus = ? " +
                                "WHERE SubjectID = ? " +
                                "AND SubmissionStatus = 'Pending'"
                            )
                ) {

                    p.setString(
                            1,
                            newStatus
                    );

                    p.setInt(
                            2,
                            subjectId
                    );

                    int updated =
                            p.executeUpdate();

                    if (updated == 0) {

                        throw new SQLException(
                                "No Pending grades were found "
                                + "for this submission."
                        );
                    }
                }

                // ---------------------------------------------
                // 5. Update Grade_Submissions
                // ---------------------------------------------

                try (
                    PreparedStatement p =
                            c.prepareStatement(
                                "UPDATE Grade_Submissions " +
                                "SET Status = ?, " +
                                "ReviewedBy = ?, " +
                                "ReviewedAt = NOW(), " +
                                "RegistrarComment = ? " +
                                "WHERE SubmissionID = ? " +
                                "AND Status = 'Pending'"
                            )
                ) {

                    p.setString(
                            1,
                            newStatus
                    );

                    p.setInt(
                            2,
                            registrarId
                    );

                    if (comment == null) {

                        p.setNull(
                                3,
                                Types.VARCHAR
                        );

                    } else {

                        p.setString(
                                3,
                                comment
                        );
                    }

                    p.setInt(
                            4,
                            submissionId
                    );

                    if (p.executeUpdate() != 1) {

                        throw new SQLException(
                                "Submission status update failed."
                        );
                    }
                }

                // ---------------------------------------------
                // 6. Commit everything together
                // ---------------------------------------------

                c.commit();

            } catch (
                    SQLException |
                    RuntimeException ex
            ) {

                c.rollback();
                throw ex;

            } finally {

                c.setAutoCommit(true);
            }
        }
    }
}