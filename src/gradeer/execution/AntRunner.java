package gradeer.execution;

import gradeer.configuration.Environment;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes specific targets in the provided ant build.xml.
 * Primarily used for runtime automated test execution, though it can be used to execute custom ant targets.
 */
public class AntRunner
{

    public AntRunner()
    {
    }

    public void runAntTarget(String targetName)
    {
        ProcessBuilder pb = new ProcessBuilder();
        List<String> command = new ArrayList<>();

    }
}
