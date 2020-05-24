package tech.clegg.gradeer.checks.generation;

import com.google.gson.Gson;
import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.TestSuiteCheck;
import tech.clegg.gradeer.checks.generation.json.CheckJSONEntry;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.junit.TestExecutor;
import tech.clegg.gradeer.execution.junit.TestResult;
import tech.clegg.gradeer.execution.junit.TestSuite;
import tech.clegg.gradeer.execution.junit.TestSuiteLoader;
import tech.clegg.gradeer.subject.JavaSource;
import tech.clegg.gradeer.subject.compilation.JavaCompiler;
import tech.clegg.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.*;

public class TestSuiteCheckGenerator extends CheckGenerator
{
    private Collection<TestSuite> testSuites;
    private static Logger logger = LogManager.getLogger(TestSuiteCheckGenerator.class);

    public TestSuiteCheckGenerator(Configuration configuration, Collection<Solution> modelSolutions)
    {
        super(configuration, modelSolutions);
    }

    @Override
    void generate()
    {
        testSuites = new TestSuiteLoader(getConfiguration().getTestsDir()).getTestSuites();

        System.out.println("Compiling " + testSuites.size() + " unit tests...");

        JavaCompiler compiler = JavaCompiler.createCompiler(getConfiguration());
        logger.info("Checking for test dependencies at " + getConfiguration().getTestDependenciesDir() + "...");

        if(getModelSolutions().size() < 1)
            logger.error("No compiled model solutions available.");
        Solution modelSolution = new ArrayList<>(getModelSolutions()).get(0);
        if (getConfiguration().getTestDependenciesDir() != null &&
                Files.exists(getConfiguration().getTestDependenciesDir()))
        {
            logger.info("Compiling test dependencies at " + getConfiguration().getTestDependenciesDir());
            compiler.compileDir(getConfiguration().getTestDependenciesDir(), modelSolution);
        }
        compiler.compileTests(modelSolution);


        logger.info("Generating checks...");
        /*
        Run each test suite on every model solution.
        If a suite is valid for all model solutions, generate a check for it.
        A suite is valid if it has >0 tests and passes on the model solution.
         */

        List<Check> invalidChecks = new ArrayList<>();

        testSuites.stream().filter(JavaSource::isCompiled).forEach(t -> {
            boolean valid = true;
            logger.info("Checking validity of compiled test " + t);
            TestExecutor testExecutor = new TestExecutor(t, getConfiguration());
            Check c = new TestSuiteCheck(t, getConfiguration());

            for (Solution ms : getModelSolutions())
            {
                if (!valid)
                    break;

                TestResult result = testExecutor.execute(ms);
                logger.info(result);

                if(getConfiguration().isSkipChecksFailingOnAnyModel())
                {
                    // Remove all tests that fail on any model solution
                    if(!result.allTestsPass())
                    {
                        valid = false;
                    }
                }

                if(result.getTotalTests() < 1)
                    valid = false;
            }

            if (valid)
            {
                addCheck(c);
                logger.info("Added Check " + c);
            }
            else
                invalidChecks.add(c);
        });

        reportRemovedChecks(invalidChecks, this.getClass().getName());
        loadFeedbackAndWeights();
    }

    /**
     * Load feedback and weights from JSON
     */
    private void loadFeedbackAndWeights()
    {
        Gson gson = new Gson();
        try
        {
            CheckJSONEntry[] checkJSONEntries =
                    gson.fromJson(new FileReader(getConfiguration().getUnittestChecksJSON().toFile()),
                            CheckJSONEntry[].class);


            for (Check c : getChecks())
            {
                Optional<CheckJSONEntry> entry = Arrays.stream(checkJSONEntries)
                        .filter(j -> j.getName().toLowerCase().equals(c.getName().toLowerCase())).findFirst();
                if(entry.isPresent())
                {
                    c.setFeedback(entry.get().getFeedbackCorrect(), entry.get().getFeedbackIncorrect());
                    c.setWeight(entry.get().getWeight());
                }
                else
                    logger.error("Could not load parameters for " + c.getName() + ", using defaults.");
            }

        }
        catch (FileNotFoundException e)
        {
            logger.error("Critical error: could not load weights and feedback entries for unit tests, using default values.");
            e.printStackTrace();
        }
    }
}
