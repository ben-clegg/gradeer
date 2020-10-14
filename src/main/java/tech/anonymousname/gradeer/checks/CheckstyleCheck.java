package tech.anonymousname.gradeer.checks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.anonymousname.gradeer.checks.checkresults.CheckResult;
import tech.anonymousname.gradeer.checks.exceptions.InvalidCheckException;
import tech.anonymousname.gradeer.solution.Flag;
import tech.anonymousname.gradeer.solution.Solution;

import java.nio.file.Path;
import java.util.Map;

public class CheckstyleCheck extends Check
{
    int minimumViolations = 0;
    int maximumViolations = 4;

    public CheckstyleCheck(JsonObject jsonObject) throws InvalidCheckException
    {
        super(jsonObject);
        this.minimumViolations = getElementOrDefault(jsonObject, "minimumViolations",
                JsonElement::getAsInt, minimumViolations);
        this.maximumViolations = getElementOrDefault(jsonObject, "maximumViolations",
                JsonElement::getAsInt, maximumViolations);
    }


    @Override
    public void execute(Solution solution)
    {
        if(solution.getCheckstyleProcessResults() == null)
        {
            System.err.println("No CheckStyle process results for Solution " + solution.getIdentifier());
            solution.addFlag(Flag.NO_CHECKSTYLE_RESULTS);
            double score = 0.0;
            solution.addCheckResult(new CheckResult(this, score, generateFeedback(score)));
            return;
        }

        // Determine grade and feedback
        solution.addCheckResult(generateResult(solution));
    }

    private CheckResult generateResult(Solution solution)
    {
        Map<Path, Integer> violations = solution.getCheckstyleProcessResults().getViolations().get(this);

        // If no violations defined for this check and solution, assume it is correct
        if(violations == null || violations.isEmpty())
        {
            return new CheckResult(this, 1.0, generateFeedback(1.0));
        }

        // Get number of violations
        int totalTrackedViolations = 0;
        for (Integer v : violations.values())
            totalTrackedViolations += v;

        // Derive score from maximum & minimum violations
        if(totalTrackedViolations >= maximumViolations)
        {
            return new CheckResult(this, 0.0, generateFeedback(0.0));
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
        return "CheckstyleCheck{" +
                "maximumViolations=" + maximumViolations +
                ", minimumViolations=" + minimumViolations +
                ", (" + super.toString() + ")" +
                '}';
    }
}
