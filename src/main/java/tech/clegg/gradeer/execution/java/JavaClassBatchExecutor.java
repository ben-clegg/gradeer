package tech.clegg.gradeer.execution.java;

import tech.clegg.gradeer.subject.ClassPath;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.SinglePrintingAntRunner;
import tech.clegg.gradeer.solution.Solution;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

public class JavaClassBatchExecutor
{
    private final Solution solution;
    private final Configuration configuration;
    private final Collection<JavaExecutor> javaExecutors;

    public JavaClassBatchExecutor(Solution solution, Configuration configuration)
    {
        this.solution = solution;
        this.configuration = configuration;
        this.javaExecutors = new ArrayList<>();
        init();
    }

    private void init()
    {
        // Skip if no classes to execute
        if(configuration.getPreManualJavaClassesToExecute().isEmpty())
            return;

        ClassPath classPath = new ClassPath();
        classPath.add(solution.getDirectory());
        if(Files.exists(configuration.getRuntimeDependenciesDir()))
            classPath.add(configuration.getRuntimeDependenciesDir());


        for (ClassExecutionTemplate cet : configuration.getPreManualJavaClassesToExecute())
        {
            ClassPath execCP = new ClassPath(classPath);
            if(cet.getAdditionalCPElems().length > 0)
            {
                for (String elem : cet.getAdditionalCPElems())
                    execCP.add(Paths.get(elem));
            }

            // Make a single ant runner - allows for the process to be terminated
            SinglePrintingAntRunner antRunner = new SinglePrintingAntRunner(configuration, execCP);
            javaExecutors.add(new JavaExecutor(antRunner, cet));
        }
    }

    public void runClasses()
    {
        System.out.println("Running classes for solution " + solution.getIdentifier());

        if(javaExecutors.isEmpty())
        {
            System.err.println("No classes marked for execution! Skipping...");
            return;
        }

        javaExecutors.forEach(JavaExecutor::start);

    }

    public void stopExecutions()
    {
        System.out.println("Stopping executions of solution " + solution.getIdentifier());

        if(javaExecutors.isEmpty())
        {
            System.err.println("No classes marked for execution! Skipping...");
            return;
        }

        javaExecutors.forEach(JavaExecutor::stop);
    }

}
