package tech.clegg.gradeer.configuration;

import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A class for accessing environment-specific constants
 */
public class Environment
{
    private static Path antExecutable;
    private static Path gradeerHomeDir;

    /**
     * Load values with OS-specific parameters
     */
    public static void init()
    {
        gradeerHomeDir = Paths.get(System.getProperty("user.dir"));

        if(SystemUtils.IS_OS_LINUX)
        {
            antExecutable = Paths.get("/usr/share/ant/bin/ant");
        }
        if(SystemUtils.IS_OS_MAC_OSX)
        {
            antExecutable = Paths.get("/usr/local/bin/ant");
        }

        // Override with env vars
        loadEnvVars();
    }

    private static void loadEnvVars()
    {
        if(System.getenv("ANT_EXECUTABLE") != null && !System.getenv("ANT_EXECUTABLE").isEmpty())
            antExecutable = Paths.get(System.getenv("ANT_EXECUTABLE"));

    }


    public static Path getAntExecutable()
    {
        return antExecutable;
    }

    public static Path getGradeerHomeDir()
    {
        return gradeerHomeDir;
    }
}
