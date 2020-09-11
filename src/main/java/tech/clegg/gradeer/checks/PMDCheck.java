package tech.clegg.gradeer.checks;

import tech.clegg.gradeer.checks.checkresults.CheckResult;
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

        // Determine grade and feedback
        solution.addCheckResult(this, generateResult(solution));
    }

    private CheckResult generateResult(Solution solution)
    {
        Collection<PMDViolation> violations = solution.getPmdProcessResults().getViolations(name);

        if(violations == null || violations.isEmpty())
        {
            return new CheckResult(1.0, generateFeedback(1.0));
        }

        int totalTrackedViolations = violations.size();

        // Derive score from maximum & minimum violations
        if(totalTrackedViolations >= maximumViolations)
        {
            return new CheckResult(0.0, generateFeedback(0.0));
        }
        if(totalTrackedViolations <= minimumViolations)
        {
            return new CheckResult(1.0, generateFeedback(1.0));
        }

        totalTrackedViolations -= minimumViolations;
        double unweightedScore = 1.0 - (double) totalTrackedViolations / (maximumViolations - minimumViolations);
        return new CheckResult(unweightedScore, generateFeedback(unweightedScore));
    }
}
