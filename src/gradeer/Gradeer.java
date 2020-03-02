package gradeer;

import gradeer.checks.Check;
import gradeer.checks.TestSuiteCheck;
import gradeer.configuration.Configuration;
import gradeer.configuration.Environment;
import gradeer.execution.junit.TestExecutor;
import gradeer.execution.junit.TestResult;
import gradeer.execution.junit.TestSuite;
import gradeer.execution.junit.TestSuiteLoader;
import gradeer.grading.GradeGenerator;
import gradeer.io.compilation.JavaCompiler;
import gradeer.misc.ErrorCode;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Gradeer
{
    private static Logger logger = LogManager.getLogger(Gradeer.class);

    private Configuration configuration;

    private Collection<TestSuite> testSuites;
    private Collection<Solution> modelSolutions;
    private Collection<Solution> studentSolutions;
    private Collection<Check> checks;

    public static void main(String[] args)
    {
        Path configJSON = Paths.get(args[0]);
        if(Files.notExists(configJSON))
        {
            logger.error("Config JSON file " + configJSON.toString() + " does not exist!");
            System.exit(ErrorCode.NO_CONFIG_FILE.getCode());
        }

        Configuration config = new Configuration(configJSON);
        Gradeer gradeer = new Gradeer(config);
        gradeer.run();
    }

    public Gradeer(Configuration config)
    {
        testSuites = new ArrayList<>();
        modelSolutions = new ArrayList<>();
        studentSolutions = new ArrayList<>();
        checks = new ArrayList<>();

        configuration = config;
        Environment.init();
        init();
    }

    private void init()
    {
        loadModelSolutions();
        loadUnitTests(new ArrayList<>(modelSolutions).get(0));
        loadChecks();
        loadStudentSolutions();
    }

    public void run()
    {
        GradeGenerator gradeGenerator = new GradeGenerator(checks);
        studentSolutions.forEach(s -> logger.info(s.getDirectory() + " : " + gradeGenerator.generateGrade(s)));
    }

    private void loadChecks()
    {
        /*
        Run each test suite on every model solution.
        If a suite is valid for all model solutions, generate a check for it.
        A suite is valid if it has >0 tests and passes on the model solution.
         */
        testSuites.forEach(t -> {
            boolean valid = true;
            TestExecutor testExecutor = new TestExecutor(t, getConfiguration());

            for (Solution ms : modelSolutions)
            {
                if (!valid)
                    break;

                TestResult result = testExecutor.execute(ms);
                if(!result.allTestsPass())
                    valid = false;
                if(result.getTotalTests() < 1)
                    valid = false;
            }

            if (valid)
                checks.add(new TestSuiteCheck(t, getConfiguration()));
        });
    }

    private void loadUnitTests(Solution modelSolution)
    {
        testSuites = new TestSuiteLoader(configuration.getTestsDir()).getTestSuites();

        logger.info("Compiling " + testSuites.size() + " unit tests...");

        ArrayList<Path> auxClassPath = new ArrayList<>();
        auxClassPath.add(configuration.getTestsDir());
        JavaCompiler testCompiler = JavaCompiler.createCompiler(modelSolution, auxClassPath);
        testSuites.forEach(t -> testCompiler.compile(t, getConfiguration()));
    }

    private List<Solution> loadSolutions(Path solutionsRootDir)
    {
        List<Solution> solutions = new ArrayList<>();
        try
        {
            Files.newDirectoryStream(solutionsRootDir).forEach(
                    p -> {
                        if(Files.isDirectory(p))
                            solutions.add(new Solution(p));
                    });
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            logger.error("Solution directories in " + solutionsRootDir + " could not be loaded.");
        }
        solutions.forEach(solution -> {
            JavaCompiler compiler = JavaCompiler.createCompiler(solution);
            solution.getSources().forEach(src -> compiler.compile(src, getConfiguration()));
        });
        return solutions;
    }

    private void loadStudentSolutions()
    {
        studentSolutions = loadSolutions(configuration.getStudentSolutionsDir());
    }

    private void loadModelSolutions()
    {
        modelSolutions = loadSolutions(configuration.getModelSolutionsDir());

    }






    public Configuration getConfiguration()
    {
        return configuration;
    }

    public Collection<Solution> getStudentSolutions()
    {
        return studentSolutions;
    }

    public Collection<Solution> getModelSolutions()
    {
        return modelSolutions;
    }

    public Collection<TestSuite> getTestSuites()
    {
        return testSuites;
    }
}
