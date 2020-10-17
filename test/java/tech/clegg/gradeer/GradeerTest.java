package tech.clegg.gradeer;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.CheckstyleCheck;
import tech.clegg.gradeer.checks.PMDCheck;
import tech.clegg.gradeer.checks.TestSuiteCheck;
import tech.clegg.gradeer.configuration.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class GradeerTest
{
    Configuration config = new Configuration(TestGlobals.JSON_CONFIG);
    Gradeer gradeer = new Gradeer(config);

    @Test
    void constructorValid()
    {
        assertFalse(gradeer.getStudentSolutions().isEmpty());
        assertFalse(gradeer.getModelSolutions().isEmpty());
        assertTrue(gradeer.getEnabledTestSuites().isEmpty());
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
        gradeer.getModelSolutions().forEach(m -> m.getSources().forEach(src -> Assertions.assertTrue(src.isCompiled())));
        // Unit tests
        assertTrue(gradeer.getEnabledTestSuites().size() > 0);
        gradeer.getEnabledTestSuites()
                .forEach(t -> Assertions.assertTrue(t.isCompiled()));
        // Student solutions
        gradeer.getStudentSolutions().forEach(s -> s.getSources().forEach(src -> Assertions.assertTrue(src.isCompiled())));
    }

    @Test
    void testChecksPassOnModel()
    {
        gradeer.startEnvironment();

        Collection<Check> checks = gradeer.getChecks();

        assertEquals(3, checks.stream().filter(c -> c.getClass().equals(TestSuiteCheck.class)).count());
        assertEquals(4, checks.stream().filter(c -> c.getClass().equals(CheckstyleCheck.class)).count());
        assertEquals(4, checks.stream().filter(c -> c.getClass().equals(PMDCheck.class)).count());
    }
}