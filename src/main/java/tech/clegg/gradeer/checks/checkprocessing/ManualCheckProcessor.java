package tech.clegg.gradeer.checks.checkprocessing;

import tech.clegg.gradeer.auxiliaryprocesses.InspectionCommandProcess;
import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.ManualCheck;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.java.JavaClassBatchExecutor;
import tech.clegg.gradeer.solution.Solution;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

public class ManualCheckProcessor extends CheckProcessor
{
    public ManualCheckProcessor(Collection<Check> checks, Configuration configuration)
    {
        super(checks, configuration);
    }

    @Override
    public void runChecks(Solution solution)
    {
        if(checks.isEmpty())
        {
            configuration.getLogFile().writeMessage("No checks in ManualCheckProcessor for solution " + solution.getIdentifier());
            return;
        }

        // Run each of the defined ClassExecutionTemplates
        JavaClassBatchExecutor classExec = generateClassExecutor(solution);
        classExec.runClasses();

        // Run inspection command (e.g. vscode)
        runInspectionCommand(solution);

        // Execute checks
        checks.forEach(c -> c.run(solution));
        executedSolutions.add(solution);

        // Stop running classes
        classExec.stopExecutions();
    }



    private JavaClassBatchExecutor generateClassExecutor(Solution solution)
    {
        return new JavaClassBatchExecutor(solution, configuration);
    }

    private void runInspectionCommand(Solution solution)
    {
        if(configuration.getInspectionCommand() == null)
            return;
        if(configuration.getInspectionCommand().isEmpty())
            return;

        if(!presentCheckClasses.contains(ManualCheck.class))
            return;

        Collection<Path> toInspect = new ArrayList<>();

        // TODO find a more elegant solution for this
        // method inside corresponding classes?

        if(Files.exists(configuration.getTestOutputDir()))
            toInspect.add(Paths.get(configuration.getTestOutputDir() + File.separator + solution.getIdentifier()));
        if(Files.exists(configuration.getMergedSolutionsDir()))
            toInspect.add(Paths.get(configuration.getMergedSolutionsDir() + File.separator + solution.getIdentifier() + ".java"));

        new InspectionCommandProcess(configuration, toInspect).run();
    }
}
