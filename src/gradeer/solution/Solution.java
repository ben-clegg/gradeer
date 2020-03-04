package gradeer.solution;

import gradeer.io.JavaSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

public class Solution
{
    private static Logger logger = LogManager.getLogger(Solution.class);

    Path directory;
    Collection<JavaSource> sources;

    public Solution(Path locationDir)
    {
        directory = locationDir;
        try
        {
            sources = Files.walk(directory)
                    .filter(p -> com.google.common.io.Files.getFileExtension(p.toString()).equals("java"))
                    .map(p -> new JavaSource(p, locationDir))
                    .collect(Collectors.toList());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Path getDirectory()
    {
        return directory;
    }

    public Collection<JavaSource> getSources()
    {
        return sources;
    }
}
