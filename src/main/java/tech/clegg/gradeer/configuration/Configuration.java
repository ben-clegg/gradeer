package tech.clegg.gradeer.configuration;

import com.google.gson.Gson;
import tech.clegg.gradeer.timing.TimerService;
import tech.clegg.gradeer.execution.java.ClassExecutionTemplate;
import tech.clegg.gradeer.results.io.LogFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Configuration
{

    private LogFile logFile;
    private TimerService timer;

    private Path rootDir;
    private Path studentSolutionsDir;
    private Path modelSolutionsDir;
    private Path testsDir;
    private boolean autoGenerateTestSuiteChecks = true;

    private Collection<Path> checkJSONs;

    private Path runtimeDependenciesDir;
    private Path testDependenciesDir;
    private Path sourceDependenciesDir;

    private Path testOutputDir;

    private Path libDir;
    private Collection<Path> builtLibComponents;

    private int perTestSuiteTimeout = 10000;

    private Path checkstyleXml;
    private List<ClassExecutionTemplate> preManualJavaClassesToExecute;

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

    private Path mergedSolutionsDir;
    private Path solutionCapturedOutputDir;

    private Path outputDir;

    private String inspectionCommand;

    private boolean skipChecksFailingOnAnyModel = false;
    private boolean forceRecompilation = false;
    private boolean multiThreadingEnabled = true;
    private boolean checkResultRecoveryEnabled = true;

    private Collection<String> requiredClasses = new HashSet<>();

    private Collection<String> includeSolutions = new HashSet<>();
    private Collection<String> excludeSolutions = new HashSet<>();
    private boolean removeInvalidChecks;


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
            // Timer
            timer = new TimerService(Paths.get(outputDir + File.separator + "timer.csv"));
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            System.err.println("Could not load configuration!");
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
        autoGenerateTestSuiteChecks = json.autoGenerateTestSuiteChecks;

        runtimeDependenciesDir = loadLocalOrAbsolutePath(json.runtimeDependenciesDirPath);
        testDependenciesDir = loadLocalOrAbsolutePath(json.testDependenciesDirPath);
        sourceDependenciesDir = loadLocalOrAbsolutePath(json.sourceDependenciesDirPath);

        libDir = loadLocalOrAbsolutePath(json.libDir);

        checkJSONs = new HashSet<>();
        for (String cj : json.checkJSONs)
        {
            checkJSONs.add(loadLocalOrAbsolutePath(cj));
        }

        loadBuiltLibComponents();

        if(json.perTestSuiteTimeout > 0)
            perTestSuiteTimeout = json.perTestSuiteTimeout * 1000; // Convert seconds to ms
        if(json.pmdRulesets != null && json.pmdRulesets.length > 0)
            pmdRulesets = json.pmdRulesets;

        checkstyleXml = loadLocalOrAbsolutePath(json.checkstyleXml);

        outputDir = Paths.get(rootDir + File.separator + "output");
        if(json.outputDirPath != null)
            outputDir = loadLocalOrAbsolutePath(json.outputDirPath);

        testOutputDir = Paths.get(outputDir + File.separator + "testOutput");
        if(json.testOutputDirPath != null)
            testOutputDir = loadLocalOrAbsolutePath(json.testOutputDirPath);

        mergedSolutionsDir = Paths.get(outputDir + File.separator + "mergedSolutions");
        if(json.mergedSolutionsDirPath != null)
            mergedSolutionsDir = loadLocalOrAbsolutePath(json.mergedSolutionsDirPath);

        solutionCapturedOutputDir = Paths.get(outputDir + File.separator + "solutionCapturedOutput");
        if(json.solutionCapturedOutputDirPath != null)
            solutionCapturedOutputDir = loadLocalOrAbsolutePath(json.solutionCapturedOutputDirPath);

        preManualJavaClassesToExecute = new ArrayList<>();
        if(json.preManualJavaClassesToExecute != null && json.preManualJavaClassesToExecute.length > 0)
            preManualJavaClassesToExecute.addAll(Arrays.asList(json.preManualJavaClassesToExecute));

        inspectionCommand = json.inspectionCommand;

        skipChecksFailingOnAnyModel = json.skipChecksFailingOnAnyModel;
        forceRecompilation = json.forceRecompilation;
        multiThreadingEnabled = json.multiThreadingEnabled;
        checkResultRecoveryEnabled = json.checkResultRecoveryEnabled;

        loadRequiredClasses(json);

        includeSolutions.addAll(loadJsonStringArray(json.includeSolutions));
        excludeSolutions.addAll(loadJsonStringArray(json.excludeSolutions));

        removeInvalidChecks = json.removeInvalidChecks;
    }

    private Collection<String> loadJsonStringArray(String[] jsonStringArray)
    {
        if(jsonStringArray == null || jsonStringArray.length < 1)
            return Collections.emptyList();

        return Arrays.asList(jsonStringArray);

    }

    private void loadRequiredClasses(ConfigurationJSON json)
    {
        String[] classStrings = json.requiredClasses;

        if(classStrings == null)
            return;

        requiredClasses.addAll(Arrays.asList(classStrings));
    }

    private void loadBuiltLibComponents()
    {
        builtLibComponents = new HashSet<>();

        if(!pathExists(libDir))
        {
            System.err.println("No built lib components defined or accessible; skipping...");
            return;
        }
        System.out.println("Loading built lib components for " + libDir);
        try
        {
            Files.walk(libDir).forEach(System.out::println);
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
        builtLibComponents.forEach(p -> System.out.println("Loaded built lib component " + p));
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

    public TimerService getTimer()
    {
        return timer;
    }

    public Collection<Path> getCheckJSONs()
    {
        return checkJSONs;
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

    public boolean isAutoGenerateTestSuiteChecks()
    {
        return autoGenerateTestSuiteChecks;
    }

    public Path getTestDependenciesDir()
    {
        return testDependenciesDir;
    }

    public Path getSourceDependenciesDir()
    {
        return sourceDependenciesDir;
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

    public Path getTestOutputDir()
    {
        return testOutputDir;
    }

    public Path getOutputDir()
    {
        return outputDir;
    }

    public Path getMergedSolutionsDir()
    {
        return mergedSolutionsDir;
    }

    public Path getSolutionCapturedOutputDir()
    {
        return solutionCapturedOutputDir;
    }

    public boolean isSkipChecksFailingOnAnyModel()
    {
        return skipChecksFailingOnAnyModel;
    }

    public List<ClassExecutionTemplate> getPreManualJavaClassesToExecute()
    {
        return preManualJavaClassesToExecute;
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

    public boolean isCheckResultRecoveryEnabled()
    {
        return checkResultRecoveryEnabled;
    }

    public Collection<String> getRequiredClasses()
    {
        return requiredClasses;
    }

    public Collection<String> getIncludeSolutions()
    {
        return includeSolutions;
    }

    public Collection<String> getExcludeSolutions()
    {
        return excludeSolutions;
    }

    public boolean isRemoveInvalidChecks()
    {
        return removeInvalidChecks;
    }
}

class ConfigurationJSON
{
    String rootDirPath;
    String studentSolutionsDirPath;
    String modelSolutionsDirPath;
    String testsDirPath;
    boolean autoGenerateTestSuiteChecks = false;
    String runtimeDependenciesDirPath;
    String testDependenciesDirPath;
    String sourceDependenciesDirPath;
    String libDir;
    int perTestSuiteTimeout = -1;
    String outputDirPath;
    String testOutputDirPath;
    String[] checkJSONs;

    String checkstyleXml;
    String[] pmdRulesets;

    String mergedSolutionsDirPath;
    String solutionCapturedOutputDirPath;

    ClassExecutionTemplate[] preManualJavaClassesToExecute;
    String inspectionCommand;

    boolean skipChecksFailingOnAnyModel = false;
    boolean forceRecompilation = false;
    boolean multiThreadingEnabled = true;
    boolean checkResultRecoveryEnabled = true;

    String[] requiredClasses;

    String[] includeSolutions;
    String[] excludeSolutions;
    boolean removeInvalidChecks = true;

    public static ConfigurationJSON loadJSON(Path jsonFile) throws FileNotFoundException
    {
        Gson gson = new Gson();
        Reader jsonReader = new FileReader(jsonFile.toFile());
        return gson.fromJson(jsonReader, ConfigurationJSON.class);
    }
}