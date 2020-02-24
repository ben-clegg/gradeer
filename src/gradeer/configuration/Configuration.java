package gradeer.configuration;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Configuration
{
    private static Logger logger = LogManager.getLogger(Configuration.class);

    private Path rootDir;
    private Path studentSolutionsDir;
    private Path modelSolutionsDir;

    public Configuration(Path jsonFile)
    {
        try
        {
            // Load json file's parent dir as default root dir
            rootDir = jsonFile.getParent();
            // Load config variables from json
            loadFromJSON(ConfigurationJSON.loadJSON(jsonFile));
            loadIndirectValues();
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
            rootDir = Paths.get(json.rootDirPath);

        // Load student solutions dir. If it doesn't exist directly, it's likely to be a local directory
        studentSolutionsDir = Paths.get(json.studentSolutionsDirPath);
        if(!studentSolutionsDir.toFile().exists())
            studentSolutionsDir = Paths.get(rootDir.toString() + File.separator + json.studentSolutionsDirPath);

        // Load model solutions dir. If it doesn't exist directly, it's likely to be a local directory
        modelSolutionsDir = Paths.get(json.modelSolutionsDirPath);
        if(!modelSolutionsDir.toFile().exists())
            modelSolutionsDir = Paths.get(rootDir.toString() + File.separator + json.modelSolutionsDirPath);
    }

    private void loadIndirectValues()
    {

    }

    public Path getStudentSolutionsDir()
    {
        return studentSolutionsDir;
    }

    public Path getModelSolutionsDir()
    {
        return modelSolutionsDir;
    }
}

class ConfigurationJSON
{
    public String rootDirPath;
    public String studentSolutionsDirPath;
    public String modelSolutionsDirPath;

    public static ConfigurationJSON loadJSON(Path jsonFile) throws FileNotFoundException
    {
        Gson gson = new Gson();
        Reader jsonReader = new FileReader(jsonFile.toFile());
        return gson.fromJson(jsonReader, ConfigurationJSON.class);
    }
}