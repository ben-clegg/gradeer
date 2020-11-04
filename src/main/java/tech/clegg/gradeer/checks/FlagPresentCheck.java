package tech.clegg.gradeer.checks;

import com.google.gson.JsonObject;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.checks.exceptions.InvalidCheckException;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.preprocessing.PreProcessor;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;

/**
 * Check to determine if a given Flag is present for a solution; passes if it is, fails otherwise.
 */
public class FlagPresentCheck extends Check
{
    protected String flag;

    public FlagPresentCheck(JsonObject jsonObject, Configuration configuration) throws InvalidCheckException
    {
        super(jsonObject, configuration);

        // Load flag
        try
        {
            // Purposefully unsafe; flags are required and have no default
            this.flag = getOptionalElement(jsonObject, "flag").get().getAsString();
        }
        catch (NoSuchElementException noElem)
        {
            System.err.println("No flag defined for FlagPresentCheck");
            throw new InvalidCheckException("No flag defined for FlagPresentCheck " + jsonObject.getAsString() + " , skipping.");
        }

        // Empty flag is invalid
        if (this.flag == null || this.flag.equals(""))
        {
            System.err.println("No flag defined for FlagPresentCheck");
            throw new InvalidCheckException("No flag defined for FlagPresentCheck " + jsonObject.getAsString() + " , skipping.");
        }
    }

    @Override
    public Collection<Class<? extends PreProcessor>> getPreProcessorTypes()
    {
        return Collections.emptySet();
    }

    @Override
    protected void execute(Solution solution)
    {
        processSolution(solution);
    }

    @Override
    protected double generateUnweightedScore(Solution solution)
    {
        if(solution.containsFlag(flag))
            return 1.0;
        return 0.0;
    }
}
