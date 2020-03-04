package gradeer.configuration;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Configuration
{
    private static Logger logger = LogManager.getLogger(Configuration.class);

    private Path rootDir;
    private Path studentSolutionsDir;
    private Path modelSolutionsDir;
    private Path testsDir;
    private Path dependenciesDir;
    private Path testDependenciesDir;

    public Configuration(Path jsonFile)
    {
        try
        {
            // Load json file's parent dir as default root dir
            rootDir = jsonFile.getParent();
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
            rootDir = Paths.get(json.rootDirPath);

        // Load student solutions dir. If it doesn't exist directly, it's likely to be a local directory
        studentSolutionsDir = loadLocalOrAbsolutePath(json.studentSolutionsDirPath);

        // Load model solutions dir. If it doesn't exist directly, it's likely to be a local directory
        modelSolutionsDir = loadLocalOrAbsolutePath(json.modelSolutionsDirPath);

        testsDir = loadLocalOrAbsolutePath(json.testsDirPath);

        dependenciesDir = loadLocalOrAbsolutePath(json.dependenciesDirPath);
        testDependenciesDir = loadLocalOrAbsolutePath(json.testDependenciesDirPath);
    }

    /**
     * Load a path. If the path doesn't exist in an absolute context, it's likely to be a local directory
     * @param uri the path to load
     * @return the specified path, in either the absolute context, or the local context if this doesn't exist
     */
    private Path loadLocalOrAbsolutePath(String uri)
    {
        if(uri == null)
            return null;

        Path p = Paths.get(uri);
        if(Files.notExists(p))
            p = Paths.get(rootDir.toString() + File.separator + uri);
        return p;
    }

    public Path getStudentSolutionsDir()
    {
        return studentSolutionsDir;
    }

    public Path getModelSolutionsDir()
    {
        return modelSolutionsDir;
    }

    public Path getTestsDir()
    {
        return testsDir;
    }

    public Path getTestDependenciesDir()
    {
        return testDependenciesDir;
    }

    public Path getDependenciesDir()
    {
        return dependenciesDir;
    }
}

class ConfigurationJSON
{
    String rootDirPath;
    String studentSolutionsDirPath;
    String modelSolutionsDirPath;
    String testsDirPath;
    String dependenciesDirPath;
    String testDependenciesDirPath;

    public static ConfigurationJSON loadJSON(Path jsonFile) throws FileNotFoundException
    {
        Gson gson = new Gson();
        Reader jsonReader = new FileReader(jsonFile.toFile());
        return gson.fromJson(jsonReader, ConfigurationJSON.class);
    }
}