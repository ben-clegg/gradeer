package tech.clegg.gradeer.checks;

import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.checks.generation.json.StaticAnalysisCheckJSONEntry;
import tech.clegg.gradeer.execution.staticanalysis.pmd.PMDViolation;
import tech.clegg.gradeer.solution.Flag;
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
    public void execute(Solution solution)
    {
        if(solution.getPmdProcessResults() == null)
        {
            System.err.println("No PMD process results for Solution " + solution.getIdentifier());
            solution.addFlag(Flag.NO_PMD_RESULTS);
            double score = 0.0;
            solution.addCheckResult(new CheckResult(this, score, generateFeedback(score)));
            return;
        }


        // Determine grade and feedback
        solution.addCheckResult(generateResult(solution));
    }

    private CheckResult generateResult(Solution solution)
    {
        Collection<PMDViolation> violations = solution.getPmdProcessResults().getViolations(name);

        if(violations == null || violations.isEmpty())
        {
            return new CheckResult(this,1.0, generateFeedback(1.0));
        }

        int totalTrackedViolations = violations.size();

        // Derive score from maximum & minimum violations
        if(totalTrackedViolations >= maximumViolations)
        {
            return new CheckResult(this,0.0, generateFeedback(0.0));
        }
        if(totalTrackedViolations <= minimumViolations)
        {
            return new CheckResult(this, 1.0, generateFeedback(1.0));
        }

        totalTrackedViolations -= minimumViolations;
        double unweightedScore = 1.0 - (double) totalTrackedViolations / (maximumViolations - minimumViolations);
        return new CheckResult(this, unweightedScore, generateFeedback(unweightedScore));
    }
}
