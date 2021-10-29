package tech.clegg.gradeer.execution.testing.junit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

public class TestSuiteLoader
{
    Collection<JUnitTestSource> testSuites;

    public TestSuiteLoader(Path testsDir)
    {
        try
        {
            testSuites = Files.walk(testsDir)
                    .filter(p -> com.google.common.io.Files.getFileExtension(p.toString()).equals("java"))
                    .map(p -> new JUnitTestSource(p, testsDir)).collect(Collectors.toList());
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
                .map(p -> new JUnitTestSource(p, testsRootDir)).collect(Collectors.toList());
    }

    public Collection<JUnitTestSource> getTestSuites()
    {
        return testSuites;
    }
}
