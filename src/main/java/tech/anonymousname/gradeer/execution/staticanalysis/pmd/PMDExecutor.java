package tech.anonymousname.gradeer.execution.staticanalysis.pmd;

import tech.anonymousname.gradeer.configuration.Configuration;
import tech.anonymousname.gradeer.solution.Solution;

import java.util.Arrays;
import java.util.List;

public class PMDExecutor
{
    private Configuration configuration;
    private List<String> pmdRuleSetNames;

    public PMDExecutor(Configuration configuration)
    {
        this.configuration = configuration;
        this.pmdRuleSetNames = Arrays.asList(configuration.getPmdRulesets());
    }

    public void execute(Solution solution)
    {
        System.out.println("Running PMD on solution " + solution);
        PMDProcess pmdProcess = new PMDProcess(solution, pmdRuleSetNames, configuration);
        pmdProcess.run();
        solution.setPmdProcessResults(pmdProcess.getResults());
    }
}
