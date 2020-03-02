package gradeer.execution.junit;

import gradeer.execution.AntProcessResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestResult
{
    private static Logger logger = LogManager.getLogger(TestResult.class);

    private int passingTests;
    private int totalTests;

    public TestResult(AntProcessResult antResult)
    {
        //logger.info(antResult);
        totalTests = antResult.getTestsRun();
        passingTests = totalTests - (antResult.getTestsFailures() + antResult.getTestsErrors());
    }

    public double proportionPassing()
    {
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
