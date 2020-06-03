package tech.clegg.gradeer.checks;

import tech.clegg.gradeer.checks.generation.json.StaticAnalysisCheckJSONEntry;
import tech.clegg.gradeer.execution.staticanalysis.pmd.PMDViolation;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;

public class PMDCheck extends Check
{

    private int maximumViolations = 4;
    private int minimumViolations = 0;

    public PMDCheck(StaticAnalysisCheckJSONEntry json)
    {
        super();
        this.name = json.getName();
        this.weight = json.getWeight();
        this.feedbackCorrect = json.getFeedbackCorrect();
        this.feedbackIncorrect = json.getFeedbackIncorrect();

        if(json.getMaxViolations() >= 1)
            maximumViolations = json.getMaxViolations();
        if(json.getMinViolations() >= 0)
            minimumViolations = json.getMinViolations();
    }

    @Override
    public void run(Solution solution)
    {
        if(solution.getPmdProcessResults() == null)
            return;

        Collection<PMDViolation> violations = solution.getPmdProcessResults().getViolations(name);

        if(violations == null || violations.isEmpty())
        {
            unweightedScores.put(solution, 1.0);
            return;
        }

        int totalTrackedViolations = violations.size();

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
}
