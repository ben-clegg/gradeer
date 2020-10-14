package tech.anonymousname.gradeer.checks.checkprocessing;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import tech.anonymousname.gradeer.checks.Check;
import tech.anonymousname.gradeer.checks.PMDCheck;
import tech.anonymousname.gradeer.checks.checkresults.CheckResult;
import tech.anonymousname.gradeer.checks.CheckstyleCheck;
import tech.anonymousname.gradeer.checks.TestSuiteCheck;
import tech.anonymousname.gradeer.configuration.Configuration;
import tech.anonymousname.gradeer.execution.staticanalysis.checkstyle.CheckstyleExecutor;
import tech.anonymousname.gradeer.execution.staticanalysis.pmd.PMDExecutor;
import tech.anonymousname.gradeer.solution.Solution;

import java.util.*;
import java.util.stream.Collectors;

public class CheckProcessor
{
    final Collection<Check> checks;
    final Configuration configuration;
    final Set<Solution> executedSolutions;

    public CheckProcessor(Collection<Check> checks, Configuration configuration)
    {
        this.checks = sortChecks(checks);
        this.configuration = configuration;
        this.executedSolutions = new HashSet<>();
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
        if(checks.stream().noneMatch(c -> c.getClass().equals(TestSuiteCheck.class)))
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

    public void runChecks(Solution solution)
    {
        if(checks.isEmpty())
        {
            configuration.getLogFile().writeMessage("No checks in AutoCheckProcessor for solution " + solution.getIdentifier());
            return;
        }

        // Run PMD on student solutions
        if(checks.stream().anyMatch(c -> c.getClass().equals(PMDCheck.class)))
        {
            PMDExecutor pmdExecutor = new PMDExecutor(configuration);
            pmdExecutor.execute(solution);
        }

        // Run Checkstyle on student solutions if present
        if(checks.stream().anyMatch(c -> c.getClass().equals(CheckstyleCheck.class)))
            runCheckstyle(solution);

        // Execute checks
        if(configuration.isMultiThreadingEnabled())
        {
            // Split concurrent compatible and incompatible checks; run separately
            // Concurrent
            checks.parallelStream().filter(Check::isConcurrentCompatible).forEach(c -> c.run(solution));
            // Single Thread
            checks.stream().filter(c -> !c.isConcurrentCompatible()).forEach(c -> c.run(solution));
        }
        else
            checks.stream().forEach(c -> c.run(solution));

        executedSolutions.add(solution);
        configuration.getTimer().split("Completed auto checks for Solution " + solution.getIdentifier());
    }

    // TODO move to preprocessor
    private void runCheckstyle(Solution solution)
    {

        CheckstyleExecutor checkstyleExecutor = new CheckstyleExecutor(configuration,
                getChecks().stream()
                        .filter(c -> c instanceof CheckstyleCheck)
                        .map(c -> (CheckstyleCheck) c)
                        .collect(Collectors.toList()));
        try
        {
            checkstyleExecutor.execute(solution);
        }
        catch (CheckstyleException checkstyleException)
        {
            // Log the file
            configuration.getLogFile().writeMessage("Checkstyle process error on solution " + solution.getIdentifier());
            configuration.getLogFile().writeException(checkstyleException);
            // Set solution as failing all checkstyle checks
            for (CheckstyleCheck c : checkstyleExecutor.getCheckstyleChecks())
                c.setSolutionAsFailed(solution);
        }

    }

}
