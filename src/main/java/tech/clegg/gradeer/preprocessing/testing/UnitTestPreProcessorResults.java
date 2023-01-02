package tech.clegg.gradeer.preprocessing.testing;

import tech.clegg.gradeer.execution.testing.UnitTest;
import tech.clegg.gradeer.preprocessing.PreProcessor;
import tech.clegg.gradeer.preprocessing.PreProcessorResults;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class UnitTestPreProcessorResults extends PreProcessorResults
{
    private Map<UnitTest, UnitTestResult> resultMap = new ConcurrentHashMap<>();
    private static final Class<? extends PreProcessor> PRE_PROCESSOR_CLASS = UnitTestPreProcessor.class;

    public UnitTestPreProcessorResults(UnitTestResult testResult)
    {
        resultMap.put(testResult.getUnitTest(), testResult);
    }

    public void bindToSolution(Solution solution)
    {
        // Determine if solution already has a result
        if (solution.hasPreProcessorResultsOfType(PRE_PROCESSOR_CLASS))
        {
            // If so, merge, but skip if a UnitTest already has results
            UnitTestPreProcessorResults existing =
                    (UnitTestPreProcessorResults) solution.getPreProcessorResultsOfType(PRE_PROCESSOR_CLASS);

            for (UnitTest t : resultMap.keySet())
            {
                if (!existing.resultMap.containsKey(t))
                    existing.resultMap.put(t, this.resultMap.get(t));
            }

        }
        else
        {
            // Otherwise add to Solution directly
            solution.addPreProcessorResults(PRE_PROCESSOR_CLASS, this);
        }
    }

    public Collection<UnitTestResult> getTestResults()
    {
        return resultMap.values();
    }

    public Collection<UnitTest> getExecutedUnitTests()
    {
        return resultMap.keySet();
    }

    public UnitTestResult getResultForUnitTest(UnitTest targetTest) throws NoSuchElementException
    {
        UnitTestResult result = resultMap.get(targetTest);
        if (result == null)
            throw new NoSuchElementException("No result for UnitTest " + targetTest);
        return result;
    }
}
