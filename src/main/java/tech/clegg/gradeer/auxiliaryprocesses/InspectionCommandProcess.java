package tech.clegg.gradeer.auxiliaryprocesses;

import tech.clegg.gradeer.configuration.Configuration;

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
    private Process process;

    public InspectionCommandProcess(Configuration configuration, Collection<Path> toInspect)
    {
        this.configuration = configuration;
        this.toInspect = toInspect;
    }

    @Override
    public void run()
    {
        List<String> command = new ArrayList<>();

        // Do nothing if no inspection command
        if(configuration.getInspectionCommand() == null)
            return;
        if(configuration.getInspectionCommand().isEmpty())
            return;

        // Add main inspection command
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
            process = processBuilder.start();
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
        }
    }

    public void stop()
    {
        if(process == null)
            return;
        if(!process.isAlive())
            return;
        process.destroy();
    }
}
