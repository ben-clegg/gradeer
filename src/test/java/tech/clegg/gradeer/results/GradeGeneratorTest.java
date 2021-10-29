package tech.clegg.gradeer.results;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.clegg.gradeer.Gradeer;
import tech.clegg.gradeer.GlobalsTest;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class GradeGeneratorTest
{

    static Configuration config = new Configuration(GlobalsTest.JSON_CONFIG_GRADING_TEST_ENV);
    static Gradeer gradeer = new Gradeer(config);
    static GradeGenerator gradeGenerator;
    static Collection<Solution> solutions;

    @BeforeAll
    public static void setup()
    {
        // Reset
        GlobalsTest.deleteOutputDir(GlobalsTest.JSON_CONFIG_GRADING_TEST_ENV);

        // Run Gradeer
        solutions = gradeer.getStudentSolutions();

        ResultsGenerator resultsGenerator = gradeer.startEnvironment();
        resultsGenerator.run();
        gradeGenerator = new GradeGenerator(resultsGenerator.checkProcessors);
    }

    @Test
    public void testCorrectSolutionGrades()
    {
        try
        {
            Solution solution = solutions.stream()
                    .filter(s -> s.getIdentifier().equals("correct"))
                    .findFirst().get();

            double grade = gradeGenerator.generateGrade(solution);
            assertEquals(100.0, grade, 0.00001);

        } catch (NoSuchElementException e) {
            fail("Solution not found");
        }
    }

    @Test
    public void testIncorrectSolutionAGrades()
    {
        try
        {
            Solution solution = solutions.stream()
                    .filter(s -> s.getIdentifier().equals("incorrectA"))
                    .findFirst().get(); // methodA and methodC will produce faults

            double grade = gradeGenerator.generateGrade(solution);
            assertEquals(40.0, grade, 0.00001);

        } catch (NoSuchElementException e) {
            fail("Solution not found");
        }
    }

    @Test
    public void testIncorrectSolutionBGrades()
    {
        try
        {
            Solution solution = solutions.stream()
                    .filter(s -> s.getIdentifier().equals("incorrectB"))
                    .findFirst().get(); // methodA and methodC will produce faults

            double grade = gradeGenerator.generateGrade(solution);
            assertEquals(40, grade, 0.00001);

        } catch (NoSuchElementException e) {
            fail("Solution not found");
        }
    }

    @Test
    public void testIncorrectSolutionCGrades()
    {
        try
        {
            Solution solution = solutions.stream()
                    .filter(s -> s.getIdentifier().equals("incorrectC"))
                    .findFirst().get(); // Only methodC will produce faults

            double grade = gradeGenerator.generateGrade(solution);
            assertEquals(80.0, grade, 0.00001);

        } catch (NoSuchElementException e) {
            fail("Solution not found");
        }
    }

    @Test
    public void testMultipleSolutionsDifferentResults()
    {
        Solution correct = solutions.stream()
                .filter(s -> s.getIdentifier().equals("correct"))
                .findFirst().get();
        assertEquals(100.0, gradeGenerator.generateGrade(correct), 0.00001);

        Solution incorrectA = solutions.stream()
                .filter(s -> s.getIdentifier().equals("incorrectA"))
                .findFirst().get();
        assertEquals(40.0, gradeGenerator.generateGrade(incorrectA), 0.00001);
    }
}
