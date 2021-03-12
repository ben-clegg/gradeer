package tech.clegg.gradeer.checks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.checks.exceptions.InvalidCheckException;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.preprocessing.CheckstylePreProcessor;
import tech.clegg.gradeer.preprocessing.PreProcessor;
import tech.clegg.gradeer.preprocessing.staticanalysis.checkstyle.CheckstyleProcess;
import tech.clegg.gradeer.preprocessing.staticanalysis.checkstyle.CheckstyleProcessResults;
import tech.clegg.gradeer.solution.DefaultFlag;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;
import java.util.Collections;

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
    protected String defaultCheckGroupForType()
    {
        return "Code Style / Quality";
    }

    @Override
    public Collection<Class<? extends PreProcessor>> getPreProcessorTypes()
    {
        return Collections.singleton(CheckstylePreProcessor.class);
    }

    @Override
    public void execute(Solution solution)
    {
        // No Checkstyle results exist
        if(!solution.hasPreProcessorResultsOfType(CheckstylePreProcessor.class))
        {
            System.err.println("No Checkstyle process results for Solution " + solution.getIdentifier());
            solution.addFlag(DefaultFlag.NO_CHECKSTYLE_RESULTS);
            solution.addCheckResult(new CheckResult(this, 0.0, generateFeedback(0.0)));
            return;
        }

        // Checkstyle crashed
        CheckstyleProcessResults processResults =
                (CheckstyleProcessResults) solution.getPreProcessorResultsOfType(CheckstylePreProcessor.class);
        if(processResults.hasSevereExecutionErrorOccured())
        {
            System.err.println("Checkstyle crashed for Solution " + solution.getIdentifier() +
                    "; setting check " + name + "as failed.");
            solution.addFlag(DefaultFlag.NO_CHECKSTYLE_RESULTS);
            solution.addFlag(DefaultFlag.CHECKSTYLE_CRASHED);
            solution.addCheckResult(
                    new CheckResult(this, 0.0,
                            "Your solution has a problem (such as not being compilable) " +
                                    "which prevents style checking from being executed.")
            );
            return;
        }

        // Process as normal
        processSolution(solution);
    }

    @Override
    protected double generateUnweightedScore(Solution solution)
    {
        CheckstyleProcessResults csResults =
                (CheckstyleProcessResults) solution.getPreProcessorResultsOfType(CheckstylePreProcessor.class);
        Collection<AuditEvent> violations = csResults.getAuditEventsForCheck(this);

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
