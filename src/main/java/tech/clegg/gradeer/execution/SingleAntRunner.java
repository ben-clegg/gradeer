package tech.clegg.gradeer.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.configuration.Environment;
import tech.clegg.gradeer.subject.ClassPath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SingleAntRunner extends AntRunner
{
    private static Logger logger = LogManager.getLogger(SingleAntRunner.class);

    private Process process;

    public SingleAntRunner(Configuration configuration, ClassPath classPath)
    {
        super(configuration, classPath);
    }

    @Override
    public AntProcessResult runAntProcess(List<String> command)
    {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(command);
        pb.directory(Environment.getGradeerHomeDir().toFile());
        pb.redirectErrorStream(true);

        logger.info("Running ant command: " + command + " from " + pb.directory());

        AntProcessResult res = new AntProcessResult();
        try
        {
            process = pb.start();

            BufferedReader is = new BufferedReader(new InputStreamReader(process.getInputStream()));
            res.setInputStream(is);

            String line;
            BufferedReader es = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder esLog = new StringBuilder();
            while ((line = es.readLine()) != null) {
                esLog.append(line).append(System.lineSeparator());
            }
            res.setErrorStreamText(esLog.toString());
        }
        catch (IOException ioEx)
        {
            res.setExceptionText(String.format("Exception: %s%s", ioEx.toString(), System.lineSeparator()));
        }

        return res;
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
