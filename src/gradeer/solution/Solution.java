package gradeer.solution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;

public class Solution
{
    private static Logger logger = LogManager.getLogger(Solution.class);

    Path directory;

    public Solution(Path locationDir)
    {
        directory = locationDir;

    }
}
