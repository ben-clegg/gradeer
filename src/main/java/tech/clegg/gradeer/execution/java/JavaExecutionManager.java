package tech.clegg.gradeer.execution.java;

import tech.clegg.gradeer.execution.SinglePrintingAntRunner;

import java.util.concurrent.TimeUnit;

public class JavaExecutionManager
{
    private final JavaExecution javaExecution;
    private int waitAfterExecutionTime;

    public JavaExecutionManager(SinglePrintingAntRunner antRunner, ClassExecutionTemplate classExecutionTemplate)
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

    public JavaExecution getJavaExecution()
    {
        return javaExecution;
    }

    public void stop()
    {
        javaExecution.interrupt();
    }
}