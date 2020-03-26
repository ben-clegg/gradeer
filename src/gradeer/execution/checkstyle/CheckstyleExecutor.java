package gradeer.execution.checkstyle;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import gradeer.checks.Check;
import gradeer.checks.CheckstyleCheck;
import gradeer.configuration.Configuration;
import gradeer.solution.Solution;
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
        checkstyleProcess.run();
        solution.setCheckstyleProcessResults(checkstyleProcess.getResults());
        for (Check c : checkstyleChecks)
            c.run(solution);
    }
}

