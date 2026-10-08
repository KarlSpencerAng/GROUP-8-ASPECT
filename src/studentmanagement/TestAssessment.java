/**
 * TestAssessment.java
 * Purpose: Existing test class; retained without behavior changes.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

public class TestAssessment {

    public static void main(String[] args) {

        Assessment worksheet =
                new Assessment("Worksheet", 20, 30);

        double studentScore = 18;

        System.out.printf(
                "Assessment: %s%n",
                worksheet.getName()
        );

        System.out.printf(
                "Percentage: %.2f%%%n",
                worksheet.calculatePercentage(studentScore)
        );

        System.out.printf(
                "Weighted Score: %.2f%%%n",
                worksheet.calculateWeightedScore(studentScore)
        );
    }
}