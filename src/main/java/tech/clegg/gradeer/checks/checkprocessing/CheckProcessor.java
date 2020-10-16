package tech.clegg.gradeer.checks.checkprocessing;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import tech.clegg.gradeer.auxiliaryprocesses.InspectionCommandProcess;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.java.JavaClassBatchExecutor;
import tech.clegg.gradeer.execution.staticanalysis.checkstyle.CheckstyleExecutor;
import tech.clegg.gradeer.execution.staticanalysis.pmd.PMDExecutor;
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
        if(!checkTypeIsPresent(TestSuiteCheck.class))
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
        if(checkTypeIsPresent(PMDCheck.class))
        {
            PMDExecutor pmdExecutor = new PMDExecutor(configuration);
            pmdExecutor.execute(solution);
        }

        // Run Checkstyle on student solutions if present
        if(checkTypeIsPresent(CheckstyleCheck.class))
            runCheckstyle(solution);


        // Execute non-manual checks
        if(configuration.isMultiThreadingEnabled())
        {
            // Split concurrent compatible and incompatible checks; run separately
            // Concurrent
            checks.parallelStream()
                    .filter(c -> !c.getClass().equals(ManualCheck.class))
                    .filter(Check::isConcurrentCompatible)
                    .forEach(c -> c.run(solution));

            // Single Thread - non-manual
            checks.stream()
                    .filter(c -> !c.getClass().equals(ManualCheck.class))
                    .filter(c -> !c.isConcurrentCompatible())
                    .forEach(c -> c.run(solution));
        }
        else
            checks.stream()
                    .filter(c -> !c.getClass().equals(ManualCheck.class))
                    .forEach(c -> c.run(solution));


        // Run Manual checks - special case
        if(checkTypeIsPresent(ManualCheck.class))
        {
            // Run each of the defined ClassExecutionTemplates
            JavaClassBatchExecutor classExec = generateClassExecutor(solution);
            classExec.runClasses();

            // Run inspection command (e.g. vscode)
            runInspectionCommand(solution);

            // Run manual checks
            checks.stream()
                    .filter(c -> c.getClass().equals(ManualCheck.class))
                    .forEach(c -> c.run(solution));

            // Stop running classes
            classExec.stopExecutions();

            // Restart ManualChecks if selected
            restartManualChecks(solution);
        }

        executedSolutions.add(solution);
        configuration.getTimer().split("Completed checks for Solution " + solution.getIdentifier());
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

    private boolean checkTypeIsPresent(Class<? extends Check> checkType)
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
            Collection<Check> manualChecks = checks.stream()
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
