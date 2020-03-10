package gradeer.execution.checkstyle;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import gradeer.configuration.Configuration;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckstyleExecutor
{
    private static Logger logger = LogManager.getLogger(CheckstyleExecutor.class);

    private Configuration configuration;
    private Map<Solution, List<String>> messagesMap;


    public CheckstyleExecutor(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void execute(Solution solution)
    {
        CheckstyleProcess checkstyleProcess = new CheckstyleProcess(solution, configuration.getCheckstyleXml());
        checkstyleProcess.run();
        messagesMap.put(solution, checkstyleProcess.getMessages());
    }

    public List<String> getMessages(Solution solution)
    {
        return messagesMap.get(solution);
    }
}

