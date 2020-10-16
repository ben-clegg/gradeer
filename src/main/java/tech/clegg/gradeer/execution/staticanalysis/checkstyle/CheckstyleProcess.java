package tech.clegg.gradeer.execution.staticanalysis.checkstyle;

import com.puppycrawl.tools.checkstyle.*;
import com.puppycrawl.tools.checkstyle.api.*;
import tech.clegg.gradeer.checks.CheckstyleCheck;
import tech.clegg.gradeer.checks.exceptions.NoCheckException;
import tech.clegg.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
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

    private AuditListener auditListener;
    private Collection<CheckstyleCheck> checkstyleChecks;

    private CheckstyleProcessResults results;

    CheckstyleProcess(Solution sut, tech.clegg.gradeer.configuration.Configuration configuration, Collection<CheckstyleCheck> checkstyleChecks)
    {
        this.solution = sut;
        this.complete = false;
        this.xml = configuration.getCheckstyleXml();

        this.checkstyleChecks = checkstyleChecks;
        this.results = new CheckstyleProcessResults();


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

                try
                {
                    CheckstyleCheck c = getCheckForAuditEvent(auditEvent);
                    logger.info("Matched check " + c);
                    addViolation(c, Paths.get(auditEvent.getFileName()));
                }
                catch (NoCheckException noCheckEx)
                {
                    //logger.error(noCheckEx);
                    results.getChecklessAuditEvents().add(auditEvent);
                }
            }

            @Override
            public void addException(AuditEvent auditEvent, Throwable throwable) {
            }
        };
    }

    private void addViolation(CheckstyleCheck check, Path source)
    {
        Map<CheckstyleCheck, Map<Path, Integer>> violations = results.getViolations();

        // Initialise for check if non-existent.
        if(violations.get(check) == null || violations.get(check).isEmpty())
            violations.put(check, new HashMap<>());

        violations.get(check).putIfAbsent(source, 0);
        int existing = violations.get(check).get(source);
        violations.get(check).put(source, existing + 1);
    }

    public void run() throws CheckstyleException
    {
        List<File> javaFiles = solution.getSources()
                .stream().map(s -> s.getJavaFile().toFile())
                .collect(Collectors.toList());

        if (javaFiles.isEmpty())
        {
            System.err.println("No java files found for solution " + solution);
            System.err.println("No java files found for solution " + solution);
            return;
        }


        Configuration csConfig = ConfigurationLoader.loadConfiguration(xml.toString(),
                new PropertiesExpander(System.getProperties()),
                ConfigurationLoader.IgnoredModulesOptions.OMIT);

        // TODO Force tab character - required for line length checks, etc
        //((DefaultConfiguration) csConfig).addAttribute("tabWidth", Integer.toString(tabWidth));

        final ClassLoader moduleClassLoader = Checker.class.getClassLoader();
        // final ClassLoader moduleClassLoader = ClassLoader.getSystemClassLoader();

        // "The first module that is run as part of Checkstyle and controls its entire behavior and children."
        final ModuleFactory moduleFactory = new PackageObjectFactory(Checker.class.getPackage().getName(), moduleClassLoader);
        final RootModule rootModule = (RootModule) moduleFactory.createModule(csConfig.getName());

        rootModule.setModuleClassLoader(moduleClassLoader);
        rootModule.configure(csConfig);
        rootModule.addListener(auditListener);

        // Run Checkstyle
        try
        {
            synchronized (rootModule)
            {
                System.out.println("Running checkstyle for " + solution.getIdentifier());
                rootModule.process(javaFiles);
                while (!complete)
                {
                    try {
                        rootModule.wait();
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

        //results.logViolations();
    }

    public CheckstyleProcessResults getResults()
    {
        return results;
    }

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
        String[] splitSourceName = auditEvent.getSourceName().split("\\.");
        String sourceNameEnding = splitSourceName[splitSourceName.length - 1];
        if(checkstyleCheck.getName().equals(sourceNameEnding) ||
                checkstyleCheck.getName().equals(sourceNameEnding.replace("Check", "")))
            return true;
        return false;
    }
}

