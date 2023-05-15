package tech.clegg.gradeer.execution.java;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.OutputMonitoringThread;
import tech.clegg.gradeer.solution.Solution;
import tech.clegg.gradeer.subject.ClassPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JavaExecution extends Thread {

    private final Logger logger = LogManager.getLogger(JavaExecution.class);

    private final ClassExecutionTemplate classExecutionTemplate;

    private Process process;
    private final Solution solution;
    private final ClassPath classPath;
    private final Configuration configuration;

    JavaExecution(
            Solution solution,
            ClassPath classPath,
            ClassExecutionTemplate classExecutionTemplate,
            Configuration configuration
    ) {
        this.solution = solution;
        this.classPath = classPath;
        this.classExecutionTemplate = classExecutionTemplate;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        System.out.println("Executing " + classExecutionTemplate.getFullClassName());
        try {
            String[] command = generateCommand(classPath, classExecutionTemplate);
            logger.debug("Java command: " + StringUtils.join(command, " "));
            process = Runtime.getRuntime()
                    .exec(command, new String[]{}, solution.getDirectory().toFile());
            OutputMonitoringThread outputMonitoringThread = new OutputMonitoringThread(
                    process.getInputStream(),
                    process.getErrorStream(),
                    Paths.get(configuration.getSolutionCapturedOutputDir() +
                            File.separator + solution.getIdentifier() + "-output.txt")
            );
            outputMonitoringThread.start();
            outputMonitoringThread.join();
        } catch (IOException e) {
            logger.error("Encountered error while running pre-manual class for solution {}",
                    solution.getIdentifier(), e);
        } catch (InterruptedException e) {
            logger.error("Interrupted OutputMonitoringThread for solution {}",
                    solution.getIdentifier(), e);
        }
    }

    private String[] generateCommand(
            ClassPath classPath,
            ClassExecutionTemplate classExecutionTemplate
    ) {
        List<String> command = new ArrayList<>();
        command.add("java");

        if (classPath != null && !classPath.isEmpty()) {
            command.add("-cp");
            command.add(classPath.toString());
        }

        command.add(classExecutionTemplate.getFullClassName());

        String args[] = classExecutionTemplate.getArgs();
        if (args != null && args.length > 0) {
            for (String arg : args)
                command.add(arg);
        }

        return command.toArray(new String[0]);
    }

    @Override
    public void interrupt() {
        process.destroy();
        super.interrupt();
        try {
            this.join();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
