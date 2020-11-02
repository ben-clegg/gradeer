package tech.clegg.gradeer.preprocessing;

import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.java.JavaClassBatchExecutor;
import tech.clegg.gradeer.solution.Solution;

public class JavaBatchExecutorPreProcessor extends PreProcessor
{
    private final JavaClassBatchExecutor batchExecutor;

    public JavaBatchExecutorPreProcessor(Solution solution, Configuration configuration)
    {
        super(solution, configuration);
        this.batchExecutor = new JavaClassBatchExecutor(solution, configuration);
    }

    @Override
    public void start()
    {
        batchExecutor.runClasses();
    }

    @Override
    public void stop()
    {
        batchExecutor.stopExecutions();
    }
}
