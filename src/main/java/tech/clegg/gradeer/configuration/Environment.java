package tech.clegg.gradeer.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A class for accessing environment-specific constants
 */
public class Environment
{
    private static Path gradeerHomeDir;

    /**
     * Load values with OS-specific parameters
     */
    public static void init()
    {
        gradeerHomeDir = Paths.get(System.getProperty("user.dir"));
    }

    private static boolean pathExists(Path toCheck)
    {
        if(toCheck == null)
            return false;
        return Files.exists(toCheck);
    }

    public static Path getGradeerHomeDir()
    {
        return gradeerHomeDir;
    }
}
