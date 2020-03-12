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
    private Map<Solution, List<String>> messagesMap;
    private Collection<CheckstyleCheck> checkstyleChecks;


    public CheckstyleExecutor(Configuration configuration, Collection<CheckstyleCheck> checkstyleChecks)
    {
        this.configuration = configuration;
        this.messagesMap = new HashMap<>();
        this.checkstyleChecks = checkstyleChecks;
    }

    public void execute(Solution solution)
    {
        CheckstyleProcess checkstyleProcess = new CheckstyleProcess(solution, configuration.getCheckstyleXml(), checkstyleChecks);
        checkstyleProcess.run();
        messagesMap.put(solution, checkstyleProcess.getMessages());
    }

    public List<String> getMessages(Solution solution)
    {
        return messagesMap.get(solution);
    }

    public void showMessages(Solution solution)
    {
        getMessages(solution).forEach(m -> {
            logger.info(solution.getIdentifier() + " : " + m);
        });
    }
}

