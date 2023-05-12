package tech.clegg.gradeer;

import tech.clegg.gradeer.auxiliaryprocesses.MergedSolutionWriter;
import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.UnitTestCheck;
import tech.clegg.gradeer.checks.checkprocessing.CheckProcessor;
import tech.clegg.gradeer.checks.checkprocessing.CheckValidator;
import tech.clegg.gradeer.checks.generation.CheckGenerator;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.configuration.Environment;
import tech.clegg.gradeer.configuration.cli.CLIOptions;
import tech.clegg.gradeer.configuration.cli.CLIReader;
import tech.clegg.gradeer.execution.testing.UnitTest;
import tech.clegg.gradeer.execution.testing.junit.JUnitTestEngine;
import tech.clegg.gradeer.execution.testing.junit.JUnitTestSource;
import tech.clegg.gradeer.execution.testing.junit.TestSuiteLoader;
import tech.clegg.gradeer.input.TestSourceFile;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessor;
import tech.clegg.gradeer.preprocessing.testing.UnitTestPreProcessorResults;
import tech.clegg.gradeer.results.ResultsGenerator;
import tech.clegg.gradeer.results.io.DelayedFileWriter;
import tech.clegg.gradeer.solution.DefaultFlag;
import tech.clegg.gradeer.subject.JavaSource;
import tech.clegg.gradeer.compilation.JavaCompiler;
import tech.clegg.gradeer.error.ErrorCode;
import tech.clegg.gradeer.solution.Solution;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Gradeer
{
    private Configuration configuration;

    private Collection<Solution> modelSolutions = new ArrayList<>();
    private Collection<Solution> studentSolutions = new ArrayList<>();
    private Collection<Solution> mutantSolutions = new ArrayList<>();
    private Collection<Check> checks = new ArrayList<>();

    public static void main(String[] args)
    {
        // Read CLI
        CLIReader cliReader = new CLIReader(args);
        if(cliReader.hasOption(CLIOptions.HELP))
        {
            cliReader.printHelp();
            System.exit(ErrorCode.HELP_DISPLAYED.getCode());
        }

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
            gradeer.loadMutantSolutions(cliReader);

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
        // For UnitTestChecks, load TestSuites
        loadTests(checks);

        // Report & remove Checks that fail on a model solution
        if(configuration.isVerifyChecksWithModelSolutions())
        {
            CheckValidator checkValidator = new CheckValidator(modelSolutions, configuration);
            checks = checkValidator.filterValidChecks(checks);
        }

        // If mutants are present, run checks on them and report any mutants that are not detected by any checks
        if(!mutantSolutions.isEmpty())
        {
            mutationAnalysis();
        }

        // Add CheckProcessor for Checks to ResultsGenerator
        CheckProcessor checkProcessor = new CheckProcessor(checks, configuration);
        resultsGenerator.addCheckProcessor(checkProcessor);

        return resultsGenerator;
    }

    private void mutationAnalysis()
    {
        System.out.println("Performing mutation analysis...");

        // Run automated checks on mutants
        Collection<Check> autoChecks = checks.stream().filter(c -> c.getClass() == UnitTestCheck.class)
                .collect(Collectors.toList());
        // TODO filter by additional check types defined in config (e.g. style checking)

        CheckProcessor autoCheckProcessor = new CheckProcessor(
                autoChecks,
                configuration
        );
        for (Solution m : mutantSolutions)
            autoCheckProcessor.runChecks(m);

        // Summarise mutation performance for each check
        DelayedFileWriter f = new DelayedFileWriter();
        f.addLine("Check, % mutants detected by check, average base score of check for mutants");
        for (Check c : autoChecks)
        {
            // Display percent of mutants that check detects (i.e base score < 1.0)
            double detectedCount = mutantSolutions.stream().map(m -> m.getCheckResult(c).getUnweightedScore())
                    .filter(d -> d < 1.0)
                    .count();
            double percentDetected = (detectedCount / mutantSolutions.size()) * 100;
            System.out.println("% mutants detected by Check " + c.getName() + ": " + percentDetected);

            // Also display average base score of check
            double totalBaseScore = mutantSolutions.stream().map(m -> m.getCheckResult(c).getUnweightedScore())
                    .reduce(0.0, Double::sum);
            double avgBaseScore = totalBaseScore / mutantSolutions.size();
            System.out.println("Average base score of Check " + c.getName() + " on mutants; " + avgBaseScore);

            // Store recorded statistics
            f.addLine(c.getName() + ", " + percentDetected + ", " + avgBaseScore);
        }
        f.write(Paths.get(configuration.getOutputDir() + File.separator + "mutantCheckPerformance.csv"));



        // Store & display mutants that are not detected by any checks
        Collection<Solution> undetectedMutants = mutantSolutions.stream()
                .filter(m -> m.getAllCheckResults().stream().noneMatch(cr -> cr.getUnweightedScore() < 1.0))
                .collect(Collectors.toList());
        if(!undetectedMutants.isEmpty())
        {
            System.out.println("Mutants not detected by any checks: ");
            System.out.println(Arrays.toString(undetectedMutants.stream().map(Solution::getIdentifier).toArray()));

            // Store
            DelayedFileWriter file = new DelayedFileWriter();
            for (Solution m : undetectedMutants)
                file.addLine(m.getIdentifier());
            file.write(Paths.get(configuration.getOutputDir() + File.separator + "undetectedMutants.txt"));

            // Exit if any mutants are not detected
            System.err.println("Some mutants were not detected by any checks. " +
                    "Please review the output, and consider making the checks more robust.");
            System.exit(ErrorCode.MUTANTS_UNDETECTED.getCode());
        }
        else
        {
            System.out.println("All mutants detected by at least one check.");
        }
    }

    private void loadTests(Collection<Check> checks)
    {
        // Get existing UnitTestChecks to attach TestSuites to
        Collection<UnitTestCheck> unitTestChecks = checks.stream()
                .filter(c -> c.getClass().equals(UnitTestCheck.class))
                .map(c -> (UnitTestCheck)c)
                .collect(Collectors.toList());

        // Load and compile tests
        // TODO Generify to accept other test engines / languages
        Collection<JUnitTestSource> testSources = new TestSuiteLoader(getConfiguration().getTestsDir()).getTestSuites();
        System.out.println("Compiling " + testSources.size() + " unit tests...");
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


        // Populate configuration with test sources
        for (TestSourceFile testSourceFile : testSources)
        {
            configuration.addTestSourceFile(JUnitTestEngine.class, testSourceFile);
        }

        // Execute test sources on model solution(s)
        UnitTestPreProcessor unitTestPreProcessor = new UnitTestPreProcessor(modelSolution, configuration);
        unitTestPreProcessor.start();

        // Link tests to checks parsed from JSON
        UnitTestPreProcessorResults preProcessorResults =
                (UnitTestPreProcessorResults) modelSolution.getPreProcessorResultsOfType(UnitTestPreProcessor.class);
        Collection<UnitTest> identifiedUnitTests = preProcessorResults.getExecutedUnitTests();
        Collection<UnitTest> linkedUnitTests = new HashSet<>();
        for (UnitTestCheck parsedCheck : unitTestChecks)
        {
            Optional<UnitTest> matchingTest = identifiedUnitTests.stream()
                    .filter(parsedCheck::matchesUnitTest)
                    .findFirst();
            if (matchingTest.isPresent())
            {
                parsedCheck.setUnitTest(matchingTest.get());
                linkedUnitTests.add(matchingTest.get());
            }
        }

        // Generate UnitTestChecks from unlinked tests
        if (configuration.isAutoGenerateUnitTestChecks())
        {
            // Identify tests that are not yet linked
            Collection<UnitTest> unlinkedUnitTests = new HashSet<>(identifiedUnitTests);
            unlinkedUnitTests.removeAll(linkedUnitTests);
            // Create a check and add it to the checks collection
            unlinkedUnitTests.forEach(t -> checks.add(new UnitTestCheck(t, configuration)));
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

        for (Solution s : studentSolutions)
        {
            if(!s.isCompiled())
                System.err.println("[SEVERE] Student solution '" + s.getIdentifier() + "' was not compiled.");
        }

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
                System.err.println("[SEVERE] Model solution '" + m.getIdentifier() + "' was not compiled.");
        }
    }

    private void loadMutantSolutions(CLIReader cliReader)
    {
        if(!cliReader.hasOption(CLIOptions.MUTANT_SOLUTIONS))
            return;

        Path dir = configuration.loadLocalOrAbsolutePath(cliReader.getInputValue(CLIOptions.MUTANT_SOLUTIONS));
        mutantSolutions = loadSolutions(dir);

        // Add copies of model solution JavaSources if those with matching names do not exist for each mutant
        // These must be copied in filesystem; execution assumes solution contains all non-sourceDependencies classes
        // Necessary since mutants are for single classes, but multiple dependencies may be required
        // TODO allow this to be disabled via Configuration

        if (modelSolutions.isEmpty())
            return;
        Solution model = modelSolutions.stream().findFirst().get();
        Collection<JavaSource> modelSources = model.getSources();
        for (Solution m : mutantSolutions)
        {
            // Copy solutions; files already present are automatically skipped
            for (JavaSource src : modelSources)
            {
                src.copyToDifferentSolution(model, m);
            }

            // Recheck missing sources flags
            m.getFlags().remove(DefaultFlag.MISSING_CLASS.name());
            m.checkForMissingSources(configuration.getRequiredClasses());
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

}
