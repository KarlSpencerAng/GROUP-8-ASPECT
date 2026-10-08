/**
 * Assessment.java
 * Purpose: Assessment model.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

public class Assessment {

    private String name;
    private double maximumScore;
    private double weight;

    public Assessment(
            String name,
            double maximumScore,
            double weight) {

        setName(name);
        setMaximumScore(maximumScore);
        setWeight(weight);
    }

    public String getName() {
        return name;
    }

    public double getMaximumScore() {
        return maximumScore;
    }

    public double getWeight() {
        return weight;
    }

    public void setName(String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Assessment name cannot be empty."
            );
        }

        this.name = name.trim();
    }

    public void setMaximumScore(double maximumScore) {

        if (!Double.isFinite(maximumScore)
                || maximumScore <= 0) {

            throw new IllegalArgumentException(
                    "Maximum score must be greater than zero."
            );
        }

        this.maximumScore = maximumScore;
    }

    public void setWeight(double weight) {

        if (!Double.isFinite(weight)
                || weight < 0
                || weight > 100) {

            throw new IllegalArgumentException(
                    "Assessment weight must be between 0 and 100."
            );
        }

        this.weight = weight;
    }

    public double calculatePercentage(double studentScore) {

        if (!Double.isFinite(studentScore)
                || studentScore < 0
                || studentScore > maximumScore) {

            throw new IllegalArgumentException(
                    "Student score must be between 0 and "
                            + maximumScore
            );
        }

        return (studentScore / maximumScore) * 100;
    }

    public double calculateWeightedScore(double studentScore) {

        return calculatePercentage(studentScore)
                * (weight / 100);
    }
}