package gradeer.results;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CSVWriter
{
    private final List<String> lines;

    public CSVWriter(List<String> headerColumns)
    {
        lines = new ArrayList<>();
        addEntry(headerColumns);
    }

    public void addEntry(List<String> values)
    {
        if(values.isEmpty())
            return;

        Iterator<String> valuesIter = values.iterator();
        StringBuilder sb = new StringBuilder();

        while (valuesIter.hasNext())
        {
            sb.append(valuesIter.next());
            if(valuesIter.hasNext())
                sb.append(",");
        }
        lines.add(sb.toString());
    }

    public void write(Path location)
    {
        FileWriter writer;
        try
        {
            Files.createDirectory(location.getParent());
            writer = new FileWriter(location.toString());
            for (String l : lines)
                writer.append(l + "\n");
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
