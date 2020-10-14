package tech.anonymousname.gradeer.checks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.anonymousname.gradeer.checks.checkresults.CheckResult;
import tech.anonymousname.gradeer.checks.exceptions.InvalidCheckException;
import tech.anonymousname.gradeer.configuration.Configuration;
import tech.anonymousname.gradeer.execution.staticanalysis.pmd.PMDViolation;
import tech.anonymousname.gradeer.solution.Flag;
import tech.anonymousname.gradeer.solution.Solution;

import java.util.Collection;

public class PMDCheck extends Check
{

    int minimumViolations = 0;
    int maximumViolations = 4;

    public PMDCheck(JsonObject jsonObject, Configuration configuration) throws InvalidCheckException
    {
        super(jsonObject, configuration);
        this.minimumViolations = getElementOrDefault(jsonObject, "minimumViolations",
                JsonElement::getAsInt, minimumViolations);
        this.maximumViolations = getElementOrDefault(jsonObject, "maximumViolations",
                JsonElement::getAsInt, maximumViolations);
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

    @Override
    public String toString()
    {
        return "PMDCheck{" +
                "maximumViolations=" + maximumViolations +
                ", minimumViolations=" + minimumViolations +
                ", (" + super.toString() + ")" +
                '}';
    }
}
