package tech.clegg.gradeer.checks.checkprocessing;

import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.preprocessing.PreProcessor;
import tech.clegg.gradeer.preprocessing.generation.PreProcessorGenerator;
import tech.clegg.gradeer.solution.Solution;
import tech.clegg.gradeer.checks.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class CheckProcessor
{
    final Collection<Check> allChecks;
    private Map<Integer, Collection<Check>> checkGroups;
    final Configuration configuration;
    final Set<Solution> executedSolutions;

    public CheckProcessor(Collection<Check> checks, Configuration configuration)
    {
        groupChecksByPriority(checks);
        this.allChecks = checks;
        this.configuration = configuration;
        this.executedSolutions = new HashSet<>();
    }

    private void groupChecksByPriority(Collection<Check> checkCollection)
    {
        checkGroups = new TreeMap<>();
        for (Check c : checkCollection)
        {
            int priority = c.getPriority();
            checkGroups.computeIfAbsent(priority, k -> new HashSet<>());
            checkGroups.get(priority).add(c);
        }
    }

    /**
     * Determine if a Solution fails every single defined unit test
     * @param solution the Solution to evaluate
     * @return true if solution does not pass a single unit test, false otherwise
     */
    public boolean failsAllUnitTests(Solution solution)
    {
        // Only fails if TestSuiteChecks are present
        if(!checkTypeIsPresent(TestSuiteCheck.class))
            return false;

        // Doesn't fail if any unit test passes
        for (Check c : getAllChecks())
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

    public Collection<Check> getAllChecks()
    {
        return allChecks;
    }

    public void runChecks(Solution solution)
    {
        // Check if no checks to run
        if(getAllChecks().isEmpty())
        {
            configuration.getLogFile().writeMessage("No checks to process for solution " + solution.getIdentifier());
            return;
        }

        // Store checks that did not have restored CheckResults; i.e. are executed now
        Collection<Check> currentlyExecutedChecks = new ArrayList<>();

        // Generate & run PreProcessors
        Collection<PreProcessor> preProcessors =
                new PreProcessorGenerator(getAllChecks(), configuration)
                        .generate(solution);
        preProcessors.forEach(PreProcessor::start);

        // Run individual Check groups, from highest priority to lowest
        List<Integer> priorityValues = new ArrayList<>(checkGroups.keySet());
        priorityValues.sort(Collections.reverseOrder());
        for (int p : priorityValues)
        {
            currentlyExecutedChecks.addAll(
                    runCheckGroup(solution, checkGroups.get(p))
            );
        }

        // Stop PreProcessors
        preProcessors.forEach(PreProcessor::stop);
        // TODO Only execute PreProcessors while they are necessary

        // Restart ManualChecks if selected & just executed
        if(checkTypeIsPresent(ManualCheck.class, currentlyExecutedChecks))
            restartManualChecks(solution);

        executedSolutions.add(solution);
        configuration.getTimer().split("Completed checks for Solution " + solution.getIdentifier());
    }

    /**
     *
     * @param solution
     * @param checks
     * @return the Checks that were not previously executed (i.e. just executed by running this method)
     */
    private Collection<Check> runCheckGroup(Solution solution, Collection<Check> checks)
    {
        // Determine which checks have not already been executed
        Collection<Check> executedChecks = checks.stream()
                .filter(c -> !solution.hasCheckResult(c))
                .collect(Collectors.toList());

        // Run all checks procedurally if multithreading is disabled
        if(!configuration.isMultiThreadingEnabled())
        {
            for (Check c : checks)
            {
                c.run(solution);
            }
            return executedChecks;
        }

        // Split concurrent and non-concurrent checks
        Collection<Check> concurrent = checks.stream()
                .filter(Check::isConcurrentCompatible)
                .collect(Collectors.toList());
        Collection<Check> nonConcurrent = checks.stream()
                .filter(c -> !c.isConcurrentCompatible())
                .collect(Collectors.toList());

        concurrent.parallelStream().forEach(c -> c.run(solution));
        nonConcurrent.forEach(c -> c.run(solution));
        return executedChecks;
    }

    private boolean checkTypeIsPresent(Class<? extends Check> checkType)
    {
        return checkTypeIsPresent(checkType, getAllChecks());
    }

    private boolean checkTypeIsPresent(Class<? extends Check> checkType, Collection<Check> checks)
    {
        return checks.stream().anyMatch(c -> c.getClass().equals(checkType));
    }

    private boolean allCheckResultsAlreadyPresent(Solution solution)
    {
        return getAllChecks().stream().allMatch(solution::hasCheckResult);
    }

    private void restartManualChecks(Solution solution)
    {
        final boolean CHECK_CONFIRM = false;


        // Check if should restart
        System.out.println("Restart manual checks for Solution " + solution.getIdentifier() + "?");
        System.out.println("(Y)es / (N)o");
        boolean restart = promptResponse();

        // Confirm
        boolean confirmed = true;
        if(CHECK_CONFIRM)
        {
            System.out.println("Are you sure?");
            System.out.println("(Y)es / (N)o");
            confirmed = promptResponse();
        }
        if(!confirmed)
            restartManualChecks(solution);

        // Perform restart
        else if(restart)
        {
            Collection<Check> manualChecks = getAllChecks().stream()
                    .filter(c -> c.getClass().equals(ManualCheck.class))
                    .collect(Collectors.toSet());
            // Clear existing manual check results for solution
            solution.clearChecks(manualChecks);
            // Re-run checks
            runChecks(solution);
        }

    }

    private boolean promptResponse()
    {
        // Get input
        Scanner scanner = new Scanner(System.in);

        if(!scanner.hasNext())
        {
            System.err.println("No input provided!");
            System.err.println("Please re-enter.");
            return promptResponse();
        }

        String input = scanner.next().trim().toLowerCase();

        if(input.isEmpty())
        {
            System.out.println("No input provided!");
            System.err.println("Please re-enter.");
            return promptResponse();
        }

        if(input.equals("n") || input.equals("no"))
            return false;
        if(input.equals("y") || input.equals("yes"))
            return true;

        System.out.println("Invalid input!");
        System.err.println("Please re-enter.");
        return promptResponse();
    }

}
