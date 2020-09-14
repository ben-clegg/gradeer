package tech.clegg.gradeer.configuration;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.clegg.gradeer.execution.java.ClassExecutionTemplate;
import tech.clegg.gradeer.results.io.LogFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
    private int tabWidth;

    private boolean removeCheckstyleFailuresOnModel;

    private boolean testSuitesEnabled = true;
    private boolean pmdEnabled = true;
    private boolean checkstyleEnabled = true;

    private Path pmdLocation;
    private String[] pmdRulesets = {
            "category/java/bestpractices.xml",
            "category/java/codestyle.xml",
            "category/java/design.xml",
            "category/java/documentation.xml",
            "category/java/errorprone.xml",
            "category/java/multithreading.xml",
            "category/java/performance.xml",
            "category/java/security.xml"
    };
    private Path pmdChecksJSON;
    private boolean removePmdFailuresOnModel;

    private Path mergedSolutionsDir;

    private Path outputDir;
    private Path checkResultsDir;

    private Path manualChecksJSON;
    private List<ClassExecutionTemplate> preManualJavaClassesToExecute;

    private String inspectionCommand;

    private boolean skipChecksFailingOnAnyModel = false;
    private boolean forceRecompilation = false;
    private boolean multiThreadingEnabled = true;

    private LogFile logFile;


    public Configuration(Path jsonFile)
    {
        try
        {
            // Load json file's parent dir as default root dir
            rootDir = jsonFile.getParent();
            // Load config variables from json
            loadFromJSON(ConfigurationJSON.loadJSON(jsonFile));

            // Setup log file
            logFile = new LogFile(Paths.get(outputDir + File.separator + "logOutput.log"));
        }
        catch (IOException ioEx)
        {
            logger.error(ioEx);
            logger.error("Could not load configuration!");
            System.exit(1);
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

        loadBuiltLibComponents();

        if(json.perTestSuiteTimeout > 0)
            perTestSuiteTimeout = json.perTestSuiteTimeout * 1000; // Convert seconds to ms
        if(json.pmdRulesets != null && json.pmdRulesets.length > 0)
            pmdRulesets = json.pmdRulesets;

        checkstyleXml = loadLocalOrAbsolutePath(json.checkstyleXml);
        checkstyleChecksJSON = loadLocalOrAbsolutePath(json.checkstyleChecksJSON);
        tabWidth = json.tabWidth;
        removeCheckstyleFailuresOnModel = json.removeCheckstyleFailuresOnModel;

        testSuitesEnabled = json.enableTestSuites;
        checkstyleEnabled = json.enableCheckStyle;
        pmdEnabled = json.enablePMD;
        removePmdFailuresOnModel = json.removePMDFailuresOnModel;

        pmdChecksJSON = loadLocalOrAbsolutePath(json.pmdChecksJSON);
        pmdLocation = loadLocalOrAbsolutePath(json.pmdLocation);

        outputDir = Paths.get(rootDir + File.separator + "output");
        if(json.outputDirPath != null)
            outputDir = loadLocalOrAbsolutePath(json.outputDirPath);

        testOutputDir = Paths.get(outputDir + File.separator + "testOutput");
        if(json.testOutputDirPath != null)
            testOutputDir = loadLocalOrAbsolutePath(json.testOutputDirPath);

        checkResultsDir = Paths.get(outputDir + File.separator + "checkResults");
        if(json.checkResultsDirPath != null)
            checkResultsDir = loadLocalOrAbsolutePath(json.checkResultsDirPath);

        mergedSolutionsDir = Paths.get(outputDir + File.separator + "mergedSolutions");
        if(json.mergedSolutionsDirPath != null)
            mergedSolutionsDir = loadLocalOrAbsolutePath(json.mergedSolutionsDirPath);

        manualChecksJSON = loadLocalOrAbsolutePath(json.manualChecksJSON);
        preManualJavaClassesToExecute = new ArrayList<>();
        if(json.preManualJavaClassesToExecute != null && json.preManualJavaClassesToExecute.length > 0)
            preManualJavaClassesToExecute.addAll(Arrays.asList(json.preManualJavaClassesToExecute));

        inspectionCommand = json.inspectionCommand;

        skipChecksFailingOnAnyModel = json.skipChecksFailingOnAnyModel;
        forceRecompilation = json.forceRecompilation;
        multiThreadingEnabled = json.multiThreadingEnabled;
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

    public Path getRootDir()
    {
        return rootDir;
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

    public String[] getPmdRulesets()
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

    public int getTabWidth()
    {
        return tabWidth;
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

    public List<ClassExecutionTemplate> getPreManualJavaClassesToExecute()
    {
        return preManualJavaClassesToExecute;
    }

    public boolean isSkipChecksFailingOnAnyModel()
    {
        return skipChecksFailingOnAnyModel;
    }

    public String getInspectionCommand()
    {
        return inspectionCommand;
    }

    public LogFile getLogFile()
    {
        return logFile;
    }

    public static boolean pathExists(Path path)
    {
        if(path == null)
            return false;
        if(Files.notExists(path))
            return false;
        return true;
    }

    public boolean isForceRecompilation()
    {
        return forceRecompilation;
    }

    public boolean isMultiThreadingEnabled() {
        return multiThreadingEnabled;
    }

    public Path getSolutionCheckResultsStoragePath()
    {
        return Paths.get(getOutputDir() + File.separator + "perSolutionCheckResults");
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
    String outputDirPath;
    String testOutputDirPath;

    String checkstyleXml;
    String checkstyleChecksJSON;
    int tabWidth = 2;
    boolean removeCheckstyleFailuresOnModel = false;
    String[] pmdRulesets;

    boolean enableTestSuites = true;
    boolean enableCheckStyle = true;
    boolean enablePMD = true;

    String pmdLocation;
    String pmdChecksJSON;
    boolean removePMDFailuresOnModel = false;

    String checkResultsDirPath;

    String mergedSolutionsDirPath;

    String manualChecksJSON;
    ClassExecutionTemplate[] preManualJavaClassesToExecute;

    String inspectionCommand;

    boolean skipChecksFailingOnAnyModel = false;
    boolean forceRecompilation = false;
    boolean multiThreadingEnabled = true;

    public static ConfigurationJSON loadJSON(Path jsonFile) throws FileNotFoundException
    {
        Gson gson = new Gson();
        Reader jsonReader = new FileReader(jsonFile.toFile());
        return gson.fromJson(jsonReader, ConfigurationJSON.class);
    }
}