package gradeer.results;

import gradeer.checks.Check;
import gradeer.checks.CheckProcessor;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GradeGenerator
{
    private static Logger logger = LogManager.getLogger(GradeGenerator.class);


    private double totalWeight;
    private CheckProcessor checkProcessor;

    public GradeGenerator(CheckProcessor checkProcessor)
    {
        this.checkProcessor = checkProcessor;
        this.totalWeight = checkProcessor.getChecks().stream().mapToDouble(Check::getWeight).sum();
    }

    /**
     * Generate a grade for the given Solution.
     * @param solution a Solution to grade.
     * @return the percentage grade of the Solution.
     */
    public double generateGrade(Solution solution)
    {
        if (!checkProcessor.wasExecuted(solution))
            checkProcessor.runChecks(solution);

        for (Check c : checkProcessor.getChecks())
            logger.info("Solution " + solution.getIdentifier() + ": " + c.getName() + " " + c.getWeightedScore(solution));

        return 100 * checkProcessor.getChecks().stream()
                .mapToDouble(c -> c.getWeightedScore(solution))
                .sum() / totalWeight;
    }
}
