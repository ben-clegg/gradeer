package tech.clegg.gradeer.execution.junit;

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
                    .map(p -> new TestSuite(p, testsDir)).collect(Collectors.toList());
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            System.err.println("Could not load unit tests in " + testsDir);
        }

    }

    public TestSuiteLoader(Collection<Path> testSrcPaths, Path testsRootDir)
    {
        testSuites = testSrcPaths.stream()
                .filter(Files::exists)
                .map(p -> new TestSuite(p, testsRootDir)).collect(Collectors.toList());
    }

    public Collection<TestSuite> getTestSuites()
    {
        return testSuites;
    }
}
