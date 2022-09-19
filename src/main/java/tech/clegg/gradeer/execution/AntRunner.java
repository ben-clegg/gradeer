package tech.clegg.gradeer.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.configuration.Environment;
import tech.clegg.gradeer.solution.Solution;
import tech.clegg.gradeer.subject.ClassPath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        this.classPath.addAll(configuration.getBuiltLibComponents());
    }

    public AntProcessResult compile(Solution solution)
    {
        List<String> command = commonCommand("compile");
        command.add("-Dsrc.dir=" + solution.getDirectory());
        command.add("-Dclass.dir=" + solution.getDirectory());

        logger.debug(command);
        return runAntProcess(command);
    }

    public AntProcessResult compile(Path testDirectory)
    {
        List<String> command = commonCommand("compile");
        command.add("-Dsrc.dir=" + testDirectory);
        command.add("-Dclass.dir=" + testDirectory);

        logger.debug(command);
        return runAntProcess(command);
    }


    public List<String> commonCommand(String targetName)
    {
        List<String> command = new ArrayList<>();
        command.add(Environment.getAntExecutable().toString());
        command.add(targetName);
        command.add("-Dgradeer.home.dir=" + Environment.getGradeerHomeDir());
        command.add("-Dadditional.cp=" + classPath.toString());

        if(config.getRuntimeDependenciesDir() != null &&
                Files.exists(config.getRuntimeDependenciesDir()))
            command.add("-Druntime.deps=" + config.getRuntimeDependenciesDir());
        else
            command.add("-Druntime.deps=" + config.getRootDir());

        if(config.getSourceDependenciesDir() != null &&
                Files.exists(config.getSourceDependenciesDir()))
            command.add("-Dsource.deps=" + config.getSourceDependenciesDir());
        else
            command.add("-Dsource.deps=");

        return command;

    }

    protected ProcessBuilder generateProcessBuilder(List<String> command)
    {
        // Remove empty ant flags for windows compatibility
        command = command.stream()
                .filter(s -> !s.startsWith("-D") || !s.endsWith("="))
                .collect(Collectors.toList());

        // Make process
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(command);
        pb.directory(Environment.getGradeerHomeDir().toFile());
        pb.redirectErrorStream(true);

        return pb;
    }

    public AntProcessResult runAntProcess(List<String> command)
    {
        ProcessBuilder pb = generateProcessBuilder(command);

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

    Configuration getConfiguration()
    {
        return config;
    }
}
