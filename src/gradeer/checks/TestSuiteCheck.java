package gradeer.checks;

import gradeer.configuration.Configuration;
import gradeer.execution.junit.TestExecutor;
import gradeer.execution.junit.TestResult;
import gradeer.execution.junit.TestSuite;
import gradeer.solution.Solution;

public class TestSuiteCheck extends Check
{
    TestSuite testSuite;
    TestExecutor testExecutor;

    public TestSuiteCheck(TestSuite testSuite, Configuration configuration)
    {
        this.testSuite = testSuite;
        this.testExecutor = new TestExecutor(testSuite, configuration);
        this.name = testSuite.getBaseName();
    }

    @Override
    public void run(Solution solution)
    {
        TestResult testResult = testExecutor.execute(solution);
        unweightedScores.put(solution, testResult.proportionPassing());
    }

    public TestSuite getTestSuite()
    {
        return testSuite;
    }
}
