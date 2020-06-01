package tech.clegg.gradeer.execution.java;

import tech.clegg.gradeer.execution.AntProcessResult;
import tech.clegg.gradeer.execution.AntRunner;
import tech.clegg.gradeer.execution.SingleAntRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaExecutor
{
    private final JavaExecution javaExecution;

    public JavaExecutor(SingleAntRunner antRunner, ClassExecutionTemplate classExecutionTemplate)
    {
        this.javaExecution = new JavaExecution(antRunner, classExecutionTemplate);
    }

    public void start()
    {
        javaExecution.start();
    }

    public void stop()
    {
        javaExecution.interrupt();
    }
}

class JavaExecution extends Thread
{
    private final SingleAntRunner antRunner;
    private final ClassExecutionTemplate classExecutionTemplate;

    JavaExecution(SingleAntRunner antRunner, ClassExecutionTemplate classExecutionTemplate)
    {
        this.antRunner = antRunner;
        this.classExecutionTemplate = classExecutionTemplate;
    }

    @Override
    public void run()
    {
        System.out.println("Executing " + classExecutionTemplate.getFullClassName());
        AntProcessResult result = antRunner.runJavaClass(classExecutionTemplate);
        // TODO handle failing AntProcessResult
    }

    @Override
    public void interrupt()
    {
        antRunner.halt();
        super.interrupt();
    }
}