    package tech.clegg.gradeer.checks.checkprocessing;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.ManualCheck;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.results.io.DelayedFileWriter;
import tech.clegg.gradeer.solution.Solution;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

    public class CheckValidator
{
    private final Collection<Solution> modelSolutions;
    private final Configuration configuration;

    public CheckValidator(Collection<Solution> modelSolutions, Configuration configuration)
    {
        this.modelSolutions = modelSolutions;
        this.configuration = configuration;
    }

    public Collection<Check> filterValidChecks(Collection<Check> checks)
    {
        Collection<String> checkIssues = new ArrayList<>();
        Collection<Check> checkPool = new ArrayList<>(checks);
        Collection<Check> validChecks = new HashSet<>();

        // Manual Checks are a special case ; skipped
        Collection<Check> manualChecks = checks.stream()
                .filter(c -> c.getClass().equals(ManualCheck.class))
                .collect(Collectors.toSet());
        validChecks.addAll(manualChecks);
        checkPool.removeAll(manualChecks);

        // Find checks that do not achieve a score of 1.0 on any model solution
        for (Solution m : modelSolutions)
        {
            Collection<Check> toRemove = new ArrayList<>();
            CheckProcessor checkProcessor = new CheckProcessor(checkPool, configuration);
            checkProcessor.runChecks(m);
            for(Check c : checkPool)
            {
                // Any non-perfect score is an invalid check
                if (m.getCheckResult(c).getUnweightedScore() < 1.0)
                {
                    checkIssues.add(c.identifier() + " is invalid (base score = " +
                            m.getCheckResult(c).getUnweightedScore() + ")");
                    toRemove.add(c);
                }
            }
            checkPool.removeAll(toRemove);
        }

        // Add remaining checks, report duplicates
        for (Check c : checkPool)
        {
            if(validChecks.stream().anyMatch(v -> v.identifier().equals(c.identifier())))
                checkIssues.add("There are multiple checks called " + c.identifier() + "; it is possible only one will be used if invalid checks are removed.");
            validChecks.add(c);
        }

        // Report invalid checks
        writeCheckIssuesReport(checkIssues);

        // Only return valid checks
        if(configuration.isRemoveInvalidChecks())
        {
            return validChecks;
        }
        // Only report invalid checks and not remove them
        return checks;
    }

    private void writeCheckIssuesReport(Collection<String> checkIssues)
    {
        DelayedFileWriter delayedFileWriter = new DelayedFileWriter();
        for (String s : checkIssues)
            delayedFileWriter.addLine(s);

        final Path reportLoc = Paths.get(configuration.getOutputDir() + File.separator + "reportedCheckIssues.log");

        if(Files.notExists(reportLoc))
            reportLoc.getParent().toFile().mkdirs();
        delayedFileWriter.write(reportLoc);
    }
}
