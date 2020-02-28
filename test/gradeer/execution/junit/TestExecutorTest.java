package gradeer.execution.junit;

import gradeer.Gradeer;
import gradeer.TestGlobals;
import gradeer.configuration.Configuration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TestExecutorTest
{
    Configuration config = new Configuration(TestGlobals.JSON_CONFIG);
    Gradeer gradeer = new Gradeer(config);

    @Test
    void testTestExecution()
    {
        gradeer.getUnitTests().forEach(t -> {
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