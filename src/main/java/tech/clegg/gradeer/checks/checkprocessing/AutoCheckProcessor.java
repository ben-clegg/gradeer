package tech.clegg.gradeer.checks.checkprocessing;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.CheckstyleCheck;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.staticanalysis.checkstyle.CheckstyleExecutor;
import tech.clegg.gradeer.execution.staticanalysis.pmd.PMDExecutor;
import tech.clegg.gradeer.solution.Solution;

import java.util.*;
import java.util.stream.Collectors;

public class AutoCheckProcessor extends CheckProcessor
{
    public AutoCheckProcessor(Collection<Check> checks, Configuration configuration)
    {
        super(checks, configuration);
    }

    @Override
    public void runChecks(Solution solution)
    {
        if(checks.isEmpty())
        {
            configuration.getLogFile().writeMessage("No checks in AutoCheckProcessor for solution " + solution.getIdentifier());
            return;
        }

        // Run PMD on student solutions
        if(configuration.isPmdEnabled())
        {
            PMDExecutor pmdExecutor = new PMDExecutor(configuration);
            pmdExecutor.execute(solution);
        }

        // Run Checkstyle on student solutions if present
        runCheckstyle(solution);

        // Execute checks
        if(configuration.isMultiThreadingEnabled())
            checks.parallelStream().forEach(c -> c.run(solution));
        else
            checks.stream().forEach(c -> c.run(solution));

        executedSolutions.add(solution);
        configuration.getTimer().split("Completed auto checks for Solution " + solution.getIdentifier());
    }

    private void runCheckstyle(Solution solution)
    {
        if(presentCheckClasses.contains(CheckstyleCheck.class))
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


}
