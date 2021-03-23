package tech.clegg.gradeer.results.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class DelayedFileWriter
{
    private final Queue<String> lines;
    private boolean appendMode = false;

    public DelayedFileWriter(boolean append)
    {
        this.lines = new ConcurrentLinkedQueue<>();
        this.appendMode = append;
    }

    public DelayedFileWriter()
    {
        this(false);
    }

    public DelayedFileWriter(List<String> existingLines)
    {
        this.lines = new ConcurrentLinkedQueue<>();
        this.lines.addAll(existingLines);
        this.appendMode = true;
    }

    public void write(Path location)
    {
        java.io.FileWriter writer;

        try
        {
            // Make dir if it doesn't exist
            if(!Files.exists(location.getParent()))
            {
                Files.createDirectories(location.getParent());
            }

            writer = new java.io.FileWriter(location.toString(), appendMode);
            for (String l : lines)
                writer.append(l).append("\n");
            writer.close();
            // Clear lines
            lines.clear();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void addLine(String line)
    {
        lines.add(line);
    }

    public void addLines(Collection<String> multipleLines) { lines.addAll(multipleLines); }

}
