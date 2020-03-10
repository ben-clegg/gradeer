package gradeer;

import gradeer.checks.Check;
import gradeer.checks.TestSuiteCheck;
import gradeer.checks.generation.TestSuiteCheckGenerator;
import gradeer.configuration.Configuration;
import gradeer.configuration.Environment;
import gradeer.execution.checkstyle.CheckstyleExecutor;
import gradeer.execution.junit.TestSuite;
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
import java.util.stream.Collectors;

public class Gradeer
{
    private static Logger logger = LogManager.getLogger(Gradeer.class);

    private Configuration configuration;

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
        if(configuration.isCheckstyleEnabled() && configuration.getCheckstyleXml() != null)
        {
            CheckstyleExecutor checkstyleExecutor = new CheckstyleExecutor(configuration);
            modelSolutions.forEach(checkstyleExecutor::execute);
            modelSolutions.forEach(s -> logger.info(checkstyleExecutor.getMessages(s)));
            System.exit(0);
        }
        if(configuration.isTestSuitesEnabled())
        {
            TestSuiteCheckGenerator testSuiteCheckGenerator = new TestSuiteCheckGenerator(configuration, modelSolutions);
            checks.addAll(testSuiteCheckGenerator.getChecks());
        }
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
            JavaCompiler compiler = JavaCompiler.createCompiler(getConfiguration());
            compiler.compile(solution);
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

    public Collection<Check> getChecks()
    {
        return checks;
    }

    public Collection<TestSuite> getEnabledTestSuites()
    {
        return checks.stream()
                .filter(c -> c instanceof TestSuiteCheck)
                .map(c -> ((TestSuiteCheck) c).getTestSuite())
                .collect(Collectors.toList());
    }
}
