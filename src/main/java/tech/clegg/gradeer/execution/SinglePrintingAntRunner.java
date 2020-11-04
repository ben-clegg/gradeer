package tech.clegg.gradeer.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.configuration.Environment;
import tech.clegg.gradeer.subject.ClassPath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SinglePrintingAntRunner extends AntRunner
{
    private static Logger logger = LogManager.getLogger(SinglePrintingAntRunner.class);

    private Process process;

    private BufferedReader stdOut;
    private BufferedReader stdErr;

    private List<String> capturedOutput;

    public SinglePrintingAntRunner(Configuration configuration, ClassPath classPath)
    {
        super(configuration, classPath);
        capturedOutput = new ArrayList<>();
    }

    @Override
    public AntProcessResult runAntProcess(List<String> command)
    {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(command);
        pb.directory(Environment.getGradeerHomeDir().toFile());
        pb.redirectErrorStream(true);

        logger.info("Running ant command: " + command + " from " + pb.directory());

        try
        {
            process = pb.start();

            stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String stdOutLine;
            while (stdOut.ready() && (stdOutLine = stdOut.readLine()) != null)
            {
                System.out.println("[StdOut] " + stdOutLine);
                capturedOutput.add("[StdOut] " + stdOutLine);
            }

            String stdErrLine;
            while (stdErr.ready() && (stdErrLine = stdErr.readLine()) != null)
            {
                System.err.println("[StdErr] " + stdErrLine);
                capturedOutput.add("[StdErr] " + stdErrLine);
            }

        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
        }

        return new AntProcessResult();
    }

    public List<String> getCapturedOutput()
    {
        return capturedOutput;
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

            stdOut.close();
            stdErr.close();
        } catch (InterruptedException e)
        {
            logger.info("Process terminated early.");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
