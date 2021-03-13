package tech.clegg.gradeer.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.configuration.Environment;
import tech.clegg.gradeer.solution.Solution;
import tech.clegg.gradeer.subject.ClassPath;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SinglePrintingAntRunner extends AntRunner
{
    private static Logger logger = LogManager.getLogger(SinglePrintingAntRunner.class);
    private final Solution solution;

    private Process process;

    private OutputMonitoringThread outputMonitoringThread;

    public SinglePrintingAntRunner(Configuration configuration, ClassPath classPath, Solution solution)
    {
        super(configuration, classPath);
        this.solution = solution;
    }

    @Override
    public AntProcessResult runAntProcess(List<String> command)
    {
        ProcessBuilder pb = generateProcessBuilder(command);

        System.out.println("Running ant command: " + command + " from " + pb.directory());

        try
        {
            process = pb.start();
            outputMonitoringThread = new OutputMonitoringThread(
                    process.getInputStream(),
                    process.getErrorStream(),
                    Paths.get(getConfiguration().getSolutionCapturedOutputDir() +
                            File.separator + solution.getIdentifier() + "-output.txt")
            );

            outputMonitoringThread.start();
            outputMonitoringThread.join();
        }
        catch (IOException | InterruptedException ex)
        {
            ex.printStackTrace();
        }

        return new AntProcessResult();
    }

    public void halt()
    {
        if(process == null)
        {
            logger.info("No ant process to halt!");
            return;
        }

        try
        {
            process.waitFor(80, TimeUnit.MILLISECONDS);
            process.destroy();
            process.waitFor(80, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e)
        {
            logger.info("Process terminated early.");
        }
    }
}
