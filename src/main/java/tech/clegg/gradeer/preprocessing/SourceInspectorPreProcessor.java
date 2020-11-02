package tech.clegg.gradeer.preprocessing;

import tech.clegg.gradeer.auxiliaryprocesses.InspectionCommandProcess;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

public class SourceInspectorPreProcessor extends PreProcessor
{
    private final InspectionCommandProcess inspectionCommandProcess;

    public SourceInspectorPreProcessor(Solution solution, Configuration configuration)
    {
        super(solution, configuration);

        // Load InspectionCommandProcess
        Collection<Path> toInspect = new ArrayList<>();
        // TODO find a more elegant solution for this
        // method inside corresponding classes?
        if(Files.exists(configuration.getTestOutputDir()))
            toInspect.add(Paths.get(configuration.getTestOutputDir() + File.separator + solution.getIdentifier()));
        if(Files.exists(configuration.getMergedSolutionsDir()))
            toInspect.add(Paths.get(configuration.getMergedSolutionsDir() + File.separator + solution.getIdentifier() + ".java"));

        this.inspectionCommandProcess = new InspectionCommandProcess(configuration, toInspect);
    }

    @Override
    public void start()
    {
        inspectionCommandProcess.run();
    }

    @Override
    public void stop()
    {
        inspectionCommandProcess.stop();
    }
}
