package tech.clegg.gradeer.execution.java;

import tech.clegg.gradeer.execution.SinglePrintingAntRunner;

public class JavaExecution extends Thread
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
        try
        {
            this.join();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public SinglePrintingAntRunner getAntRunner()
    {
        return antRunner;
    }
}
