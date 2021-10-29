package tech.clegg.gradeer.execution;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutputMonitoringThread extends Thread
{
    private final InputStream stdOut;
    private final InputStream stdErr;
    private BufferedWriter fileWriter = null;

    private final boolean PRINT_OUTPUT_TO_CONSOLE = true; // TODO set from configuration

    public OutputMonitoringThread(InputStream stdOut, InputStream stdErr, Path outputFile)
    {
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        initFileOutput(outputFile);
    }

    @Override
    public void run()
    {
        BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(stdOut));
        BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(stdErr));

        try
        {
            String outLine = null;
            String errLine = null;

            while ((outLine = stdOutReader.readLine()) != null ||
                    (errLine = stdErrReader.readLine()) != null)
            {
                if (outLine != null)
                {
                    appendLineToFile(outLine);
                    printLine(outLine);
                }
                if (errLine != null)
                {
                    appendLineToFile(errLine);
                    printLine(errLine);
                }
            }
        } catch (IOException ignored) {
            // Don't show stack trace; usually the program has simply been closed
        } finally
        {
            try
            {
                fileWriter.close();
            } catch (IOException ioEx)
            {
                ioEx.printStackTrace();
            }
        }
    }

    private void printLine(String line)
    {
        if(!PRINT_OUTPUT_TO_CONSOLE)
            return;

        System.out.println("[Solution Execution] " + line);
    }


    private void initFileOutput(Path outputFile)
    {
        // Skip if no output file
        if(outputFile == null)
            return;

        // Make parent directory
        try
        {
            if(Files.notExists(outputFile.getParent()))
                Files.createDirectories(outputFile.getParent());
        } catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            return;
        }

        // Create file writer
        try
        {
            fileWriter = new BufferedWriter(new FileWriter(outputFile.toFile(), true));
        } catch (IOException ioEx)
        {
            ioEx.printStackTrace();
        }
    }

    private void appendLineToFile(String line)
    {
        // Skip if no file writer
        if (fileWriter == null)
            return;

        // Write
        try
        {
            fileWriter.write(line);
            fileWriter.newLine();
        } catch (IOException ioEx)
        {
            ioEx.printStackTrace();
        }
    }
}
