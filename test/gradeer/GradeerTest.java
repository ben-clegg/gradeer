package gradeer;

import gradeer.configuration.Configuration;
import gradeer.solution.Solution;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

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
        assertFalse(gradeer.getUnitTests().isEmpty());
    }

    @Test
    void getConfiguration()
    {
        assertEquals(config, gradeer.getConfiguration());
    }

}