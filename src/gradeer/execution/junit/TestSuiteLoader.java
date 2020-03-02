package gradeer.execution.junit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

public class TestSuiteLoader
{
    private static Logger logger = LogManager.getLogger(TestSuiteLoader.class);

    Collection<TestSuite> testSuites;

    public TestSuiteLoader(Path testsDir)
    {
        try
        {
            testSuites = Files.walk(testsDir)
                    .filter(p -> com.google.common.io.Files.getFileExtension(p.toString()).equals("java"))
                    .map(TestSuite::new).collect(Collectors.toList());
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            logger.error("Could not load unit tests in " + testsDir);
        }

    }

    public TestSuiteLoader(Collection<Path> testSrcPaths)
    {
        testSuites = testSrcPaths.stream()
                .filter(Files::exists)
                .map(TestSuite::new).collect(Collectors.toList());
    }

    public Collection<TestSuite> getTestSuites()
    {
        return testSuites;
    }
}
