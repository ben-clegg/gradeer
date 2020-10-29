package tech.clegg.gradeer.results;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.checkprocessing.CheckProcessor;
import tech.clegg.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class GradeGenerator
{
    private static Logger logger = LogManager.getLogger(GradeGenerator.class);


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

            for (Check c : checkProcessor.getAllChecks())
                logger.info("Solution " + solution.getIdentifier() + ": " + c.getName() + " " + solution.calculateWeightedScore(c));
        }

        return 100 * checkProcessors.stream()
                .mapToDouble(cp -> cp.getAllChecks().stream()
                        .mapToDouble(solution::calculateWeightedScore).sum())
                .sum() / totalWeight;
    }
}
