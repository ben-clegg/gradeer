package gradeer.checks.generation;

import gradeer.checks.Check;
import gradeer.checks.TestSuiteCheck;
import gradeer.configuration.Configuration;
import gradeer.execution.junit.TestExecutor;
import gradeer.execution.junit.TestResult;
import gradeer.execution.junit.TestSuite;
import gradeer.execution.junit.TestSuiteLoader;
import gradeer.subject.JavaSource;
import gradeer.subject.compilation.JavaCompiler;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

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

        logger.info("Compiling " + testSuites.size() + " unit tests...");

        JavaCompiler compiler = JavaCompiler.createCompiler(getConfiguration());
        logger.info("Checking for test dependencies at " + getConfiguration().getTestDependenciesDir() + "...");

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
        testSuites.stream().filter(JavaSource::isCompiled).forEach(t -> {
            boolean valid = true;
            logger.info("Checking validity of compiled test " + t);
            TestExecutor testExecutor = new TestExecutor(t, getConfiguration());

            for (Solution ms : getModelSolutions())
            {
                if (!valid)
                    break;

                TestResult result = testExecutor.execute(ms);
                logger.info(result);
                if(!result.allTestsPass())
                    valid = false;
                if(result.getTotalTests() < 1)
                    valid = false;
            }

            if (valid)
            {
                Check c = new TestSuiteCheck(t, getConfiguration());
                addCheck(c);
                logger.info("Added Check " + c);
            }
        });
    }

    @Override
    void setWeights()
    {
        getChecks().forEach(c -> {
            // TODO implement from configuration; name match, set weight
        });
    }
}
