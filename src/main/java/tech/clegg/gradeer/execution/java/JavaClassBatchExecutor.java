package tech.clegg.gradeer.execution.java;

import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.results.io.DelayedFileWriter;
import tech.clegg.gradeer.solution.Solution;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JavaClassBatchExecutor {
    private final Solution solution;
    private final Configuration configuration;
    private final Collection<JavaExecutionManager> javaExecutionManagers;

    public JavaClassBatchExecutor(Solution solution, Configuration configuration) {
        this.solution = solution;
        this.configuration = configuration;
        this.javaExecutionManagers = new ArrayList<>();
        init();
    }

    private void init() {
        // Skip if no classes to execute
        if (configuration.getPreManualJavaClassesToExecute().isEmpty())
            return;

        for (ClassExecutionTemplate cet : configuration.getPreManualJavaClassesToExecute()) {
            javaExecutionManagers.add(new JavaExecutionManager(configuration, cet, solution));
        }
    }

    public void runClasses() {
        System.out.println("Running classes for solution " + solution.getIdentifier());

        if (javaExecutionManagers.isEmpty()) {
            System.err.println("No classes marked for execution in pre-processing! Skipping...");
            return;
        }

        javaExecutionManagers.forEach(JavaExecutionManager::start);

    }

    public void stopExecutions() {
        System.out.println("Stopping executions of solution " + solution.getIdentifier());

        if (javaExecutionManagers.isEmpty()) {
            System.err.println("No classes marked for execution! Skipping...");
            return;
        }

        for (JavaExecutionManager je : javaExecutionManagers) {
            je.stop();
        }
    }

    private void storeCapturedOutput(List<String> capturedOutput) {
        DelayedFileWriter w = new DelayedFileWriter(capturedOutput);
        w.write(Paths.get(configuration.getSolutionCapturedOutputDir() +
                File.separator + solution.getIdentifier() + "-output.txt"));
    }

}
