package tech.clegg.gradeer.results.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileWriter
{
    private final List<String> lines;
    private boolean appendMode = false;

    public FileWriter(boolean append)
    {
        this.lines = new ArrayList<>();
        this.appendMode = append;
    }

    public FileWriter()
    {
        this(false);
    }

    public FileWriter(List<String> lines)
    {
        this.lines = lines;
    }

    public void write(Path location)
    {
        java.io.FileWriter writer;

        try
        {
            // Make dir if it doesn't exist
            if(!Files.exists(location.getParent()))
            {
                Files.createDirectory(location.getParent());
            }

            writer = new java.io.FileWriter(location.toString(), appendMode);
            for (String l : lines)
                writer.append(l + "\n");
            writer.close();
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
