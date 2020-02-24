package gradeer.configuration;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class Configuration
{
    private static Logger logger = LogManager.getLogger(Configuration.class);

    private File rootDir;
    private File studentSolutionsDir;



    public Configuration(File jsonFile)
    {
        try
        {
            // Load json file's parent dir as default root dir
            rootDir = jsonFile.getParentFile();
            // Load config variables from json
            loadFromJSON(ConfigurationJSON.loadJSON(jsonFile));
        }
        catch (IOException ioEx)
        {
            logger.error(ioEx);
            logger.error("Could not load configuration!");
        }
    }

    private void loadFromJSON(ConfigurationJSON json)
    {
        // Overwrite root dir if specified
        if(json.rootDirPath != null)
            rootDir = new File(json.rootDirPath);

        studentSolutionsDir = new File(json.studentSolutionsDirPath);
    }


}

class ConfigurationJSON
{
    public String rootDirPath;
    public String studentSolutionsDirPath;

    public static ConfigurationJSON loadJSON(File jsonFile) throws FileNotFoundException
    {
        Gson gson = new Gson();
        Reader jsonReader = new FileReader(jsonFile);
        return gson.fromJson(jsonReader, ConfigurationJSON.class);
    }
}