package gradeer.execution.junit;

import gradeer.configuration.Configuration;
import gradeer.execution.AntProcessResult;
import gradeer.execution.AntRunner;
import gradeer.io.ClassPath;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        //logger.info(antProcessResult);
        return new TestResult(antProcessResult);
    }
}
