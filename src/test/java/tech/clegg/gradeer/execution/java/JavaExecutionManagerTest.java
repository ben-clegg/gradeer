package tech.clegg.gradeer.execution.java;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.clegg.gradeer.GlobalsTest;
import tech.clegg.gradeer.Gradeer;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;

public class JavaExecutionManagerTest {
    Configuration config = new Configuration(GlobalsTest.JSON_CONFIG_GRADING_TEST_ENV);
    Gradeer gradeer = new Gradeer(config);

    @BeforeAll
    static void setup() {
        GlobalsTest.deleteOutputDir(GlobalsTest.JSON_CONFIG_LIFT);
    }

    @Test
    void runsClass() throws InterruptedException {
        gradeer.startEnvironment();
        List<ClassExecutionTemplate> preManualClassesToExecute = config.getPreManualJavaClassesToExecute();

        ClassExecutionTemplate classExecutionTemplate = new ClassExecutionTemplate();
        classExecutionTemplate.setFullClassName("task.ExampleTask");

        Optional<Solution> s = gradeer.getStudentSolutions().stream().findFirst();
        if (s.isEmpty()) {
            fail("Could not load solution");
        }

        JavaExecutionManager execManager = new JavaExecutionManager(config, classExecutionTemplate, s.get());
        execManager.start();
        execManager.getJavaExecution().join(1000);
    }
}
