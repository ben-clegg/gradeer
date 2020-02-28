package gradeer;

import gradeer.configuration.Configuration;
import gradeer.configuration.Environment;
import gradeer.execution.junit.UnitTest;
import gradeer.execution.junit.UnitTestLoader;
import gradeer.io.compilation.JavaCompiler;
import gradeer.misc.ErrorCode;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
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

    private Collection<UnitTest> unitTests;
    private Collection<Solution> studentSolutions;
    private Collection<Solution> modelSolutions;


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
        configuration = config;
        Environment.init();
        init();
    }


    private void init()
    {
        loadModelSolutions();

        loadUnitTests(new ArrayList<>(modelSolutions).get(0));

        // TODO Load checks

        loadStudentSolutions();
    }

    public void run()
    {

    }

    private void loadUnitTests(Solution modelSolution)
    {
        unitTests = new UnitTestLoader(configuration.getTestsDir()).getUnitTests();

        logger.info("Compiling " + unitTests.size() + " unit tests...");

        ArrayList<Path> auxClassPath = new ArrayList<>();
        auxClassPath.add(configuration.getTestsDir());
        JavaCompiler testCompiler = JavaCompiler.createCompiler(modelSolution, auxClassPath);
        unitTests.forEach(t -> testCompiler.compile(t, getConfiguration()));
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

    public Collection<UnitTest> getUnitTests()
    {
        return unitTests;
    }
}
