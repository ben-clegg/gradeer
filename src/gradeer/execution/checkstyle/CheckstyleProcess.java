package gradeer.execution.checkstyle;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import gradeer.checks.Check;
import gradeer.checks.CheckstyleCheck;
import gradeer.checks.exceptions.NoCheckException;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CheckstyleProcess
{
    private static Logger logger = LogManager.getLogger(CheckstyleProcess.class);

    private Solution solution;
    private boolean complete;
    private Path xml;

    private final Checker checker = new Checker();

    private List<String> messages;
    private AuditListener auditListener;

    private Collection<CheckstyleCheck> checkstyleChecks;
    private Map<CheckstyleCheck, Map<Path, Integer>> violations;

    CheckstyleProcess(Solution sut, Path checkstyleXml, Collection<CheckstyleCheck> checkstyleChecks)
    {
        this.solution = sut;
        this.complete = false;
        this.xml = checkstyleXml;
        this.violations = new HashMap<>();

        this.checkstyleChecks = checkstyleChecks;

        messages = new ArrayList<>();

        auditListener = new AuditListener() {
            @Override
            public void auditStarted(AuditEvent auditEvent) {
                // TODO lock thread?
                complete = false;
            }

            @Override
            public void auditFinished(AuditEvent auditEvent) {
                // TODO unlock thread?
                complete = true;
            }

            @Override
            public void fileStarted(AuditEvent auditEvent) {
            }

            @Override
            public void fileFinished(AuditEvent auditEvent) {
                logger.info("Finished checking file " + auditEvent.getFileName() + " (Solution " + solution.getIdentifier() + ")");
            }

            @Override
            public void addError(AuditEvent auditEvent) {
                /*
                messages.add(auditEvent.getMessage());
                logger.info(auditEvent.getModuleId() + "-" + auditEvent.getMessage());
                logger.info(auditEvent.getSourceName());
                logger.info(auditEvent.getFileName());
                 */

                try
                {
                    CheckstyleCheck c = getCheckForAuditEvent(auditEvent);
                    logger.info("Matched check " + c);
                    addViolation(c, Paths.get(auditEvent.getFileName()));
                }
                catch (NoCheckException noCheckEx)
                {
                    logger.error(noCheckEx);
                    //noCheckEx.printStackTrace();
                }
            }

            @Override
            public void addException(AuditEvent auditEvent, Throwable throwable) {
            }
        };
    }

    private void addViolation(CheckstyleCheck check, Path source)
    {
        // Initialise for check if non-existent.
        if(violations.get(check) == null || violations.get(check).isEmpty())
            violations.put(check, new HashMap<>());

        violations.get(check).putIfAbsent(source, 0);
        int existing = violations.get(check).get(source);
        violations.get(check).put(source, existing + 1);
    }

    protected void run()
    {
        List<File> javaFiles = solution.getSources()
                .stream().map(s -> s.getJavaFile().toFile())
                .collect(Collectors.toList());

        if (javaFiles.isEmpty())
        {
            System.err.println("No java files found for solution " + solution);
            return;
        }

        checker.addListener(auditListener);

        try {
            checker.setModuleClassLoader(ClassLoader.getSystemClassLoader());
            checker.configure(ConfigurationLoader.loadConfiguration(xml.toString(), new PropertyResolver() {
                @Override
                public String resolve(String s)
                {
                    logger.info("Resolved property: " + s);
                    return null;
                }
            }));
        } catch (CheckstyleException e) {
            e.printStackTrace();
            return;
        }

        // Run Checkstyle
        try
        {
            synchronized (checker)
            {
                System.err.println("Running checkstyle for " + solution.getIdentifier());
                checker.process(javaFiles);
                while (!complete)
                {
                    try {
                        checker.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        catch (CheckstyleException chEx)
        {
            chEx.printStackTrace();
        }

        for (CheckstyleCheck c : violations.keySet())
        {
            for (Map.Entry<Path, Integer> e : violations.get(c).entrySet())
            {
                logger.info(c.getName() + " : " + e);
            }
        }
    }

    protected List<String> getMessages() { return messages; }


    private CheckstyleCheck getCheckForAuditEvent(AuditEvent auditEvent) throws NoCheckException
    {
        for (CheckstyleCheck c : checkstyleChecks)
        {
            if(nameMatchesAuditEvent(c, auditEvent))
                return c;
        }
        throw new NoCheckException("No check exists for AuditEvent " + auditEvent.getSourceName() + "/" + auditEvent.getModuleId());
    }

    private static boolean nameMatchesAuditEvent(CheckstyleCheck checkstyleCheck, AuditEvent auditEvent)
    {
        if(checkstyleCheck.getName() == null || checkstyleCheck.getName().isEmpty())
            return false;
        if(checkstyleCheck.getName().equals(auditEvent.getModuleId()))
            return true;

        // Checkstyle source names are often in the form
        // "com.puppycrawl.tools.checkstyle.checks.whitespace.FileTabCharacterCheck"

        logger.info(auditEvent.getSourceName());
        String[] splitSourceName = auditEvent.getSourceName().split("\\.");
        logger.info(Arrays.toString(splitSourceName));
        String sourceNameEnding = splitSourceName[splitSourceName.length - 1];
        if(checkstyleCheck.getName().equals(sourceNameEnding) ||
                checkstyleCheck.getName().equals(sourceNameEnding.replace("Check", "")))
            return true;
        return false;
    }
}
