/**
 * TestAssessmentManager.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.util.Arrays;
import java.util.List;

public class TestAssessmentManager {

    public static void main(String[] args) {

        AssessmentManager manager =
                new AssessmentManager();

        manager.addAssessment(
                new Assessment("Worksheet", 20, 30)
        );

        manager.addAssessment(
                new Assessment("Assignment", 50, 40)
        );

        manager.addAssessment(
                new Assessment("Activity", 25, 30)
        );

        System.out.println(
                "Total Weight: "
                        + manager.getTotalWeight()
                        + "%"
        );

        System.out.println(
                "Valid Weights: "
                        + manager.hasValidWeights()
        );

        // Scores correspond to the assessment order
        List<Double> scores =
                Arrays.asList(
                        18.0,
                        45.0,
                        25.0
                );

        double olaGrade =
                manager.calculateOLAGrade(scores);

        System.out.printf(
                "OLA Grade: %.2f%%%n",
                olaGrade
        );

        System.out.printf(
                "Course Contribution: %.2f%%%n",
                olaGrade * 0.10
        );
    }
}