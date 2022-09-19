package tech.clegg.gradeer.execution.java;

import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.SinglePrintingAntRunner;
import tech.clegg.gradeer.solution.Solution;
import tech.clegg.gradeer.subject.ClassPath;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class JavaExecutionManager {
    private final JavaExecution javaExecution;
    private int waitAfterExecutionTime;

    public JavaExecutionManager(SinglePrintingAntRunner antRunner, ClassExecutionTemplate classExecutionTemplate) {
        this.javaExecution = new JavaExecution(antRunner, classExecutionTemplate);
        this.waitAfterExecutionTime = classExecutionTemplate.getWaitAfterExecutionTime();
    }

    public JavaExecutionManager(Configuration config, ClassExecutionTemplate classExecTemplate, Solution solution) {

        ClassPath classPath = initClassPath(config, classExecTemplate, solution);

        SinglePrintingAntRunner antRunner = new SinglePrintingAntRunner(config, classPath, solution);
        //this.javaExecution = new JavaExecution(antRunner, classExecTemplate);
        this.javaExecution = new JavaExecution(solution, classPath, classExecTemplate, config);
        this.waitAfterExecutionTime = classExecTemplate.getWaitAfterExecutionTime();
    }

    private ClassPath initClassPath(Configuration config, ClassExecutionTemplate classExecTemplate, Solution solution) {
        ClassPath classPath = new ClassPath();
        classPath.add(solution.getDirectory());
        if (config.getRuntimeDependenciesDir() != null && Files.exists(config.getRuntimeDependenciesDir())) {
            classPath.add(config.getRuntimeDependenciesDir());
        }
        String[] additionalCPElems = classExecTemplate.getAdditionalCPElems();
        if (additionalCPElems != null && additionalCPElems.length > 0) {
            for (String elem : classExecTemplate.getAdditionalCPElems()) {
                classPath.add(Paths.get(elem));
            }
        }
        return classPath;
    }

    public void start() {
        javaExecution.start();
        try {
            javaExecution.join();
            TimeUnit.SECONDS.sleep(waitAfterExecutionTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public JavaExecution getJavaExecution() {
        return javaExecution;
    }

    public void stop() {
        javaExecution.interrupt();
    }
}