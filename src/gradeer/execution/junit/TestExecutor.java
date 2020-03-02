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

    private TestSuite test;
    private Configuration config;

    public TestExecutor(TestSuite testSuite, Configuration configuration)
    {
        test = testSuite;
        config = configuration;
    }

    public TestResult execute(Solution solution)
    {
        ClassPath classPath = new ClassPath();
        classPath.add(test.getJavaFile().getParent());
        classPath.add(solution.getDirectory());

        AntRunner antRunner = new AntRunner(config, classPath);
        AntProcessResult antProcessResult = antRunner.runTest(test, solution);
        return new TestResult(antProcessResult);
    }
}
