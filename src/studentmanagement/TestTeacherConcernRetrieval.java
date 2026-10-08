/**
 * TestTeacherConcernRetrieval.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.util.List;

public class TestTeacherConcernRetrieval {

    public static void main(String[] args) {

        try (Connection connection =
                DatabaseConnection.getConnection()) {

            // Safety check: use the test database only.
            String database = connection.getCatalog();

            if (!"GradingSystem_Test".equalsIgnoreCase(database)) {
                throw new IllegalStateException(
                    "STOP: Connected to " + database
                    + ". Use GradingSystem_Test.");
            }

            System.out.println("Connected to: " + database);

            // Initialize academic data.
            DataInitializer.initialize();

            // Retrieve all existing grade concerns.
            List<GradeConcern> concerns =
                    GradeConcernRepository.loadAllConcerns();

            System.out.println(
                    "Total concerns retrieved: " + concerns.size());

            boolean[] found = new boolean[5];

            for (GradeConcern concern : concerns) {

                int id = concern.getConcernId();

                System.out.println(
                        "ID: " + id
                        + " | Student: "
                        + concern.getStudent().getStudentNumber()
                        + " | Course: "
                        + concern.getCourse().getCourseCode()
                        + " | Status: "
                        + concern.getStatus());

                if (id >= 1 && id <= 4) {
                    found[id] = true;
                }
            }

            // Verify all four existing concerns.
            for (int id = 1; id <= 4; id++) {
                if (!found[id]) {
                    throw new AssertionError(
                            "FAIL: Missing concern ID " + id);
                }
            }

            System.out.println(
                    "PASS: All four existing concerns retrieved.");

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
