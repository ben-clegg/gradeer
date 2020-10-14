package tech.anonymousname.gradeer;

import tech.anonymousname.gradeer.checks.Check;
import tech.anonymousname.gradeer.checks.TestSuiteCheck;
import tech.anonymousname.gradeer.checks.generation.CheckGenerator;
import tech.anonymousname.gradeer.configuration.Configuration;
import tech.anonymousname.gradeer.configuration.Environment;
import tech.anonymousname.gradeer.execution.junit.TestSuite;
import tech.anonymousname.gradeer.results.ResultsGenerator;
import tech.anonymousname.gradeer.subject.compilation.JavaCompiler;
import tech.anonymousname.gradeer.misc.ErrorCode;
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

        // TODO Load checks from JSON
        CheckGenerator checkGenerator = new CheckGenerator(configuration, modelSolutions);

        // TODO For TestSuiteChecks, add specific TestExecutors and TestSuites

        // TODO Add CheckProcessor for checks to results generator
        // TODO Report & remove Checks that fail on a model solution

        return resultsGenerator;
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
