package gradeer.results;

import gradeer.checks.Check;
import gradeer.checks.CheckExecutor;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GradeGenerator
{
    private static Logger logger = LogManager.getLogger(GradeGenerator.class);


    private double totalWeight;
    private CheckExecutor checkExecutor;

    public GradeGenerator(CheckExecutor checkExecutor)
    {
        this.checkExecutor = checkExecutor;
        this.totalWeight = checkExecutor.getChecks().stream().mapToDouble(Check::getWeight).sum();
    }

    /**
     * Generate a grade for the given Solution.
     * @param solution a Solution to grade.
     * @return the percentage grade of the Solution.
     */
    public double generateGrade(Solution solution)
    {
        if (!checkExecutor.wasExecuted(solution))
            checkExecutor.runChecks(solution);

        return 100 * checkExecutor.getChecks().stream()
                .mapToDouble(c -> c.getWeightedScore(solution))
                .sum() / totalWeight;
    }
}
