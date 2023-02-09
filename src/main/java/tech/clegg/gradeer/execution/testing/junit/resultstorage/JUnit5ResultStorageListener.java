package tech.clegg.gradeer.execution.testing.junit.resultstorage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import tech.clegg.gradeer.execution.testing.junit.JUnitTest;
import tech.clegg.gradeer.execution.testing.junit.TestDescription;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessorResults;
import tech.clegg.gradeer.preprocessing.testing.UnitTestResult;
import tech.clegg.gradeer.solution.Solution;

public class JUnit5ResultStorageListener implements TestExecutionListener
{
    Solution solution;

    private final Logger logger = LogManager.getLogger(JUnit5ResultStorageListener.class);

    public JUnit5ResultStorageListener(Solution solution)
    {
        super();
        this.solution = solution;
    }

    public TestDescription toDescription(TestIdentifier i) {
        TestDescription d = new TestDescription();
        d.setDisplayName(i.getDisplayName());
        TestSource ts = i.getSource().orElse(null);
        if (ts instanceof MethodSource m) {
            d.setClassName(m.getJavaClass());
            d.setMethodName(m.getMethodName());
            return d;
        } else {
            logger.debug("Skipping anything that is not a test method.");
            return null;
        }
    }

    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {

        Throwable result = testExecutionResult.getThrowable().orElse(null);
        if (testIdentifier.isTest()) {
            JUnitTest jUnitTest = new JUnitTest(toDescription(testIdentifier));
            UnitTestResult unitTestResult = new UnitTestResult(jUnitTest,
                    result == null ? UnitTestResult.UnitTestResultFlag.PASS : UnitTestResult.UnitTestResultFlag.FAIL,
                    result == null ? "" : result.getMessage());
            new UnitTestPreProcessorResults(unitTestResult).bindToSolution(solution);
        }
    }
}
