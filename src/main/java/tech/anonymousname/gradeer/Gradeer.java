package tech.anonymousname.gradeer;

import tech.anonymousname.gradeer.auxiliaryprocesses.MergedSolutionWriter;
import tech.anonymousname.gradeer.checks.Check;
import tech.anonymousname.gradeer.checks.TestSuiteCheck;
import tech.anonymousname.gradeer.checks.checkprocessing.CheckProcessor;
import tech.anonymousname.gradeer.checks.checkprocessing.CheckValidator;
import tech.anonymousname.gradeer.checks.generation.CheckGenerator;
import tech.anonymousname.gradeer.configuration.Configuration;
import tech.anonymousname.gradeer.configuration.Environment;
import tech.anonymousname.gradeer.execution.junit.TestSuite;
import tech.anonymousname.gradeer.execution.junit.TestSuiteLoader;
import tech.anonymousname.gradeer.results.ResultsGenerator;
import tech.anonymousname.gradeer.subject.compilation.JavaCompiler;
import tech.anonymousname.gradeer.error.ErrorCode;
import tech.anonymousname.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
        if (Files.notExists(configJSON))
        {
            logger.error("Config JSON file " + configJSON.toString() + " does not exist!");
            System.exit(ErrorCode.NO_CONFIG_FILE.getCode());
        }

        Configuration config = new Configuration(configJSON);
        Gradeer gradeer = new Gradeer(config);

        ResultsGenerator resultsGenerator = gradeer.startEnvironment();
        resultsGenerator.run();

        System.out.println("Completed grading for config " + configJSON.getFileName());
        config.getTimer().end();
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
        loadStudentSolutions();
        configuration.getTimer().split("Finished solution loading.");
    }

    public ResultsGenerator startEnvironment()
    {
        ResultsGenerator resultsGenerator = new ResultsGenerator(studentSolutions, configuration);

        //  Load checks from JSON
        CheckGenerator checkGenerator = new CheckGenerator(configuration, modelSolutions);
        checks = checkGenerator.getChecks();
        // For TestSuiteChecks, load TestSuites
        loadTests(checks);

        // Report & remove Checks that fail on a model solution
        CheckValidator checkValidator = new CheckValidator(modelSolutions, configuration);
        checks = checkValidator.filterValidChecks(checks);

        // Add CheckProcessor for Checks to ResultsGenerator
        CheckProcessor checkProcessor = new CheckProcessor(checks, configuration);
        resultsGenerator.addCheckProcessor(checkProcessor);

        return resultsGenerator;
    }

    private void loadTests(Collection<Check> checks)
    {
        // Get existing TestSuiteChecks to attach TestSuites to
        Collection<TestSuiteCheck> testSuiteChecks = checks.stream()
                .filter(c -> c.getClass().equals(TestSuiteCheck.class))
                .map(c -> (TestSuiteCheck)c)
                .collect(Collectors.toList());

        // Load and compile tests
        Collection<TestSuite> testSuites = new TestSuiteLoader(getConfiguration().getTestsDir()).getTestSuites();
        System.out.println("Compiling " + testSuites.size() + " unit tests...");
        JavaCompiler compiler = JavaCompiler.createCompiler(getConfiguration());
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

        // Link compiled TestSuites to TestSuiteChecks
        for (TestSuiteCheck c : testSuiteChecks)
            c.loadTestSuite(testSuites);


        // Create default checks for unlinked TestSuites if enabled
        if(configuration.isAutoGenerateTestSuiteChecks())
        {
            Collection<TestSuite> unlinkedSuites = new HashSet<>(testSuites);
            unlinkedSuites.removeAll(
                    testSuiteChecks.stream()
                    .map(TestSuiteCheck::getTestSuite)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
            );

            for (TestSuite t : unlinkedSuites)
                checks.add(new TestSuiteCheck(t, configuration));
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
                        {
                            Solution s = new Solution(p);
                            s.checkForMissingSources(getConfiguration().getRequiredClasses());
                            solutions.add(s);
                        }
                    });
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            logger.error("Solution directories in " + solutionsRootDir + " could not be loaded.");
        }

        // Sort alphabetically
        solutions.sort(Comparator.comparing(Solution::getIdentifier));

        // Attempt to compile solutions
        List<Solution> uncompilableSolutions = new ArrayList<>();
        solutions.forEach(solution -> {
            JavaCompiler compiler = JavaCompiler.createCompiler(getConfiguration());
            if (!compiler.compile(solution))
                uncompilableSolutions.add(solution);
        });
        // Remove solutions that cannot be compiled to prevent further processing.
        solutions.removeAll(uncompilableSolutions);

        // Merge the source files of each Solution if enabled
        if(configuration.getMergedSolutionsDir() != null)
            new MergedSolutionWriter(configuration, solutions).run();

        return solutions;
    }

    private void loadStudentSolutions()
    {
        studentSolutions = loadSolutions(configuration.getStudentSolutionsDir());
        System.out.println(studentSolutions.size() + " students' solutions loaded.");

        // Filter solutions
        // Include
        if(!configuration.getIncludeSolutions().isEmpty())
        {
            studentSolutions = studentSolutions.stream()
                    .filter(s -> configuration.getIncludeSolutions().contains(s.getIdentifier()))
                    .collect(Collectors.toSet());
        }
        // Exclude
        if(!configuration.getExcludeSolutions().isEmpty())
        {
            studentSolutions = studentSolutions.stream()
                    .filter(s -> !configuration.getExcludeSolutions().contains(s.getIdentifier()))
                    .collect(Collectors.toSet());
        }
        System.out.println(studentSolutions.size() + " students' solutions present after filtering.");

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
