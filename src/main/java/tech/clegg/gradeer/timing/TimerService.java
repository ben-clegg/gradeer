package tech.clegg.gradeer.timing;


import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class TimerService
{
    FileWriter writer;
    StopWatch stopWatch;

    public TimerService(Path outputFileLocation)
    {
        stopWatch = new StopWatch();
        stopWatch.start();
        stopWatch.split();
        writeEvent("Started grading session", stopWatch.getStartTime());
        try
        {
            Files.createDirectories(outputFileLocation.getParent());
            writer = new FileWriter(outputFileLocation.toFile(), true);
        } catch (IOException ioException)
        {
            ioException.printStackTrace();
        }
    }

    private void writeEvent(String event, String time)
    {
        if (writer == null)
            return;

        try
        {
            String eventText = time + "," + " " + event;
            System.out.println(eventText);
            writer.write(eventText + "\n");
            writer.flush();
        } catch (IOException ioException)
        {
            ioException.printStackTrace();
        }
    }

    private void writeEvent(String event, long timeMillis)
    {
        writeEvent(event, DateFormatUtils.format(timeMillis, "HH:mm:ss"));
    }

    public void end()
    {
        stopWatch.unsplit();
        stopWatch.stop();
        writeEvent("Ended grading session (total time)", stopWatch.getTime() - stopWatch.getStartTime());
    }

    public void split(String splitEvent)
    {
        writeEvent(splitEvent, stopWatch.getTime() - stopWatch.getSplitTime());
        stopWatch.split();
    }

}
