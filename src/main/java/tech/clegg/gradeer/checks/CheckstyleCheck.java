package tech.clegg.gradeer.checks;

import tech.clegg.gradeer.checks.generation.json.StaticAnalysisCheckJSONEntry;
import tech.clegg.gradeer.solution.Solution;

import java.nio.file.Path;
import java.util.Map;

public class CheckstyleCheck extends Check
{
    private int maximumViolations = 4;
    private int minimumViolations = 0;

    public CheckstyleCheck(StaticAnalysisCheckJSONEntry json)
    {
        super();
        this.name = json.getName();
        this.feedbackCorrect = json.getFeedbackCorrect();
        this.feedbackIncorrect = json.getFeedbackIncorrect();
        this.weight = json.getWeight();

        if(json.getMaxViolations() >= 1)
            maximumViolations = json.getMaxViolations();
        if(json.getMinViolations() >= 0)
            minimumViolations = json.getMinViolations();
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

        // Get number of violations
        int totalTrackedViolations = 0;
        for (Integer v : violations.values())
            totalTrackedViolations += v;

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
