package gradeer.execution.pmd;

import gradeer.configuration.Configuration;
import gradeer.solution.Solution;
import net.sourceforge.pmd.PMD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PMDExecutor
{
    private Configuration configuration;
    private List<String> pmdRuleSetNames;

    public PMDExecutor(Configuration configuration)
    {
        this.configuration = configuration;
        this.pmdRuleSetNames = Arrays.asList(configuration.getPmdRulesets().split(","));
    }

    public void execute(Solution solution)
    {
        PMDProcess pmdProcess = new PMDProcess(solution, pmdRuleSetNames, configuration);
        pmdProcess.run();
    }
}
