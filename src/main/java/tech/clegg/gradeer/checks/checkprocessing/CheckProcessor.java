package tech.clegg.gradeer.checks.checkprocessing;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.TestSuiteCheck;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;

import java.util.*;

public abstract class CheckProcessor
{
    final Collection<Check> checks;
    final Configuration configuration;
    final Set<Solution> executedSolutions;

    final Set<Class<? extends Check>> presentCheckClasses;

    public CheckProcessor(Collection<Check> checks, Configuration configuration)
    {
        this.checks = sortChecks(checks);
        this.configuration = configuration;
        this.executedSolutions = new HashSet<>();
        this.presentCheckClasses = new HashSet<>();

        for (Check c : checks)
            presentCheckClasses.add(c.getClass());
    }

    protected Collection<Check> sortChecks(Collection<Check> checksToSort)
    {
        Map<String, List<Check>> checkTypeGroups = new HashMap<>();
        for (Check c : checksToSort)
        {
            String key = c.getClass().getName();
            if(checkTypeGroups.containsKey(key))
                checkTypeGroups.get(key).add(c);
            else
                checkTypeGroups.put(key, new ArrayList<>(Collections.singletonList(c)));
        }

        Collection<Check> sorted = new ArrayList<>();

        // Sort each list alphabetically
        for (List<Check> l : checkTypeGroups.values())
        {
            l.sort(Comparator.comparing(Check::getName));
            sorted.addAll(l);
        }

        return sorted;
    }

    /**
     * Determine if a Solution fails every single defined unit test
     * @param solution the Solution to evaluate
     * @return true if solution does not pass a single unit test, false otherwise
     */
    public boolean failsAllUnitTests(Solution solution)
    {
        // Only fails if TestSuiteChecks are present
        if(!presentCheckClasses.contains(TestSuiteCheck.class))
            return false;

        // Doesn't fail if any unit test passes
        for (Check c : checks)
        {
            // Skip if not a unit test
            if(!c.getClass().equals(TestSuiteCheck.class))
                continue;

            CheckResult checkResult = solution.getCheckResult(c);
            if(checkResult == null)
                continue;

            if(checkResult.getUnweightedScore() > 0.0)
                return false;
        }

        return true;
    }

    public boolean wasExecuted(Solution solution)
    {
        return executedSolutions.contains(solution);
    }

    public Collection<Check> getChecks()
    {
        return checks;
    }

    public abstract void runChecks(Solution solution);

}
