package gradeer.runtime;

import gradeer.Gradeer;
import gradeer.auxiliaryprocesses.MergedSolutionWriter;
import gradeer.checks.CheckProcessor;
import gradeer.checks.generation.ManualCheckGenerator;
import gradeer.configuration.Configuration;

import java.nio.file.Files;

public class ManualRuntime extends Runtime
{
    public ManualRuntime(Gradeer gradeer, Configuration configuration)
    {
        super(gradeer, configuration);
    }

    @Override
    protected void loadChecks()
    {
        if(configuration.getManualChecksJSON() != null && Files.exists(configuration.getManualChecksJSON()))
        {
            ManualCheckGenerator manualCheckGenerator = new ManualCheckGenerator(configuration, gradeer.getModelSolutions());
            checks.addAll(manualCheckGenerator.getChecks());
        }
    }

    @Override
    public CheckProcessor run()
    {
        if(configuration.getMergedSolutionsDir() != null)
            new MergedSolutionWriter(configuration, gradeer.getStudentSolutions()).run();

        return new CheckProcessor(checks, configuration);
    }
}
