package tech.clegg.gradeer.preprocessing;

import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.preprocessing.staticanalysis.pmd.PMDProcess;
import tech.clegg.gradeer.solution.Solution;

import java.util.Arrays;
import java.util.List;

public class PMDPreProcessor extends PreProcessor
{
    private List<String> pmdRuleSetNames;

    public PMDPreProcessor(Solution solution, Configuration configuration)
    {
        super(solution, configuration);
        this.pmdRuleSetNames = Arrays.asList(configuration.getPmdRulesets());
    }

    @Override
    public void start()
    {
        System.out.println("Running PMD on solution " + getSolution());

        PMDProcess pmdProcess = new PMDProcess(getSolution(), pmdRuleSetNames, getConfiguration());
        pmdProcess.run();
        getSolution().addPreProcessorResults(this.getClass(), pmdProcess.getResults());
    }

    @Override
    public void stop()
    {
        // Do nothing
    }
}
