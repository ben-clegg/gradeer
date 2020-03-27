package gradeer.checks;

import gradeer.configuration.Configuration;
import gradeer.execution.staticanalysis.checkstyle.CheckstyleExecutor;
import gradeer.execution.staticanalysis.pmd.PMDExecutor;
import gradeer.solution.Solution;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CheckProcessor
{
    private Collection<Check> checks;
    private Configuration configuration;
    private Set<Solution> executedSolutions;

    public CheckProcessor(Collection<Check> checks, Configuration configuration)
    {

        this.checks = checks;
        this.configuration = configuration;
        executedSolutions = new HashSet<>();
    }

    public void runChecks(Solution solution)
    {
        // Run PMD on student solutions
        if(configuration.isPmdEnabled())
        {
            PMDExecutor pmdExecutor = new PMDExecutor(configuration);
            pmdExecutor.execute(solution);
        }

        // Run Checkstyle on student solutions
        if(configuration.isCheckstyleEnabled())
        {
            CheckstyleExecutor checkstyleExecutor = new CheckstyleExecutor(configuration,
                    getChecks().stream()
                            .filter(c -> c instanceof CheckstyleCheck)
                            .map(c -> (CheckstyleCheck) c)
                            .collect(Collectors.toList()));
            checkstyleExecutor.execute(solution);
        }


        checks.forEach(c -> c.run(solution));
        executedSolutions.add(solution);
    }

    public boolean wasExecuted(Solution solution)
    {
        return executedSolutions.contains(solution);
    }

    public Collection<Check> getChecks()
    {
        return checks;
    }
}
