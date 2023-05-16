package tech.clegg.gradeer.results;

import org.junit.jupiter.api.Test;
import tech.clegg.gradeer.GlobalsTest;
import tech.clegg.gradeer.Gradeer;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.testing.junit.JUnitTestEngine;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessor;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessorResults;
import tech.clegg.gradeer.preprocessing.testing.UnitTestResult;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradeGeneratorDependenciesTest
{

    @Test
    void testStartEnvironmentWithDependencies()
    {
        Configuration config = new Configuration(GlobalsTest.JSON_CONFIG_DEPENDENCIES);
        assertTrue(config.isForceRecompilation());
        GlobalsTest.deleteOutputDir(GlobalsTest.JSON_CONFIG_DEPENDENCIES);
        Gradeer gradeer = new Gradeer(config);
        gradeer.startEnvironment();
        Collection<Solution> solutions = gradeer.getStudentSolutions();
        assertEquals(1, gradeer.getModelSolutions().size());
        assertEquals(2, solutions.size());
        solutions.forEach((s) -> assertTrue(s.isCompiled()));

        Solution passing = gradeer.getStudentSolutions().stream()
                .filter(s -> s.getIdentifier().equals("correct"))
                .findFirst().get();

        Solution failing = gradeer.getStudentSolutions().stream()
                .filter(s -> s.getIdentifier().equals("incorrect"))
                .findFirst().get();

        JUnitTestEngine testEngine = new JUnitTestEngine(config);

        testEngine.execute(passing);
        testEngine.execute(failing);

        // Should be 1 test executed
        UnitTestPreProcessorResults passingResults =
                (UnitTestPreProcessorResults) passing.getPreProcessorResultsOfType(UnitTestPreProcessor.class);
        assertEquals(1, passingResults.getExecutedUnitTests().size());

        UnitTestPreProcessorResults failingResults =
                (UnitTestPreProcessorResults) failing.getPreProcessorResultsOfType(UnitTestPreProcessor.class);
        assertEquals(1, failingResults.getExecutedUnitTests().size());


        UnitTestResult passingUnitTestResults = passingResults.getTestResults().stream().findFirst().orElse(null);
        assertTrue(passingUnitTestResults.getResultFlag().equals(UnitTestResult.UnitTestResultFlag.PASS));

        UnitTestResult failingUnitTestResults = failingResults.getTestResults().stream().findFirst().orElse(null);
        assertTrue(failingUnitTestResults.getResultFlag().equals(UnitTestResult.UnitTestResultFlag.FAIL));
    }
}
