package tech.clegg.gradeer.checks;

import com.google.gson.JsonObject;
import tech.clegg.gradeer.checks.exceptions.InvalidCheckException;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.junit.TestExecutor;
import tech.clegg.gradeer.execution.junit.TestResult;
import tech.clegg.gradeer.execution.junit.TestSuite;
import tech.clegg.gradeer.preprocessing.PreProcessor;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class TestSuiteCheck extends Check
{
    private TestSuite testSuite;
    private TestExecutor testExecutor;

    public TestSuiteCheck(JsonObject jsonObject, Configuration configuration) throws InvalidCheckException
    {
        super(jsonObject, configuration);
    }

    public TestSuiteCheck(TestSuite testSuite, Configuration configuration)
    {
        super(testSuite.getBaseName(), configuration);
        setTestSuite(testSuite);
    }

    public void loadTestSuite(Collection<TestSuite> availableTestSuites)
    {
        // Find a TestSuite with a matching name
        Optional<TestSuite> test = availableTestSuites.stream().filter(t -> t.getBaseName().equals(this.getName())).findFirst();
        test.ifPresent(this::setTestSuite);
    }

    private void setTestSuite(TestSuite testSuite)
    {
        this.testSuite = testSuite;
        this.testExecutor = new TestExecutor(testSuite, getConfiguration());
    }

    @Override
    public void execute(Solution solution)
    {
        // Fail if no compiled is TestSuite loaded.
        // This will cause the check to be removed when executed on the model solution by default.
        if(testSuite == null || testExecutor == null)
        {
            System.err.println("TestSuiteCheck " + getName() + " has no defined TestSuite.");
            solution.addCheckResult(generateCheckResult(0.0));
            return;
        }
        if(!testSuite.isCompiled())
        {
            System.err.println("TestSuiteCheck " + getName() + "'s TestSuite is not compiled.");
            solution.addCheckResult(generateCheckResult(0.0));
            return;
        }

        // Process
        processSolution(solution);
    }

    @Override
    protected double generateUnweightedScore(Solution solution)
    {
        TestResult testResult = testExecutor.execute(solution);
        return testResult.proportionPassing();
    }

    @Override
    public Collection<Class<? extends PreProcessor>> getPreProcessorTypes()
    {
        return Collections.emptySet();
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
