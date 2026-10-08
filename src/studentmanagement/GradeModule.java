/**
 * GradeModule.java
 * Purpose: Grade module.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

import java.util.List;

public class GradeModule {

    // Calculate the overall average of all subjects
    public double calculateOverallAverage(List<Grade> grades) {

        if (grades == null || grades.isEmpty()) {
            return 0;
        }

        double total = 0;

        for (Grade grade : grades) {
            total += grade.getFinalGrade();
        }

        return total / grades.size();
    }

    // Get the highest final grade
    public double getHighestGrade(List<Grade> grades) {

        if (grades == null || grades.isEmpty()) {
            return 0;
        }

        double highest = grades.get(0).getFinalGrade();

        for (Grade grade : grades) {
            if (grade.getFinalGrade() > highest) {
                highest = grade.getFinalGrade();
            }
        }

        return highest;
    }

    // Get the lowest final grade
    public double getLowestGrade(List<Grade> grades) {

        if (grades == null || grades.isEmpty()) {
            return 0;
        }

        double lowest = grades.get(0).getFinalGrade();

        for (Grade grade : grades) {
            if (grade.getFinalGrade() < lowest) {
                lowest = grade.getFinalGrade();
            }
        }

        return lowest;
    }
}