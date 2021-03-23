package tech.clegg.gradeer.results;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.checkprocessing.CheckProcessor;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;
import java.util.List;

public class GradeGenerator
{
    private double totalWeight;
    private List<CheckProcessor> checkProcessors;

    public GradeGenerator(List<CheckProcessor> checkProcessors)
    {
        this.checkProcessors = checkProcessors;

        this.totalWeight = checkProcessors.stream()
                .mapToDouble(cp -> cp.getAllChecks().stream()
                        .mapToDouble(Check::getWeight).sum())
                .sum();
    }

    /**
     * Generate a grade for the given Solution.
     * @param solution a Solution to grade.
     * @return the percentage grade of the Solution.
     */
    public double generateGrade(Solution solution)
    {
        for (CheckProcessor checkProcessor : checkProcessors)
        {
            if (!checkProcessor.wasExecuted(solution))
                checkProcessor.runChecks(solution);
        }

        return 100 * checkProcessors.stream()
                .mapToDouble(cp -> cp.getAllChecks().stream()
                        .mapToDouble(solution::calculateWeightedScore).sum())
                .sum() / totalWeight;
    }

    public static double generateGrade(Solution solution, Collection<Check> checks)
    {
        double sumWeights = checks.stream().mapToDouble(Check::getWeight).sum();

        return 100 * checks.stream().mapToDouble(solution::calculateWeightedScore).sum()
                / sumWeights;
    }
}
