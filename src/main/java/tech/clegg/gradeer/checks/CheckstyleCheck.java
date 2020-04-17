package tech.clegg.gradeer.checks;

import tech.clegg.gradeer.solution.Solution;

import java.nio.file.Path;
import java.util.Map;

public class CheckstyleCheck extends Check
{
    private int perSourceFileGrace = 0;

    private int maximumViolations = 5;
    private int minimumViolations = 1;


    public CheckstyleCheck(String name, String feedbackCorrect, String feedbackIncorrect, double weight)
    {
        super();
        this.name = name;
        this.feedbackCorrect = feedbackCorrect;
        this.feedbackIncorrect = feedbackIncorrect;
        this.weight = weight;
    }

    @Override
    public void run(Solution solution)
    {
        if(solution.getCheckstyleProcessResults() == null)
            return;

        Map<Path, Integer> violations = solution.getCheckstyleProcessResults().getViolations().get(this);

        // If no violations defined for this check and solution, assume it is correct
        if(violations == null || violations.isEmpty())
        {
            unweightedScores.put(solution, 1.0);
            return;
        }

        int totalTrackedViolations = 0;
        for (Integer v : violations.values())
        {
            // Remove grace value to prevent excessive punishment for infrequent errors (optional)
            if(v > perSourceFileGrace)
                totalTrackedViolations += v - perSourceFileGrace;
        }

        // Derive score from maximum & minimum violations
        if(totalTrackedViolations >= maximumViolations)
        {
            unweightedScores.put(solution, 0.0);
            return;
        }
        if(totalTrackedViolations <= minimumViolations)
        {
            unweightedScores.put(solution, 1.0);
            return;
        }

        totalTrackedViolations -= minimumViolations;
        double score = 1.0 - (double) totalTrackedViolations / (maximumViolations - minimumViolations);
        unweightedScores.put(solution, score);
    }

    @Override
    public String toString()
    {
        return "CheckstyleCheck{" +
                "name='" + name + "'" +
                ", feedback='" + feedbackCorrect + "'" +
                ", weight=" + weight +
                '}';
    }
}
