package tech.clegg.gradeer.results.io;

import java.nio.file.Path;

public class LogFile extends DelayedFileWriter
{
    private Path location;

    /**
     * Make the log file, with a specified location, from the configuration that creates it.
     * The log will always be appended.
     * @param location Location to write log to.
     */
    public LogFile(Path location)
    {
        super(true);
        this.location = location;
    }

    public void writeMessage(String message)
    {
        //System.out.println("[Log] " + message);
        addLine(message);
        write(location);
    }

    public void writeException(Exception exception)
    {
        writeMessage(exception.getMessage());
    }

    public void setLocation(Path location)
    {
        this.location = location;
    }
}
