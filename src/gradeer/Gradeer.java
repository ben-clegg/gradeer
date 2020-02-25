package gradeer;

import gradeer.configuration.Configuration;
import gradeer.configuration.Environment;
import gradeer.execution.junit.UnitTest;
import gradeer.execution.junit.UnitTestLoader;
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

    private void init()
    {
        loadModelSolutions();

        loadUnitTests();

        // TODO Load checks

        loadStudentSolutions();
    }

    private void loadUnitTests()
    {
        unitTests = new UnitTestLoader(configuration.getTestsDir()).getUnitTests();
    }

    private void loadStudentSolutions()
    {
        try
        {
            studentSolutions = new ArrayList<>();
            Files.newDirectoryStream(configuration.getStudentSolutionsDir()).forEach(
                    p -> {
                        if(Files.isDirectory(p))
                            studentSolutions.add(new Solution(p));
                    });
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            logger.error("Student solutions could not be loaded.");
        }
    }

    private void loadModelSolutions()
    {
        try
        {
            modelSolutions = new ArrayList<>();
            Files.newDirectoryStream(configuration.getModelSolutionsDir()).forEach(
                    p -> {
                        if(Files.isDirectory(p))
                            modelSolutions.add(new Solution(p));
                    });
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            logger.error("Model solutions could not be loaded.");
        }
    }



    public Gradeer(Configuration config)
    {
        configuration = config;
        Environment.init();
        init();
    }

    public void run()
    {

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
