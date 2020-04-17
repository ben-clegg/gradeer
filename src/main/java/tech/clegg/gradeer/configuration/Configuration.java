package tech.clegg.gradeer.configuration;

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

    private Path runtimeDependenciesDir;
    private Path testDependenciesDir;
    private Path sourceDependenciesDir;

    private Path unittestChecksJSON;
    private Path testOutputDir;

    private Path libDir;
    private Collection<Path> builtLibComponents;

    private int perTestSuiteTimeout = 10000;

    private Path checkstyleXml;
    private Path checkstyleChecksJSON;
    private boolean removeCheckstyleFailuresOnModel;

    private boolean testSuitesEnabled = true;
    private boolean pmdEnabled = true;
    private boolean checkstyleEnabled = true;

    private Path pmdLocation;
    private String pmdRulesets = "category/java/bestpractices.xml";
    private Path pmdChecksJSON;
    private boolean removePmdFailuresOnModel;

    private Path mergedSolutionsDir;

    private Path outputDir;
    private Path checkResultsDir;

    private Path manualChecksJSON;


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

        runtimeDependenciesDir = loadLocalOrAbsolutePath(json.runtimeDependenciesDirPath);
        testDependenciesDir = loadLocalOrAbsolutePath(json.testDependenciesDirPath);
        sourceDependenciesDir = loadLocalOrAbsolutePath(json.sourceDependenciesDirPath);

        libDir = loadLocalOrAbsolutePath(json.libDir);

        unittestChecksJSON = loadLocalOrAbsolutePath(json.unittestChecksJSON);
        testOutputDir = loadLocalOrAbsolutePath(json.testOutputDirPath);

        loadBuiltLibComponents();

        if(json.perTestSuiteTimeout > 0)
            perTestSuiteTimeout = json.perTestSuiteTimeout * 1000; // Convert seconds to ms
        if(json.pmdRulesets != null && !json.pmdRulesets.isEmpty())
            pmdRulesets = json.pmdRulesets;

        checkstyleXml = loadLocalOrAbsolutePath(json.checkstyleXml);
        checkstyleChecksJSON = loadLocalOrAbsolutePath(json.checkstyleChecksJSON);
        removeCheckstyleFailuresOnModel = json.removeCheckstyleFailuresOnModel;

        testSuitesEnabled = json.enableTestSuites;
        checkstyleEnabled = json.enableCheckStyle;
        pmdEnabled = json.enablePMD;
        removePmdFailuresOnModel = json.removePMDFailuresOnModel;

        pmdChecksJSON = loadLocalOrAbsolutePath(json.pmdChecksJSON);
        pmdLocation = loadLocalOrAbsolutePath(json.pmdLocation);

        outputDir = Paths.get(rootDir + File.separator + "output");

        mergedSolutionsDir = loadLocalOrAbsolutePath(json.mergedSolutionsDirPath);
        checkResultsDir = loadLocalOrAbsolutePath(json.checkResultsDirPath);

        manualChecksJSON = loadLocalOrAbsolutePath(json.manualChecksJSON);
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

    public Path getSourceDependenciesDir()
    {
        return sourceDependenciesDir;
    }

    public Path getUnittestChecksJSON()
    {
        return unittestChecksJSON;
    }

    public Path getRuntimeDependenciesDir()
    {
        return runtimeDependenciesDir;
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

    public Path getCheckstyleChecksJSON()
    {
        return checkstyleChecksJSON;
    }

    public Path getPmdChecksJSON()
    {
        return pmdChecksJSON;
    }

    public boolean isRemoveCheckstyleFailuresOnModel()
    {
        return removeCheckstyleFailuresOnModel;
    }

    public boolean isRemovePmdFailuresOnModel()
    {
        return removePmdFailuresOnModel;
    }

    public void setRemoveCheckstyleFailuresOnModel(boolean removeCheckstyleFailuresOnModel)
    {
        this.removeCheckstyleFailuresOnModel = removeCheckstyleFailuresOnModel;
    }

    public boolean isTestSuitesEnabled()
    {
        return testSuitesEnabled;
    }

    public Path getTestOutputDir()
    {
        return testOutputDir;
    }

    public boolean isPmdEnabled()
    {
        return pmdEnabled;
    }

    public boolean isCheckstyleEnabled()
    {
        return checkstyleEnabled;
    }

    public Path getPmdLocation()
    {
        return pmdLocation;
    }

    public Path getOutputDir()
    {
        return outputDir;
    }

    public Path getMergedSolutionsDir()
    {
        return mergedSolutionsDir;
    }

    public Path getCheckResultsDir()
    {
        return checkResultsDir;
    }

    public Path getManualChecksJSON()
    {
        return manualChecksJSON;
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
    String runtimeDependenciesDirPath;
    String testDependenciesDirPath;
    String sourceDependenciesDirPath;
    String libDir;
    int perTestSuiteTimeout = -1;
    String unittestChecksJSON;
    String testOutputDirPath;

    String checkstyleXml;
    String checkstyleChecksJSON;
    boolean removeCheckstyleFailuresOnModel = false;
    String pmdRulesets;

    boolean enableTestSuites = true;
    boolean enableCheckStyle = true;
    boolean enablePMD = true;

    String pmdLocation;
    String pmdChecksJSON;
    boolean removePMDFailuresOnModel = false;

    String checkResultsDirPath;

    String mergedSolutionsDirPath;

    String manualChecksJSON;

    public static ConfigurationJSON loadJSON(Path jsonFile) throws FileNotFoundException
    {
        Gson gson = new Gson();
        Reader jsonReader = new FileReader(jsonFile.toFile());
        return gson.fromJson(jsonReader, ConfigurationJSON.class);
    }
}