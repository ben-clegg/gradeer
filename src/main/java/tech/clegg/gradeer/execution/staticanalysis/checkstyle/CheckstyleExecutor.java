package tech.clegg.gradeer.execution.staticanalysis.checkstyle;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import tech.clegg.gradeer.checks.CheckstyleCheck;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;
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

    public void execute(Solution solution)
    {
        CheckstyleProcess checkstyleProcess = new CheckstyleProcess(solution,
                configuration.getCheckstyleXml(), checkstyleChecks);
        try
        {
            checkstyleProcess.run();
        }
        catch (CheckstyleException e)
        {
            e.printStackTrace();
            // TODO Handle checkstyle exceptions more elegantly
        }
        solution.setCheckstyleProcessResults(checkstyleProcess.getResults());
        for (CheckstyleCheck c : checkstyleChecks)
            c.run(solution);
    }
}

