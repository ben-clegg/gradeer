package gradeer;

import gradeer.configuration.Configuration;
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
    private Collection<Solution> studentSolutions;
    private Collection<Solution> modelSolutions;


    public static void main(String[] args)
    {
        Path configJSON = Paths.get(args[0]);
        if(!configJSON.toFile().exists())
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
        // TODO load checks
        // Load solutions
        try
        {
            studentSolutions = new ArrayList<>();
            Files.newDirectoryStream(configuration.getStudentSolutionsDir()).forEach(
                    p -> {
                        if(p.toFile().isDirectory())
                            studentSolutions.add(new Solution(p.toFile()));
                    });
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            logger.error("Student solutions could not be loaded.");
        }
    }

    public Gradeer(Configuration config)
    {
        configuration = config;
        init();
    }

    public void run()
    {

    }
}
