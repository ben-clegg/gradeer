package tech.clegg.gradeer.checks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.checks.exceptions.InvalidCheckException;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.preprocessing.PMDPreProcessor;
import tech.clegg.gradeer.preprocessing.PreProcessor;
import tech.clegg.gradeer.preprocessing.staticanalysis.pmd.PMDProcessResults;
import tech.clegg.gradeer.preprocessing.staticanalysis.pmd.PMDViolation;
import tech.clegg.gradeer.solution.DefaultFlag;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;
import java.util.Collections;

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
    protected String defaultCheckGroupForType()
    {
        return "Code Style / Quality";
    }

    @Override
    public Collection<Class<? extends PreProcessor>> getPreProcessorTypes()
    {
        return Collections.singleton(PMDPreProcessor.class);
    }

    @Override
    public void execute(Solution solution)
    {
        // No PMD results exist
        if(!solution.hasPreProcessorResultsOfType(PMDPreProcessor.class))
        {
            System.err.println("No PMD process results for Solution " + solution.getIdentifier());
            solution.addFlag(DefaultFlag.NO_PMD_RESULTS);
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
        PMDProcessResults pmdResults = (PMDProcessResults) solution.getPreProcessorResultsOfType(PMDPreProcessor.class);
        Collection<PMDViolation> violations = pmdResults.getViolations(name);

        if(violations == null || violations.isEmpty())
            return 1.0;

        int totalTrackedViolations = violations.size();

        // Derive score from maximum & minimum violations
        if(totalTrackedViolations >= maximumViolations)
            return 0.0;
        if(totalTrackedViolations <= minimumViolations)
            return 1.0;

        totalTrackedViolations -= minimumViolations;
        return (1.0 - ((double) totalTrackedViolations / (maximumViolations - minimumViolations)));
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
