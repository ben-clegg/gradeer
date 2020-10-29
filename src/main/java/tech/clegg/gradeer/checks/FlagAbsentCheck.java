package tech.clegg.gradeer.checks;

import com.google.gson.JsonObject;
import tech.clegg.gradeer.checks.exceptions.InvalidCheckException;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;

import java.util.NoSuchElementException;

/**
 * Check to determine if a given Flag is absent (_not_ present) for a solution; passes if it is absent, fails otherwise.
 */
public class FlagAbsentCheck extends FlagPresentCheck
{
    public FlagAbsentCheck(JsonObject jsonObject, Configuration configuration) throws InvalidCheckException
    {
        // Inherited from FlagPresentCheck
        super(jsonObject, configuration);
    }

    @Override
    protected double generateUnweightedScore(Solution solution)
    {
        if(solution.containsFlag(this.flag))
            return 0.0;
        return 1.0;
    }
}
