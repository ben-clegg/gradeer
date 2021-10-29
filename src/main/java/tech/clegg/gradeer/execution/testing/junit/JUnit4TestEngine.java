package tech.clegg.gradeer.execution.testing.junit;

import org.junit.runner.JUnitCore;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.testing.TestEngine;
import tech.clegg.gradeer.execution.testing.junit.resultstorage.JUnit4ResultStorageListener;
import tech.clegg.gradeer.input.TestSourceFile;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessor;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessorResults;
import tech.clegg.gradeer.preprocessing.testing.UnitTestResult;
import tech.clegg.gradeer.solution.Solution;
import tech.clegg.gradeer.subject.JavaSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class JUnit4TestEngine extends TestEngine
{
    public JUnit4TestEngine(Configuration configuration)
    {
        super(configuration);
    }

    private static boolean dirExists(Path dir)
    {
        return dir != null && Files.exists(dir);
    }

    @Override
    protected void execute(Solution solutionUnderTest, TestSourceFile testSource)
    {
        if (!(testSource instanceof JUnitTestSource))
            throw new IllegalArgumentException(testSource + "is not a valid JUnit test");

        JUnitTestSource jUnitTestSource = (JUnitTestSource) testSource;

        System.out.println("Running test suite " + jUnitTestSource.getBaseName() +
                " on solution " + solutionUnderTest.getIdentifier());

        try
        {
            // Setup root directories to find classes in
            Collection<URL> classRoots = new ArrayList<>();
            classRoots.add(jUnitTestSource.getRootDir().toUri().toURL());
            classRoots.add(solutionUnderTest.getDirectory().toUri().toURL());
            // Include test dependencies
            Path testDependenciesDir = getConfiguration().getTestDependenciesDir();
            if (dirExists(testDependenciesDir))
                classRoots.add(testDependenciesDir.toUri().toURL());
            // Include source dependencies
            Path sourceDependenciesDir = getConfiguration().getSourceDependenciesDir();
            if (dirExists(sourceDependenciesDir))
                classRoots.add(sourceDependenciesDir.toUri().toURL());

            URL[] classRootsArray = new URL[classRoots.size()];
            classRootsArray = classRoots.toArray(classRootsArray);

            try (URLClassLoader classLoader = new URLClassLoader(classRootsArray))
            {
                // Load test class
                Class<?> testClass = classLoader.loadClass(jUnitTestSource.getComplexClassName());
                // Load solution classes
                for (JavaSource source : solutionUnderTest.getSources())
                {
                    classLoader.loadClass(source.getComplexClassName());
                }

                // Load test dependencies listed in configuration
                if (dirExists(testDependenciesDir))
                {
                    Collection<JavaSource> testDependencies = JavaSource.loadAllSourcesInRootDir(testDependenciesDir);
                    for (JavaSource td : testDependencies)
                    {
                        classLoader.loadClass(td.getComplexClassName());
                    }
                }

                // Load source dependencies
                if (dirExists(sourceDependenciesDir))
                {
                    Collection<JavaSource> sourceDependencies = JavaSource.loadAllSourcesInRootDir(sourceDependenciesDir);
                    for (JavaSource sd : sourceDependencies)
                    {
                        classLoader.loadClass(sd.getComplexClassName());
                    }
                }

                JUnitCore jUnitCore = new JUnitCore();
                // Listener will populate the solution with results for each test
                jUnitCore.addListener(new JUnit4ResultStorageListener(solutionUnderTest));
                // Run the test class
                jUnitCore.run(testClass);
                // Ensure class is unloaded - likely not necessary
                testClass = null;
            }

        } catch (ClassNotFoundException | IOException e)
        {
            e.printStackTrace();
        }
    }
}
