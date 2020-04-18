package tech.clegg.gradeer.auxiliaryprocesses;

import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.staticanalysis.pmd.PMDProcessResults;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InspectionCommandProcess implements Runnable
{
    private Configuration configuration;
    private Collection<Path> toInspect;

    public InspectionCommandProcess(Configuration configuration, Collection<Path> toInspect)
    {
        this.configuration = configuration;
        this.toInspect = toInspect;
    }

    @Override
    public void run()
    {
        List<String> command = new ArrayList<>();

        // Load PMD
        command.add(configuration.getInspectionCommand());

        for (Path p : toInspect)
        {
            if(Files.exists(p))
                command.add(p.toString());
        }

        // Run process
        System.out.println("Running inspection process with command: " + command);
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        try
        {
            Process process = processBuilder.start();
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
        }
    }
}
