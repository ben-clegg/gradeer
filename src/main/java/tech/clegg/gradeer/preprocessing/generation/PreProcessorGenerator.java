package tech.clegg.gradeer.preprocessing.generation;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.preprocessing.*;
import tech.clegg.gradeer.solution.Solution;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

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

        // Filter checks to only include those that haven't already been executed for the solution
        Collection<Check> unexecutedChecks = checks.stream()
                .filter(c -> !solution.hasCheckResult(c))
                .collect(Collectors.toList());

        // Identify PreProcessor types to generate from Checks
        Collection<Class<? extends PreProcessor>> preProcessorTypes = new HashSet<>();
        unexecutedChecks.forEach(c -> preProcessorTypes.addAll(c.getPreProcessorTypes()));

        // Generate PreProcessors
        Collection<PreProcessor> preProcessors = new HashSet<>();
        for (Class<? extends PreProcessor> t : preProcessorTypes)
        {
            try
            {
                PreProcessor p = t.getConstructor(Solution.class, Configuration.class)
                        .newInstance(solution, configuration);
                preProcessors.add(p);
            } catch (InstantiationException e)
            {
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            } catch (InvocationTargetException e)
            {
                e.printStackTrace();
            } catch (NoSuchMethodException e)
            {
                e.printStackTrace();
            }
        }

        return preProcessors;
    }

}
