package tech.clegg.gradeer.execution.java;

import tech.clegg.gradeer.execution.AntProcessResult;
import tech.clegg.gradeer.execution.AntRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaExecutor
{
    private final JavaExecution javaExecution;
    private final ExecutorService executorService;

    public JavaExecutor(AntRunner antRunner, ClassExecutionTemplate classExecutionTemplate)
    {
        this.javaExecution = new JavaExecution(antRunner, classExecutionTemplate);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void start()
    {
        executorService.submit(javaExecution);
        executorService.shutdown();
    }

    public void stop()
    {
        executorService.shutdownNow();
    }
}

class JavaExecution implements Runnable
{
    private final AntRunner antRunner;
    private final ClassExecutionTemplate classExecutionTemplate;

    JavaExecution(AntRunner antRunner, ClassExecutionTemplate classExecutionTemplate)
    {
        this.antRunner = antRunner;
        this.classExecutionTemplate = classExecutionTemplate;
    }

    @Override
    public void run()
    {
        System.out.println("Executing " + classExecutionTemplate.getFullClassName());
        AntProcessResult result = antRunner.runJavaClass(classExecutionTemplate);
    }
}