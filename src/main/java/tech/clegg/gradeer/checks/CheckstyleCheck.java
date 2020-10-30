package tech.clegg.gradeer.checks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.checks.exceptions.InvalidCheckException;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.DefaultFlag;
import tech.clegg.gradeer.solution.Solution;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CheckstyleCheck extends Check
{
    int minimumViolations = 0;
    int maximumViolations = 4;

    public CheckstyleCheck(JsonObject jsonObject, Configuration configuration) throws InvalidCheckException
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
        // No Checkstyle results exist
        if(solution.getCheckstyleProcessResults() == null)
        {
            System.err.println("No Checkstyle process results for Solution " + solution.getIdentifier());
            solution.addFlag(DefaultFlag.NO_CHECKSTYLE_RESULTS);
            double score = 0.0;
            solution.addCheckResult(new CheckResult(this, score, generateFeedback(score)));
            return;
        }

        // Process as normal
        processSolution(solution);
    }

    @Override
    protected double generateUnweightedScore(Solution solution)
    {
        Collection<AuditEvent> violations = solution.getCheckstyleProcessResults()
                .getAuditEventsForCheck(this);

        // If no violations defined for this check and solution, assume it is correct
        if(violations == null || violations.isEmpty())
            return 1.0;

        // Get number of violations
        int numViolations = violations.size();

        // Derive score from maximum & minimum violations
        if(numViolations >= maximumViolations)
            return 0.0;
        if(numViolations <= minimumViolations)
            return 1.0;

        numViolations -= minimumViolations;
        return (1.0 - ((double) numViolations / (maximumViolations - minimumViolations)));
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
