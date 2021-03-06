package tech.clegg.gradeer.execution.junit;

import tech.clegg.gradeer.Gradeer;
import tech.clegg.gradeer.TestGlobals;
import tech.clegg.gradeer.configuration.Configuration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TestTestExecutor
{
    Configuration config = new Configuration(TestGlobals.JSON_CONFIG_LIFT);
    Gradeer gradeer = new Gradeer(config);

    @Test
    void testTestExecution()
    {
        gradeer.getEnabledTestSuites().forEach(t -> {
            TestExecutor testExecutor = new TestExecutor(t, config);
            TestResult result = testExecutor.execute(new ArrayList<>(gradeer.getModelSolutions()).get(0));
            System.out.println(result.toString());
            switch (t.getBaseName())
            {
                case "TestDummyA":
                    assertTrue(result.allTestsPass());
                    assertEquals(1.0, result.proportionPassing());
                    break;
                case "TestDummyB":
                    assertFalse(result.allTestsPass());
                    assertEquals(0.0, result.proportionPassing());
                    break;
                case "TestDummyC":
                    assertFalse(result.allTestsPass());
                    assertEquals(0.5, result.proportionPassing());
                    break;
                default:
                    break;
            }
        });
    }

}