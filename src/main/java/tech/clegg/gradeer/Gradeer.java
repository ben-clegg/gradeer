package tech.clegg.gradeer;

import tech.clegg.gradeer.auxiliaryprocesses.MergedSolutionWriter;
import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.TestSuiteCheck;
import tech.clegg.gradeer.checks.checkprocessing.CheckProcessor;
import tech.clegg.gradeer.checks.checkprocessing.CheckValidator;
import tech.clegg.gradeer.checks.generation.CheckGenerator;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.configuration.Environment;
import tech.clegg.gradeer.configuration.cli.CLIOptions;
import tech.clegg.gradeer.configuration.cli.CLIReader;
import tech.clegg.gradeer.execution.junit.TestSuite;
import tech.clegg.gradeer.execution.junit.TestSuiteLoader;
import tech.clegg.gradeer.results.ResultsGenerator;
import tech.clegg.gradeer.subject.compilation.JavaCompiler;
import tech.clegg.gradeer.error.ErrorCode;
import tech.clegg.gradeer.solution.Solution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Gradeer
{
    private Configuration configuration;

    private Collection<Solution> modelSolutions;
    private Collection<Solution> studentSolutions;
    private Collection<Check> checks;

    public static void main(String[] args)
    {
        // Read CLI
        CLIReader cliReader = new CLIReader(args);

        try
        {
            // Setup config
            Path configJSON = Paths.get(cliReader.getInputValue(CLIOptions.CONFIGURATION_LOCATION));
            if (Files.notExists(configJSON))
            {
                System.err.println("Config JSON file " + configJSON.toString() + " does not exist!");
                System.exit(ErrorCode.NO_CONFIG_FILE.getCode());
            }
            Configuration config = new Configuration(configJSON);

            // Add included / excluded solutions
            config.getIncludeSolutions().addAll(cliReader.getArrayInputOrEmpty(CLIOptions.INCLUDE_SOLUTIONS));
            config.getExcludeSolutions().addAll(cliReader.getArrayInputOrEmpty(CLIOptions.EXCLUDE_SOLUTIONS));

            // Start Gradeer
            Gradeer gradeer = new Gradeer(config);

            ResultsGenerator resultsGenerator = gradeer.startEnvironment();
            resultsGenerator.run();

            System.out.println("Completed grading for config " + configJSON.getFileName());
            config.getTimer().end();

        } catch (IllegalArgumentException e)
        {
            // No config file
            e.printStackTrace();
            System.err.println("No configuration file defined, exiting... ");
            System.exit(ErrorCode.NO_CONFIG_FILE.getCode());
        }

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
            System.err.println("No compiled model solutions available.");
        Solution modelSolution = new ArrayList<>(getModelSolutions()).get(0);
        if (getConfiguration().getTestDependenciesDir() != null &&
                Files.exists(getConfiguration().getTestDependenciesDir()))
        {
            System.out.println("Compiling test dependencies at " + getConfiguration().getTestDependenciesDir());
            compiler.compileDir(getConfiguration().getTestDependenciesDir(), modelSolution);
        }
        compiler.compileTests(modelSolution);

        // Link compiled TestSuites to TestSuiteChecks
        for (TestSuiteCheck c : testSuiteChecks)
        {
            c.loadTestSuite(testSuites);
            if(!c.getTestSuite().isCompiled())
                System.err.println("[WARNING] Test Suite " + c.getName() + " is not compiled!");
        }


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
            System.err.println("Solution directories in " + solutionsRootDir + " could not be loaded.");
        }

        // Sort alphabetically
        solutions.sort(Comparator.comparing(Solution::getIdentifier));

        // Attempt to compile solutions
        solutions.forEach(solution -> {
            JavaCompiler compiler = JavaCompiler.createCompiler(getConfiguration());
            // Compile; this flags the solution as uncompilable if compilation fails
            compiler.compile(solution);
        });
        
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
        for (Solution m : modelSolutions)
        {
            if(!m.isCompiled())
                System.err.println("[SEVERE] Model solution " + m.getIdentifier() + " was not compiled.");
        }
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
