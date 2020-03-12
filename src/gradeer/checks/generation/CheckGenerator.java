package gradeer.checks.generation;

import gradeer.checks.Check;
import gradeer.configuration.Configuration;
import gradeer.solution.Solution;

import java.util.Collection;
import java.util.HashSet;

public abstract class CheckGenerator
{
    private Configuration configuration;
    private Collection<Solution> modelSolutions;
    private Collection<Check> checks;

    public CheckGenerator(Configuration configuration, Collection<Solution> modelSolutions)
    {
        this.configuration = configuration;
        this.modelSolutions = modelSolutions;

        this.checks = new HashSet<>();
        generate();
        setWeights();
    }

    protected void addCheck(Check check)
    {
        checks.add(check);
    }

    abstract void generate();

    abstract void setWeights();

    protected Configuration getConfiguration()
    {
        return configuration;
    }

    protected Collection<Solution> getModelSolutions()
    {
        return modelSolutions;
    }

    public Collection<Check> getChecks()
    {
        return checks;
    }
}