package tech.clegg.gradeer.execution.java;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.clegg.gradeer.GlobalsTest;
import tech.clegg.gradeer.Gradeer;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class JavaExecutionManagerTest {
    Configuration config = new Configuration(GlobalsTest.JSON_CONFIG_EXECUTABLE_TEST_ENV);
    Gradeer gradeer = new Gradeer(config);

    @BeforeAll
    static void setup() {
        GlobalsTest.deleteOutputDir(GlobalsTest.JSON_CONFIG_EXECUTABLE_TEST_ENV);
    }

    @Test
    void runsClass() throws InterruptedException {
        gradeer.startEnvironment();
        List<ClassExecutionTemplate> preManualClassesToExecute = config.getPreManualJavaClassesToExecute();

        ClassExecutionTemplate classExecutionTemplate = new ClassExecutionTemplate();
        classExecutionTemplate.setFullClassName("task.ExampleTask");

        Optional<Solution> solution = gradeer.getStudentSolutions().stream()
                .filter(s -> s.getDirectory().endsWith("correct"))
                .findFirst();
        if (solution.isEmpty()) {
            fail("Could not load solution");
        }

        JavaExecutionManager execManager = new JavaExecutionManager(config, classExecutionTemplate, solution.get());
        execManager.start();
        execManager.getJavaExecution().join(1000);

        Path path = Paths.get(config.getSolutionCapturedOutputDir() +
                File.separator + solution.get().getIdentifier() + "-output.txt");
        try {
            List<String> capturedOutput = Files.readAllLines(path);
            assertThat(capturedOutput)
                    .anyMatch(l -> l.contains("Running ExampleTask"))
                    .anyMatch(l -> l.contains("resultA: 4"));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not open solution captured output file");
        }
    }

    @Test
    void runsClassWithArguments() throws InterruptedException {
        gradeer.startEnvironment();
        List<ClassExecutionTemplate> preManualClassesToExecute = config.getPreManualJavaClassesToExecute();

        ClassExecutionTemplate classExecutionTemplate = new ClassExecutionTemplate();
        classExecutionTemplate.setFullClassName("task.ExampleTaskWithArg");
        classExecutionTemplate.setArgs(new String[] { "Hello" } );
        Optional<Solution> solution = gradeer.getStudentSolutions().stream()
                .filter(s -> s.getDirectory().endsWith("correct"))
                .findFirst();
        if (solution.isEmpty()) {
            fail("Could not load solution");
        }

        JavaExecutionManager execManager = new JavaExecutionManager(config, classExecutionTemplate, solution.get());
        execManager.start();
        execManager.getJavaExecution().join(1000);

        Path path = Paths.get(config.getSolutionCapturedOutputDir() +
                File.separator + solution.get().getIdentifier() + "-output.txt");
        try {
            List<String> capturedOutput = Files.readAllLines(path);
            assertThat(capturedOutput)
                    .anyMatch(l -> l.contains("Running ExampleTaskWithArg:Hello"))
                    .anyMatch(l -> l.contains("input:Hello"));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not open solution captured output file");
        }
    }
}
