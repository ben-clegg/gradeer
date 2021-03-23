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
    private final InspectorThread inspectorThread;

    public SourceInspectorPreProcessor(Solution solution, Configuration configuration)
    {
        super(solution, configuration);
        inspectorThread = new InspectorThread(solution, configuration);
    }

    @Override
    public void start()
    {
        inspectorThread.start();
    }

    @Override
    public void stop() { inspectorThread.interrupt(); }

    class InspectorThread extends Thread
    {
        private InspectionCommandProcess inspectionCommandProcess;
        private final Solution solution;
        private final Configuration configuration;

        public InspectorThread(Solution solution, Configuration configuration)
        {
            this.solution = solution;
            this.configuration = configuration;
        }

        @Override
        public synchronized void start()
        {
            super.start();
            // Wait before starting, need to let results get populated
            // TODO replace with a wait/notify system
            if(!configuration.getPreManualJavaClassesToExecute().isEmpty()) {
                try {
                    Thread.sleep(1000 * configuration.getPreManualJavaClassesToExecute().get(0).getWaitAfterExecutionTime());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Load InspectionCommandProcess
            Collection<Path> toInspect = new ArrayList<>();
            // TODO find a more elegant solution for this
            // method inside corresponding classes?
            if(Files.exists(configuration.getTestOutputDir()))
                toInspect.add(Paths.get(configuration.getTestOutputDir() + File.separator + solution.getIdentifier()).toAbsolutePath());
            if(Files.exists(configuration.getMergedSolutionsDir()))
                toInspect.add(Paths.get(configuration.getMergedSolutionsDir() + File.separator + solution.getIdentifier() + ".java").toAbsolutePath());
            if(Files.exists(configuration.getSolutionCapturedOutputDir()))
                toInspect.add(Paths.get(configuration.getSolutionCapturedOutputDir() + File.separator + solution.getIdentifier() + "-output.txt").toAbsolutePath());

            inspectionCommandProcess = new InspectionCommandProcess(configuration, toInspect);
            inspectionCommandProcess.run();
        }

        @Override
        public void interrupt()
        {
            super.interrupt();
            if(inspectionCommandProcess != null)
                inspectionCommandProcess.stop();
        }
    }
}
