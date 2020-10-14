package tech.anonymousname.gradeer.checks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.anonymousname.gradeer.checks.checkresults.CheckResult;
import tech.anonymousname.gradeer.checks.exceptions.InvalidCheckException;
import tech.anonymousname.gradeer.configuration.Configuration;
import tech.anonymousname.gradeer.execution.junit.TestExecutor;
import tech.anonymousname.gradeer.execution.junit.TestResult;
import tech.anonymousname.gradeer.execution.junit.TestSuite;
import tech.anonymousname.gradeer.solution.Solution;

public class TestSuiteCheck extends Check
{
    private TestSuite testSuite;
    private TestExecutor testExecutor;

    public TestSuiteCheck(JsonObject jsonObject) throws InvalidCheckException
    {
        super(jsonObject);
    }


    /*
    public TestSuiteCheck(TestSuite testSuite, Configuration configuration)
    {
        this.testSuite = testSuite;
        this.testExecutor = new TestExecutor(testSuite, configuration);
        this.name = testSuite.getBaseName();
    }
     */

    @Override
    public void execute(Solution solution)
    {
        TestResult testResult = testExecutor.execute(solution);

        double unweightedScore = testResult.proportionPassing();
        solution.addCheckResult(new CheckResult(
                this,
                unweightedScore,
                generateFeedback(unweightedScore)
        ));
    }

    public TestSuite getTestSuite()
    {
        return testSuite;
    }

    @Override
    public String toString()
    {
        return "TestSuiteCheck{" +
                "testSuite=" + testSuite +
                ", testExecutor=" + testExecutor +
                "(" + super.toString() + ")" +
                '}';
    }
}
