package tech.clegg.gradeer.execution.testing.junit.resultstorage;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import tech.clegg.gradeer.execution.testing.junit.JUnitTest;
import tech.clegg.gradeer.preprocessing.PreProcessorResults;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessor;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessorResults;
import tech.clegg.gradeer.preprocessing.testing.UnitTestResult;
import tech.clegg.gradeer.solution.Solution;

public class JUnit4ResultStorageListener extends RunListener
{
    Solution solution;

    public JUnit4ResultStorageListener(Solution solution)
    {
        super();
        this.solution = solution;
    }

    @Override
    public void testFinished(Description description) throws Exception
    {
        super.testFinished(description);

        JUnitTest jUnitTest = new JUnitTest(description);
        UnitTestResult unitTestResult =
                new UnitTestResult(jUnitTest, UnitTestResult.UnitTestResultFlag.PASS, "");
        // TODO Include test explanations from tags (e.g. "@DisplayName")
        new UnitTestPreProcessorResults(unitTestResult).bindToSolution(solution);
    }

    @Override
    public void testFailure(Failure failure) throws Exception
    {
        super.testFailure(failure);
        JUnitTest jUnitTest = new JUnitTest(failure.getDescription());
        UnitTestResult unitTestResult =
                new UnitTestResult(jUnitTest, UnitTestResult.UnitTestResultFlag.FAIL, failure.getMessage());
        new UnitTestPreProcessorResults(unitTestResult).bindToSolution(solution);
    }
}
