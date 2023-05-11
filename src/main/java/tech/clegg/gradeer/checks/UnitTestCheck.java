package tech.clegg.gradeer.checks;

import com.google.gson.JsonObject;
import tech.clegg.gradeer.checks.exceptions.InvalidCheckException;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.testing.UnitTest;
import tech.clegg.gradeer.execution.testing.junit.JUnitTestSource;
import tech.clegg.gradeer.preprocessing.PreProcessor;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessor;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessorResults;
import tech.clegg.gradeer.preprocessing.testing.UnitTestResult;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

public class UnitTestCheck extends Check
{
    private UnitTest unitTest;
    private static final Class<? extends PreProcessor> PREPROCESSOR_CLASS = UnitTestPreProcessor.class;

    public UnitTestCheck(JsonObject jsonObject, Configuration configuration) throws InvalidCheckException
    {
        super(jsonObject, configuration);
    }

    public UnitTestCheck(UnitTest unitTest, Configuration configuration)
    {
        super(unitTest.toString(), configuration);
        this.unitTest = unitTest;
    }

    public boolean matchesUnitTest(UnitTest toMatch)
    {
        // Objects equal
        if (this.unitTest != null && this.unitTest.equals(toMatch))
            return true;

        // Otherwise can match by name (e.g. for tests defined in configuration)
        if (this.name.equals(toMatch.toString()))
            return true;

        return false;
    }

    public void setUnitTest(UnitTest unitTest)
    {
        this.unitTest = unitTest;
    }

    @Override
    protected String defaultCheckGroupForType()
    {
        return "Functionality";
    }

    @Override
    public void execute(Solution solution)
    {
        // Fail if no compiled is TestSuite loaded.
        // This will cause the check to be removed when executed on the model solution by default.

        if(unitTest == null)
        {
            System.err.println("UnitTestCheck " + getName() + " has no defined UnitTest.");
            solution.addCheckResult(generateCheckResult(0.0));
            return;
        }

        // Process
        processSolution(solution);
    }

    @Override
    protected double generateUnweightedScore(Solution solution)
    {
        // Retrieve existing result
        try
        {
            return scoreFromPreProcessorResult(solution);
        }
        catch (NoSuchElementException ignore) { } // Will re-run instead

        // Run PreProcessor if no matching results
        UnitTestPreProcessor unitTestPreProcessor = new UnitTestPreProcessor(solution, getConfiguration());
        unitTestPreProcessor.start();

        // Should now have results - if not, assume failure or misconfiguration
        // (score of 0; the check would be excluded when run on the model solution)
        try
        {
            return scoreFromPreProcessorResult(solution);
        }
        catch (NoSuchElementException e)
        {
            e.printStackTrace();
            return 0.0;
        }
    }

    private double scoreFromPreProcessorResult(Solution solution) throws NoSuchElementException
    {
        // TODO alternate error handling? (e.g. exception)
        if (this.getUnitTest() == null)
            return 0.0;

        if (!solution.hasPreProcessorResultsOfType(PREPROCESSOR_CLASS))
            throw new NoSuchElementException("No UnitTestPreProcessorResults for " + solution);

        UnitTestPreProcessorResults results = (UnitTestPreProcessorResults) solution.getPreProcessorResultsOfType(PREPROCESSOR_CLASS);

        UnitTestResult testResult = results.getResultForUnitTest(this.getUnitTest());

        if (testResult.getResultFlag().equals(UnitTestResult.UnitTestResultFlag.PASS))
            return 1.0;
        return 0.0;
    }

    @Override
    public Collection<Class<? extends PreProcessor>> getPreProcessorTypes()
    {
        return Collections.singleton(PREPROCESSOR_CLASS);
    }

    public UnitTest getUnitTest()
    {
        return unitTest;
    }

    @Override
    public String toString()
    {
        return "UnitTestCheck{" +
                "testSuite=" + unitTest +
                " (" + super.toString() + ")" +
                '}';
    }
}
