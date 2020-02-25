package gradeer.execution.junit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class UnitTestLoader
{
    private static Logger logger = LogManager.getLogger(UnitTestLoader.class);

    Collection<UnitTest> unitTests;

    public UnitTestLoader(Path testsDir)
    {
        try
        {
            unitTests = Files.walk(testsDir)
                    .filter(p -> com.google.common.io.Files.getFileExtension(p.toString()).equals("java"))
                    .map(UnitTest::new).collect(Collectors.toList());
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            logger.error("Could not load unit tests in " + testsDir);
        }

    }

    public UnitTestLoader(Collection<Path> testSrcPaths)
    {
        unitTests = testSrcPaths.stream().filter(Files::exists).map(UnitTest::new).collect(Collectors.toList());
    }

    public Collection<UnitTest> getUnitTests()
    {
        return unitTests;
    }
}
