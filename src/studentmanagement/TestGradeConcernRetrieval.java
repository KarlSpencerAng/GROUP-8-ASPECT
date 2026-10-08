/**
 * TestGradeConcernRetrieval.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.util.List;

public class TestGradeConcernRetrieval {

    public static void main(String[] args) {

        try (Connection connection =
                DatabaseConnection.getConnection()) {

            // Ensure we're using the test database.
            String database = connection.getCatalog();

            if (!"GradingSystem_Test".equalsIgnoreCase(database)) {
                throw new IllegalStateException(
                    "STOP: Connected to " + database
                    + ". Use the test database.");
            }

            System.out.println("Connected to: " + database);

            // Load students and courses into AcademicData.
            DataInitializer.initialize();

            // Retrieve Marco's concerns.
            List<GradeConcern> concerns =
                GradeConcernRepository.loadStudentConcerns(
                    "20260001");

            System.out.println(
                "Concerns retrieved: " + concerns.size());

            for (GradeConcern concern : concerns) {
                System.out.println(
                    "ID: " + concern.getConcernId()
                    + " | Course: "
                    + concern.getCourse().getCourseCode()
                    + " | Status: "
                    + concern.getStatus());
            }

            // Verify the two expected concerns.
            boolean mathFound = false;
            boolean cssFound = false;

            for (GradeConcern concern : concerns) {
                if (concern.getConcernId() == 1
                        && "MATH174".equals(
                            concern.getCourse().getCourseCode())
                        && "Pending".equals(concern.getStatus())) {
                    mathFound = true;
                }

                if (concern.getConcernId() == 4
                        && "CSS123P".equals(
                            concern.getCourse().getCourseCode())
                        && "Pending".equals(concern.getStatus())) {
                    cssFound = true;
                }
            }

            if (!mathFound || !cssFound) {
                throw new AssertionError(
                    "FAIL: Expected concerns were not retrieved.");
            }

            System.out.println(
                "PASS: Both existing concerns retrieved correctly.");

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
