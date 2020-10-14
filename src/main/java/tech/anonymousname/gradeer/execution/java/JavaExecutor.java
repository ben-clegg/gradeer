package tech.anonymousname.gradeer.execution.java;

import tech.anonymousname.gradeer.execution.SinglePrintingAntRunner;

import java.util.concurrent.TimeUnit;

public class JavaExecutor
{
    private final JavaExecution javaExecution;
    private int waitAfterExecutionTime;

    public JavaExecutor(SinglePrintingAntRunner antRunner, ClassExecutionTemplate classExecutionTemplate)
    {
        this.javaExecution = new JavaExecution(antRunner, classExecutionTemplate);
        this.waitAfterExecutionTime = classExecutionTemplate.getWaitAfterExecutionTime();
    }

    public void start()
    {
        javaExecution.start();
        try
        {
            TimeUnit.SECONDS.sleep(waitAfterExecutionTime);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void stop()
    {
        javaExecution.interrupt();
    }
}

class JavaExecution extends Thread
{
    private final SinglePrintingAntRunner antRunner;
    private final ClassExecutionTemplate classExecutionTemplate;

    JavaExecution(SinglePrintingAntRunner antRunner, ClassExecutionTemplate classExecutionTemplate)
    {
        this.antRunner = antRunner;
        this.classExecutionTemplate = classExecutionTemplate;
    }

    @Override
    public void run()
    {
        System.out.println("Executing " + classExecutionTemplate.getFullClassName());
        antRunner.runJavaClass(classExecutionTemplate);
    }

    @Override
    public void interrupt()
    {
        antRunner.halt();
        super.interrupt();
    }
}