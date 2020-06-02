package tech.clegg.gradeer.execution.junit;

import tech.clegg.gradeer.execution.AntProcessResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestResult
{
    private static Logger logger = LogManager.getLogger(TestResult.class);

    private final int passingTests;
    private final int totalTests;
    private final int failuresAndErrors;

    public TestResult(AntProcessResult antResult)
    {
        //logger.info(antResult);
        totalTests = antResult.getTestsRun();
        failuresAndErrors = antResult.getTestsFailures() + antResult.getTestsErrors();
        passingTests = totalTests - failuresAndErrors;
    }

    public double proportionPassing()
    {
        // Prevent divide by 0
        if(failuresAndErrors >= totalTests)
            return 0;

        return (double) passingTests / (double) totalTests;
    }

    public boolean allTestsPass()
    {
        return (passingTests == totalTests);
    }

    public int getTotalTests()
    {
        return totalTests;
    }

    @Override
    public String toString()
    {
        return "TestResult{" +
                "passingTests=" + passingTests +
                ", totalTests=" + totalTests +
                '}';
    }
}
