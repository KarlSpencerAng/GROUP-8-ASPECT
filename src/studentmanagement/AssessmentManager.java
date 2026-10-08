/**
 * AssessmentManager.java
 * Purpose: Assessment management logic.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.util.ArrayList;
import java.util.List;

public class AssessmentManager {

    private List<Assessment> assessments;

    public AssessmentManager() {
        assessments = new ArrayList<>();
    }

    // ADD ASSESSMENT

    public void addAssessment(Assessment assessment) {

        if (assessment == null) {
            throw new IllegalArgumentException(
                    "Assessment cannot be null."
            );
        }

        assessments.add(assessment);
    }

    // GET ASSESSMENTS

    public List<Assessment> getAssessments() {
        return new ArrayList<>(assessments);
    }

    // REMOVE ASSESSMENT

    public void removeAssessment(Assessment assessment) {
        assessments.remove(assessment);
    }

    // CALCULATE TOTAL WEIGHT

    public double getTotalWeight() {

        double total = 0;

        for (Assessment assessment : assessments) {
            total += assessment.getWeight();
        }

        return total;
    }

    // VALIDATE TOTAL WEIGHT

    public boolean hasValidWeights() {

        return !assessments.isEmpty()
                && Math.abs(getTotalWeight() - 100) < 0.000001;
    }

    // CALCULATE STUDENT'S OLA GRADE

    public double calculateOLAGrade(
            List<Double> studentScores) {

        if (!hasValidWeights()) {
            throw new IllegalStateException(
                    "Assessment weights must total 100%."
            );
        }

        if (studentScores == null
                || studentScores.size() != assessments.size()) {

            throw new IllegalArgumentException(
                    "A score is required for every assessment."
            );
        }

        double total = 0;

        for (int i = 0; i < assessments.size(); i++) {

            Assessment assessment = assessments.get(i);

            double score = studentScores.get(i);

            total += assessment.calculateWeightedScore(score);
        }

        return total;
    }
}