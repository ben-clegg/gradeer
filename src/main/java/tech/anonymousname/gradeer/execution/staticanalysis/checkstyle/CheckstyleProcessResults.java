package tech.anonymousname.gradeer.execution.staticanalysis.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import tech.anonymousname.gradeer.checks.CheckstyleCheck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.*;

public class CheckstyleProcessResults
{
    private static Logger logger = LogManager.getLogger(CheckstyleProcessResults.class);

    private List<String> messages;
    private Map<CheckstyleCheck, Map<Path, Integer>> violations;
    private Collection<AuditEvent> checklessAuditEvents;

    public CheckstyleProcessResults()
    {
        messages = new ArrayList<>();
        this.violations = new HashMap<>();
        this.checklessAuditEvents = new ArrayList<>();
    }

    public List<String> getMessages()
    {
        return messages;
    }

    public Map<CheckstyleCheck, Map<Path, Integer>> getViolations()
    {
        return violations;
    }

    public Collection<AuditEvent> getChecklessAuditEvents()
    {
        return checklessAuditEvents;
    }

    public void logViolations()
    {
        for (CheckstyleCheck c : violations.keySet())
        {
            for (Map.Entry<Path, Integer> e : violations.get(c).entrySet())
            {
                logger.info(c.getName() + " : " + e);
            }
        }
    }
}
