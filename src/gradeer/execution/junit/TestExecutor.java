package gradeer.execution.junit;

import gradeer.configuration.Configuration;
import gradeer.execution.AntProcessResult;
import gradeer.execution.AntRunner;
import gradeer.results.io.FileWriter;
import gradeer.subject.ClassPath;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestExecutor
{
    private static Logger logger = LogManager.getLogger(TestExecutor.class);

    private TestSuite testSuite;
    private Configuration configuration;

    public TestExecutor(TestSuite testSuite, Configuration configuration)
    {
        this.testSuite = testSuite;
        this.configuration = configuration;
    }

    public TestResult execute(Solution solution)
    {
        ClassPath classPath = new ClassPath();
        classPath.add(testSuite.getJavaFile().getParent());
        classPath.add(solution.getDirectory());
        classPath.add(configuration.getTestDependenciesDir());

        AntRunner antRunner = new AntRunner(configuration, classPath);
        AntProcessResult antProcessResult = antRunner.runTest(testSuite, solution);
        if(configuration.getTestOutputDir() != null) {
            configuration.getTestOutputDir().toFile().mkdirs();
            Path output = Paths.get(configuration.getTestOutputDir() + File.separator + solution.getIdentifier());

            FileWriter f = new FileWriter();
            f.addLine(antProcessResult.getJUnitMessage());
            f.write(output);
        }
        //logger.info(antProcessResult);
        return new TestResult(antProcessResult);
    }
}
