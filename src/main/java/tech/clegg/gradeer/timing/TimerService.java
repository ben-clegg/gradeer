package tech.clegg.gradeer.timing;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class TimerService
{
    private FileWriter writer;
    private StopWatch stopWatch;
    private SimpleDateFormat dateFormat;

    private Logger logger = LogManager.getLogger(TimerService.class);

    public TimerService(Path outputFileLocation)
    {
        // Configure date formatter
        dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("utc"));

        // Start timer
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
            logger.info(eventText);
            writer.write(eventText + "\n");
            writer.flush();
        } catch (IOException ioException)
        {
            ioException.printStackTrace();
        }
    }

    private void writeEvent(String event, long timeMillis)
    {
        writeEvent(event, dateFormat.format(timeMillis));
    }

    public void end()
    {
        stopWatch.stop();

        writeEvent("Ended grading session (total time)", stopWatch.getTime());
    }

    public void split(String splitEvent)
    {
        long previousSplit = stopWatch.getSplitTime();
        stopWatch.split();
        writeEvent(splitEvent, stopWatch.getSplitTime() - previousSplit);
    }

}
