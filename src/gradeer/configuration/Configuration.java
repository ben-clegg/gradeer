package gradeer.configuration;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;

public class Configuration
{
    private static Logger logger = LogManager.getLogger(Configuration.class);

    private Path rootDir;
    private Path studentSolutionsDir;
    private Path modelSolutionsDir;
    private Path testsDir;
    private Path dependenciesDir;
    private Path testDependenciesDir;

    private Path libDir;
    private Collection<Path> builtLibComponents;

    private int perTestSuiteTimeout = 10000;

    private Path checkstyleXml;
    private String pmdRulesets = "java-basic,java-braces,java-clone,java-codesize,java-comments," +
            "java-coupling,java-design,java-empty,java-finalizers,java-imports,java-metrics," +
            "java-migrating,java-naming,java-sunsecure,java-strings,java-typeresolution," +
            "java-unnecessary,java-unusedcode";

    private boolean testSuitesEnabled = true;
    private boolean pmdEnabled = true;
    private boolean checkStyleEnabled = true;

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
        libDir = loadLocalOrAbsolutePath(json.libDir);

        loadBuiltLibComponents();

        if(json.perTestSuiteTimeout > 0)
            perTestSuiteTimeout = json.perTestSuiteTimeout * 1000; // Convert seconds to ms
        if(json.pmdRulesets != null && !json.pmdRulesets.isEmpty())
            pmdRulesets = json.pmdRulesets;

        checkstyleXml = loadLocalOrAbsolutePath(json.checkstyleXml);

        testSuitesEnabled = json.enableTestSuites;
        checkStyleEnabled = json.enableCheckStyle;
        pmdEnabled = json.enablePMD;
    }

    private void loadBuiltLibComponents()
    {
        builtLibComponents = new HashSet<>();

        logger.info("Loading built lib components for " + libDir);
        if(!pathExists(libDir))
        {
            return;
        }
        try
        {
            Files.walk(libDir).forEach(logger::info);
            Files.walk(libDir).filter(p ->
                    com.google.common.io.Files.getFileExtension(p.toString()).equals("jar"))
                    .forEach(builtLibComponents::add);
            Files.walk(libDir).filter(p ->
                    com.google.common.io.Files.getFileExtension(p.toString()).equals("class"))
                    .forEach(builtLibComponents::add);
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
        }
        builtLibComponents.forEach(p -> logger.info("Loaded built lib component " + p));
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

    public Path getLibDir()
    {
        return libDir;
    }

    public Collection<Path> getBuiltLibComponents()
    {
        return builtLibComponents;
    }

    public int getPerTestSuiteTimeout()
    {
        return perTestSuiteTimeout;
    }

    public String getPmdRulesets()
    {
        return pmdRulesets;
    }

    public Path getCheckstyleXml()
    {
        return checkstyleXml;
    }

    public boolean isTestSuitesEnabled()
    {
        return testSuitesEnabled;
    }

    public boolean isPmdEnabled()
    {
        return pmdEnabled;
    }

    public boolean isCheckStyleEnabled()
    {
        return checkStyleEnabled;
    }

    public static boolean pathExists(Path path)
    {
        if(path == null)
            return false;
        if(Files.notExists(path))
            return false;
        return true;
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
    String libDir;
    int perTestSuiteTimeout = -1;

    String checkstyleXml;
    String pmdRulesets;

    boolean enableTestSuites = true;
    boolean enableCheckStyle = true;
    boolean enablePMD = true;

    public static ConfigurationJSON loadJSON(Path jsonFile) throws FileNotFoundException
    {
        Gson gson = new Gson();
        Reader jsonReader = new FileReader(jsonFile.toFile());
        return gson.fromJson(jsonReader, ConfigurationJSON.class);
    }
}