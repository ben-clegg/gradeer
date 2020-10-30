package tech.clegg.gradeer.checks.checkprocessing;

import tech.clegg.gradeer.auxiliaryprocesses.InspectionCommandProcess;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.java.JavaClassBatchExecutor;
import tech.clegg.gradeer.preprocessing.CheckstylePreProcessor;
import tech.clegg.gradeer.preprocessing.PMDPreProcessor;
import tech.clegg.gradeer.preprocessing.staticanalysis.pmd.PMDExecutor;
import tech.clegg.gradeer.solution.Solution;
import tech.clegg.gradeer.checks.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

        // Run PreProcessors
        // TODO Automatically generate PreProcessors & skip any that do not need to be run for remaining checks to run
        new CheckstylePreProcessor(solution, configuration).start();
        new PMDPreProcessor(solution, configuration).start();

        // Run individual Check groups, from highest priority to lowest
        List<Integer> priorityValues = new ArrayList<>(checkGroups.keySet());
        priorityValues.sort(Collections.reverseOrder());
        for (int p : priorityValues)
        {
            runCheckGroup(solution, checkGroups.get(p));
        }


        // TODO split before and after running checks
        // TODO Ideally wait until the exact group that needs
        // Run Manual check preprocessing - special case
        if(checkTypeIsPresent(ManualCheck.class))
        {
            // Run each of the defined ClassExecutionTemplates
            JavaClassBatchExecutor classExec = generateClassExecutor(solution);
            classExec.runClasses();

            // Run inspection command (e.g. vscode)
            runInspectionCommand(solution);

            // TODO SPLIT HERE

            // Stop running classes
            classExec.stopExecutions();

            // Restart ManualChecks if selected
            restartManualChecks(solution);
        }

        executedSolutions.add(solution);
        configuration.getTimer().split("Completed checks for Solution " + solution.getIdentifier());
    }

    private void runCheckGroup(Solution solution, Collection<Check> checks)
    {
        // Run all checks procedurally if multithreading is disabled
        if(!configuration.isMultiThreadingEnabled())
        {
            for (Check c : checks)
            {
                c.run(solution);
            }
            return;
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
    }

    private boolean checkTypeIsPresent(Class<? extends Check> checkType)
    {
        return checkTypeIsPresent(checkType, getAllChecks());
    }

    private boolean checkTypeIsPresent(Class<? extends Check> checkType, Collection<Check> checks)
    {
        return checks.stream().anyMatch(c -> c.getClass().equals(checkType));
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

    private JavaClassBatchExecutor generateClassExecutor(Solution solution)
    {
        return new JavaClassBatchExecutor(solution, configuration);
    }

    private void runInspectionCommand(Solution solution)
    {
        if(configuration.getInspectionCommand() == null)
            return;
        if(configuration.getInspectionCommand().isEmpty())
            return;

        if(!checkTypeIsPresent(ManualCheck.class))
            return;

        Collection<Path> toInspect = new ArrayList<>();

        // TODO find a more elegant solution for this
        // method inside corresponding classes?

        if(Files.exists(configuration.getTestOutputDir()))
            toInspect.add(Paths.get(configuration.getTestOutputDir() + File.separator + solution.getIdentifier()));
        if(Files.exists(configuration.getMergedSolutionsDir()))
            toInspect.add(Paths.get(configuration.getMergedSolutionsDir() + File.separator + solution.getIdentifier() + ".java"));

        new InspectionCommandProcess(configuration, toInspect).run();
    }

}
