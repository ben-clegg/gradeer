package gradeer.execution;

import gradeer.configuration.Configuration;
import gradeer.configuration.Environment;
import gradeer.execution.junit.TestSuite;
import gradeer.io.ClassPath;
import gradeer.io.JavaSource;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes specific targets in the provided ant build.xml.
 * Primarily used for runtime automated test execution, though it can be used to execute custom ant targets.
 */
public class AntRunner
{
    private static Logger logger = LogManager.getLogger(AntRunner.class);

    private Configuration config;
    private ClassPath classPath;

    public AntRunner(Configuration configuration, ClassPath classPath)
    {
        config = configuration;
        this.classPath = classPath;



    }

    public AntProcessResult compile(Solution solution)
    {
        List<String> command = commonCommand("compile");
        command.add("-Dsrc.dir=" + solution.getDirectory());
        command.add("-Dclass.dir=" + solution.getDirectory());

        logger.info(command);
        return runAntProcess(command);
    }

    public AntProcessResult compile(Path testDirectory)
    {
        List<String> command = commonCommand("compile");
        command.add("-Dsrc.dir=" + testDirectory);
        command.add("-Dclass.dir=" + testDirectory);

        logger.info(command);
        return runAntProcess(command);
    }

    public AntProcessResult runTest(TestSuite test, Solution solution)
    {
        String packagePrefix = test.getPackage();
        if(!packagePrefix.isEmpty())
            packagePrefix = packagePrefix + ".";
        List<String> command = commonCommand("run-test");
        command.add("-Dtest.class.name=" + packagePrefix + test.getBaseName());
        command.add("-Dtest.class.dir=" + test.getClassFile().getParent().toString());
        command.add("-Dsource.dir=" + solution.getDirectory());
        command.add("-Dtest.dir=" + config.getTestsDir());

        return runAntProcess(command);
    }

    public List<String> commonCommand(String targetName)
    {
        List<String> command = new ArrayList<>();
        command.add(Environment.getAntExecutable().toString());
        command.add(targetName);
        command.add("-Dgradeer.home.dir=" + Environment.getGradeerHomeDir());
        command.add("-Dadditional.cp=" + classPath.toString());

        if(config.getDependenciesDir() != null &&
                Files.exists(config.getDependenciesDir()))
            command.add("-Druntime.deps=" + config.getDependenciesDir());
        else
            command.add("-Druntime.deps=");
        // TODO add source dependencies dir from config

        return command;

    }

    public AntProcessResult runAntProcess(List<String> command)
    {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(command);
        pb.directory(Environment.getGradeerHomeDir().toFile());
        pb.redirectErrorStream(true);

        logger.info("Running ant command: " + command + " from " + pb.directory());

        AntProcessResult res = new AntProcessResult();
        try
        {
            Process p = pb.start();

            BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
            res.setInputStream(is);

            String line;
            BufferedReader es = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder esLog = new StringBuilder();
            while ((line = es.readLine()) != null) {
                esLog.append(line).append(System.lineSeparator());
            }
            res.setErrorStreamText(esLog.toString());
        }
        catch (IOException ioEx)
        {
            res.setExceptionText(String.format("Exception: %s%s", ioEx.toString(), System.lineSeparator()));
        }

        return res;
    }
}
