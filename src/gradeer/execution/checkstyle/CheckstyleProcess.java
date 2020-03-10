package gradeer.execution.checkstyle;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

    CheckstyleProcess(Solution sut, Path checkstyleXml)
    {
        solution = sut;
        complete = false;
        xml = checkstyleXml;

        messages = new ArrayList<>();

        auditListener = new AuditListener() {
            @Override
            public void auditStarted(AuditEvent auditEvent) {
                // TODO lock thread?
                //System.out.println("Started Checkstyle for Solution " + solution.getIdentifier());
                complete = false;
            }

            @Override
            public void auditFinished(AuditEvent auditEvent) {
                // TODO unlock thread?
                //System.out.println("Finished Checkstyle for Solution " + solution.getIdentifier());
                complete = true;
            }

            @Override
            public void fileStarted(AuditEvent auditEvent) {
                //System.err.println(solution.getSrcDirectory());
                //System.out.println("Started checking file " + auditEvent.getFileName() + " (Solution " + solution.getIdentifier() + ")");
            }

            @Override
            public void fileFinished(AuditEvent auditEvent) {
                System.out.println("Finished checking file " + auditEvent.getFileName() + " (Solution " + solution.getIdentifier() + ")");
            }

            @Override
            public void addError(AuditEvent auditEvent) {
                messages.add(auditEvent.getMessage());
                //errorDetected = true;
                //System.err.println("Message: " + auditEvent.getMessage() +
                //        " ModuleId: " + auditEvent.getModuleId());
            }

            @Override
            public void addException(AuditEvent auditEvent, Throwable throwable) {

            }
        };
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
    }

    protected List<String> getMessages() { return messages; }
}
