package gradeer.grading;

import gradeer.checks.Check;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GradeGenerator
{
    private static Logger logger = LogManager.getLogger(GradeGenerator.class);

    private Collection<Check> checks;
    private double totalWeight;
    private Set<Solution> executedSolutions;

    public GradeGenerator(Collection<Check> checks)
    {
        this.checks = checks;
        this.totalWeight = checks.stream().mapToDouble(Check::getWeight).sum();
        this.executedSolutions = new HashSet<>();
    }

    public void runChecks(Collection<Solution> solutions)
    {
        solutions.forEach(this::runChecks);
    }

    public void runChecks(Solution solution)
    {
        if(executedSolutions.contains(solution))
            return;

        logger.info("Running checks for Solution " + solution.getDirectory());
        checks.forEach(check -> check.run(solution));
        executedSolutions.add(solution);
    }

    /**
     * Generate a grade for the given Solution.
     * @param solution a Solution to grade.
     * @return the percentage grade of the Solution.
     */
    public double generateGrade(Solution solution)
    {
        if(!executedSolutions.contains(solution))
            runChecks(solution);

        return 100 * checks.stream()
                .mapToDouble(c -> c.getWeightedScore(solution))
                .sum() / totalWeight;
    }
}
