package tech.anonymousname.gradeer.execution.staticanalysis.checkstyle;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import tech.anonymousname.gradeer.checks.CheckstyleCheck;
import tech.anonymousname.gradeer.configuration.Configuration;
import tech.anonymousname.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CheckstyleExecutor
{
    private static Logger logger = LogManager.getLogger(CheckstyleExecutor.class);

    private Configuration configuration;
    private Collection<CheckstyleCheck> checkstyleChecks;


    public CheckstyleExecutor(Configuration configuration, Collection<CheckstyleCheck> checkstyleChecks)
    {
        this.configuration = configuration;
        this.checkstyleChecks = checkstyleChecks;
    }

    public void execute(Solution solution) throws CheckstyleException
    {
        CheckstyleProcess checkstyleProcess = new CheckstyleProcess(solution, configuration, checkstyleChecks);

        checkstyleProcess.run();

        // Update check results for this solution
        solution.setCheckstyleProcessResults(checkstyleProcess.getResults());
        for (CheckstyleCheck c : checkstyleChecks)
            c.execute(solution);
    }

    public Collection<CheckstyleCheck> getCheckstyleChecks()
    {
        return checkstyleChecks;
    }
}

