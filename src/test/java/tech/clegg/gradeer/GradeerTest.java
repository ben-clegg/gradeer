package tech.clegg.gradeer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.CheckstyleCheck;
import tech.clegg.gradeer.checks.PMDCheck;
import tech.clegg.gradeer.checks.UnitTestCheck;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.input.TestSourceFile;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GradeerTest
{
    Configuration config = new Configuration(GlobalsTest.JSON_CONFIG_LIFT);
    Gradeer gradeer = new Gradeer(config);

    @BeforeAll
    static void setup()
    {
        GlobalsTest.deleteOutputDir(GlobalsTest.JSON_CONFIG_LIFT);
    }

    public Collection<TestSourceFile> enabledTestSources()
    {
        return config.getTestSourceFilesMap().values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Test
    void constructorValid()
    {
        assertFalse(gradeer.getStudentSolutions().isEmpty());
        assertFalse(gradeer.getModelSolutions().isEmpty());
        assertTrue(enabledTestSources().isEmpty());
    }

    @Test
    void getConfiguration()
    {
        assertEquals(config, gradeer.getConfiguration());
    }

    @Test
    void testSourcesCompiled()
    {
        gradeer.startEnvironment();

        // Model solutions
        gradeer.getModelSolutions()
                .forEach(m -> m.getSources().forEach(src -> Assertions.assertTrue(src.isCompiled())));
        // Unit tests
        assertTrue(enabledTestSources().size() > 0);
        enabledTestSources()
                .forEach(t -> Assertions.assertTrue(t.isCompiled()));
        // Student solutions
        assertTrue(gradeer.getStudentSolutions().stream().filter(Solution::isCompiled).count() >= 3);
        gradeer.getStudentSolutions().stream().filter(Solution::isCompiled)
                .forEach(m -> m.getSources().forEach(src -> Assertions.assertTrue(src.isCompiled())));
    }

    @Test
    void testChecksPassOnModel()
    {
        gradeer.startEnvironment();

        Collection<Check> checks = gradeer.getChecks();

        assertEquals(7, checks.stream().filter(c -> c.getClass().equals(UnitTestCheck.class)).count());
        assertEquals(4, checks.stream().filter(c -> c.getClass().equals(CheckstyleCheck.class)).count());
        assertEquals(4, checks.stream().filter(c -> c.getClass().equals(PMDCheck.class)).count());
    }
}