package tech.clegg.gradeer.checks;

import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.junit.TestExecutor;
import tech.clegg.gradeer.execution.junit.TestResult;
import tech.clegg.gradeer.execution.junit.TestSuite;
import tech.clegg.gradeer.solution.Solution;

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

        double unweightedScore = testResult.proportionPassing();
        solution.addCheckResult(this, new CheckResult(
                this,
                unweightedScore,
                generateFeedback(unweightedScore)
        ));
    }

    public TestSuite getTestSuite()
    {
        return testSuite;
    }
}
