package tech.clegg.gradeer.preprocessing;

import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;

public abstract class PreProcessor
{
    private Solution solution;
    private Configuration configuration;

    public PreProcessor(Solution solution, Configuration configuration)
    {
        this.solution = solution;
        this.configuration = configuration;
    }

    /**
     * Starts the PreProcessor.
     * Called by CheckProcessor before any checks are run on the solution.
     */
    abstract public void start();

    /**
     * Stops and cleans up the PreProcessor.
     * Called by CheckProcessor after every check is run on the solution.
     */
    abstract public void stop();

    protected Solution getSolution()
    {
        return solution;
    }

    protected Configuration getConfiguration()
    {
        return configuration;
    }
}
