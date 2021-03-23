package tech.clegg.gradeer.configuration;

import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Files;
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

        // Check if any important paths are missing
        exitOnMissingEnvironment();
    }

    private static void loadEnvVars()
    {
        if(System.getenv("ANT_EXECUTABLE") != null && !System.getenv("ANT_EXECUTABLE").isEmpty())
            antExecutable = Paths.get(System.getenv("ANT_EXECUTABLE"));
    }

    private static void exitOnMissingEnvironment()
    {
        if(!pathExists(antExecutable))
        {
            System.err.println("Ant executable location is not defined. ");
            System.err.println("This can be defined with the environment variable ANT_EXECUTABLE");
            System.exit(2);
        }
    }

    private static boolean pathExists(Path toCheck)
    {
        if(toCheck == null)
            return false;
        return Files.exists(toCheck);
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
