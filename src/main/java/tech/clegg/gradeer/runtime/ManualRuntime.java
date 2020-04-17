package tech.clegg.gradeer.runtime;

import tech.clegg.gradeer.Gradeer;
import tech.clegg.gradeer.auxiliaryprocesses.MergedSolutionWriter;
import tech.clegg.gradeer.checks.CheckProcessor;
import tech.clegg.gradeer.checks.generation.ManualCheckGenerator;
import tech.clegg.gradeer.configuration.Configuration;

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
