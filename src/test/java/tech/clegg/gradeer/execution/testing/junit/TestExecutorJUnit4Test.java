package tech.clegg.gradeer.execution.testing.junit;

import org.junit.jupiter.api.BeforeAll;
import tech.clegg.gradeer.Gradeer;
import tech.clegg.gradeer.GlobalsTest;
import tech.clegg.gradeer.configuration.Configuration;
import org.junit.jupiter.api.Test;
import tech.clegg.gradeer.input.TestSourceFile;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessor;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessorResults;
import tech.clegg.gradeer.preprocessing.testing.UnitTestResult;
import tech.clegg.gradeer.solution.Solution;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TestExecutorJUnit4Test
{
    Configuration config = new Configuration(GlobalsTest.JSON_CONFIG_LIFT_JUNIT4);
    Gradeer gradeer = new Gradeer(config);
    @BeforeAll
    static void setup()
    {
        GlobalsTest.deleteOutputDir(GlobalsTest.JSON_CONFIG_LIFT_JUNIT4);
    }

    // TODO Make tests for the correct loading for test and source dependencies

    @Test
    void testTestExecutionJUnit4() {
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
        resultFlagMap.put("testpack.TestDummyA::test::test(testpack.TestDummyA)", UnitTestResult.UnitTestResultFlag.PASS);
        resultFlagMap.put("testpack.TestDummyA::test2::test2(testpack.TestDummyA)", UnitTestResult.UnitTestResultFlag.PASS);
        resultFlagMap.put("testpack.TestDummyB::testAlwaysFails::testAlwaysFails(testpack.TestDummyB)", UnitTestResult.UnitTestResultFlag.FAIL);
        resultFlagMap.put("testpack.TestDummyB::testAlwaysFails2::testAlwaysFails2(testpack.TestDummyB)", UnitTestResult.UnitTestResultFlag.FAIL);
        resultFlagMap.put("testpack.TestDummyC::testAlwaysFails::testAlwaysFails(testpack.TestDummyC)", UnitTestResult.UnitTestResultFlag.FAIL);
        resultFlagMap.put("testpack.TestDummyC::test2::test2(testpack.TestDummyC)", UnitTestResult.UnitTestResultFlag.PASS);
        resultFlagMap.put("testpack.TestLiftA::test::test(testpack.TestLiftA)", UnitTestResult.UnitTestResultFlag.PASS);
        resultFlagMap.put("testpack.TestLiftA::test2::test2(testpack.TestLiftA)", UnitTestResult.UnitTestResultFlag.PASS);
        resultFlagMap.put("testpack.TestLiftB::test::test(testpack.TestLiftB)", UnitTestResult.UnitTestResultFlag.PASS);
        resultFlagMap.put("testpack.TestLiftC::test::test(testpack.TestLiftC)", UnitTestResult.UnitTestResultFlag.FAIL);

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