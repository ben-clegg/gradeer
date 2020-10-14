package tech.anonymousname.gradeer;

import tech.anonymousname.gradeer.configuration.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        assertFalse(gradeer.getEnabledTestSuites().isEmpty());
    }

    @Test
    void getConfiguration()
    {
        assertEquals(config, gradeer.getConfiguration());
    }

    @Test
    void testSourcesCompiled()
    {
        // Model solutions
        gradeer.getModelSolutions().forEach(m -> m.getSources().forEach(src -> Assertions.assertTrue(src.isCompiled())));
        // Unit tests
        assertTrue(gradeer.getEnabledTestSuites().size() > 0);
        gradeer.getEnabledTestSuites()
                .forEach(t -> Assertions.assertTrue(t.isCompiled()));
        // Student solutions
        gradeer.getStudentSolutions().forEach(s -> s.getSources().forEach(src -> Assertions.assertTrue(src.isCompiled())));
    }
}