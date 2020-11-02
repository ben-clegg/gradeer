package tech.clegg.gradeer.preprocessing.generation;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.ManualCheck;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.preprocessing.*;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;
import java.util.HashSet;

public class PreProcessorGenerator
{
    private final Collection<Check> checks;
    private final Configuration configuration;

    public PreProcessorGenerator(Collection<Check> checks, Configuration configuration)
    {
        this.checks = checks;
        this.configuration = configuration;
    }

    public Collection<PreProcessor> generate(Solution solution)
    {
        Collection<PreProcessor> preProcessors = new HashSet<>();

        // TODO Filter checks to only include those that haven't already been executed for the solution

        // TODO Automatically generate based on available checks; PreProcessor types are defined in Checks themselves
        preProcessors.add(new CheckstylePreProcessor(solution, configuration));
        preProcessors.add(new PMDPreProcessor(solution, configuration));
        if(checks.stream().anyMatch(c -> c.getClass().equals(ManualCheck.class)))
        {
            preProcessors.add(new JavaBatchExecutorPreProcessor(solution, configuration));
            preProcessors.add(new SourceInspectorPreProcessor(solution, configuration));
        }

        return preProcessors;
    }
}
