package tech.clegg.gradeer.execution.testing.junit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.clegg.gradeer.GlobalsTest;
import tech.clegg.gradeer.Gradeer;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.input.TestSourceFile;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessor;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessorResults;
import tech.clegg.gradeer.preprocessing.testing.UnitTestResult;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class TestExecutorJUnit5Test
{
    Configuration config = new Configuration(GlobalsTest.JSON_CONFIG_LIFT);
    Gradeer gradeer = new Gradeer(config);

    @BeforeAll
    static void setup()
    {
        GlobalsTest.deleteOutputDir(GlobalsTest.JSON_CONFIG_LIFT);
    }

    // TODO Make tests for the correct loading for test and source dependencies

    @Test
    void testTestExecutionJUnit5() {
        gradeer.startEnvironment();

        Collection<TestSourceFile> testSources = config.getTestSourceFilesMap().get(JUnitTestEngine.class);

        Solution studentA = gradeer.getStudentSolutions().stream()
                .filter(s -> s.getIdentifier().equals("testStudentA"))
                .findFirst().get();

        JUnitTestEngine testEngine = new JUnitTestEngine(config);

        // Run tests
        for (TestSourceFile testSourceFile : testSources)
        {
            testEngine.execute(studentA, testSourceFile);
        }

        // Should be 12 tests executed
        UnitTestPreProcessorResults results =
                (UnitTestPreProcessorResults) studentA.getPreProcessorResultsOfType(UnitTestPreProcessor.class);
        assertEquals(10, results.getExecutedUnitTests().size());

        Collection<UnitTestResult> testResults = results.getTestResults();

        // Expected test results
        Map<String, UnitTestResult.UnitTestResultFlag> resultFlagMap = new HashMap<>();
        //resultFlagMap.put()
        resultFlagMap.put("testpack.TestDummyA::test::Display name for test", UnitTestResult.UnitTestResultFlag.PASS);
        resultFlagMap.put("testpack.TestDummyA::test2::Display name for test2", UnitTestResult.UnitTestResultFlag.PASS);
        resultFlagMap.put("testpack.TestDummyB::test_Always_Fails::test Always Fails", UnitTestResult.UnitTestResultFlag.FAIL);
        resultFlagMap.put("testpack.TestDummyB::test_Always_Fails2::test Always Fails2", UnitTestResult.UnitTestResultFlag.FAIL);
        resultFlagMap.put("testpack.TestDummyC::testAlwaysFails::Display name for testAlwaysFails", UnitTestResult.UnitTestResultFlag.FAIL);
        resultFlagMap.put("testpack.TestDummyC::test2::Display name for test2", UnitTestResult.UnitTestResultFlag.PASS);
        resultFlagMap.put("testpack.TestLiftA::test::Display name for test", UnitTestResult.UnitTestResultFlag.PASS);
        resultFlagMap.put("testpack.TestLiftA::test2::Display name for test2", UnitTestResult.UnitTestResultFlag.PASS);
        resultFlagMap.put("testpack.TestLiftB::test::Display name for test", UnitTestResult.UnitTestResultFlag.PASS);
        resultFlagMap.put("testpack.TestLiftC::test::Display name for test", UnitTestResult.UnitTestResultFlag.FAIL);

        // Check actual results match
        for (UnitTestResult testResult : testResults)
        {
            JUnitTest jUnitTest = (JUnitTest) testResult.getUnitTest();
            Optional<String> match = resultFlagMap.keySet().stream()
                    .filter(s -> s.equals(jUnitTest.toString()))
                    .findFirst();
            if (match.isPresent())
            {
                UnitTestResult.UnitTestResultFlag expectedFlag = resultFlagMap.get(match.get());
                assertEquals(expectedFlag, testResult.getResultFlag(),
                        "Did not match expected result for " + jUnitTest.toString()
                );
            }
            else
            {
                fail("No matching result for " + jUnitTest);
            }
        }
    }

}