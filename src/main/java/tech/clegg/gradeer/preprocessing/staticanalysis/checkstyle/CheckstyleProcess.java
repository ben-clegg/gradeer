package tech.clegg.gradeer.preprocessing.staticanalysis.checkstyle;

import com.puppycrawl.tools.checkstyle.*;
import com.puppycrawl.tools.checkstyle.api.*;
import tech.clegg.gradeer.solution.Solution;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CheckstyleProcess
{
    private Solution solution;
    private boolean complete;
    private Path xml;

    private AuditListener auditListener;

    private CheckstyleProcessResults results;

    CheckstyleProcess(Solution sut, tech.clegg.gradeer.configuration.Configuration configuration)
    {
        this.solution = sut;
        this.complete = false;
        this.xml = configuration.getCheckstyleXml();

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
                System.out.println("Finished checking file " + auditEvent.getFileName() + " (Solution " + solution.getIdentifier() + ")");
            }

            @Override
            public void addError(AuditEvent auditEvent) {
                results.addAuditEvent(auditEvent);
            }

            @Override
            public void addException(AuditEvent auditEvent, Throwable throwable) {
            }
        };
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
                    try
                    {
                        rootModule.wait();
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        catch (CheckstyleException chEx)
        {
            chEx.printStackTrace();

            System.err.println("CheckStyle encountered a severe error for this solution, " +
                    "likely because it did not compile.");
            System.err.println("Setting all relevant check results' scores to 0 and leaving " +
                    "feedback that it could not be evaluated.");
            results.setSevereError();
        }

        //results.logViolations();
    }

    public CheckstyleProcessResults getResults()
    {
        return results;
    }

}

