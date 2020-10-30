package tech.clegg.gradeer.preprocessing;

import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.preprocessing.staticanalysis.pmd.PMDExecutor;
import tech.clegg.gradeer.solution.Solution;

public class PMDPreProcessor extends PreProcessor
{
    public PMDPreProcessor(Solution solution, Configuration configuration)
    {
        super(solution, configuration);
    }

    @Override
    public void start()
    {
        PMDExecutor pmdExecutor = new PMDExecutor(getConfiguration());
        pmdExecutor.execute(getSolution());
    }

    @Override
    public void stop()
    {
        // Do nothing
    }
}
