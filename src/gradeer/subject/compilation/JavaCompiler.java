package gradeer.subject.compilation;

import gradeer.configuration.Configuration;
import gradeer.execution.AntProcessResult;
import gradeer.execution.AntRunner;
import gradeer.results.io.FileWriter;
import gradeer.subject.ClassPath;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class JavaCompiler
{
    private static Logger logger = LogManager.getLogger(JavaCompiler.class);

    public ClassPath classPath;
    private Configuration configuration;

    public JavaCompiler(ClassPath classPath, Configuration configuration)
    {
        this.classPath = classPath;
        this.configuration = configuration;
    }

    public static JavaCompiler createCompiler(Configuration configuration)
    {
        return createCompiler(configuration, Collections.EMPTY_LIST);
    }

    public static JavaCompiler createCompiler(Configuration configuration, Collection<Path> auxiliaryClassPathElements)
    {
        ClassPath cp = new ClassPath();
        cp.addAll(auxiliaryClassPathElements);

        return new JavaCompiler(cp, configuration);
    }

    public boolean compile(Solution solutionToCompile)
    {
        ClassPath cp = new ClassPath(classPath);
        cp.add(solutionToCompile.getDirectory());

        AntRunner antRunner = new AntRunner(configuration, cp);
        AntProcessResult result = antRunner.compile(solutionToCompile);

        // Report if uncompilable
        if (!result.compiled())
        {
            FileWriter fileWriter = new FileWriter();
            fileWriter.addLine(result.getErrorMessage());

            final Path uncompilableSolutionsDir = Paths.get(configuration.getOutputDir() + File.separator + "uncompilableSolutions");
            uncompilableSolutionsDir.toFile().mkdirs();
            fileWriter.write(Paths.get(uncompilableSolutionsDir + File.separator + solutionToCompile.getIdentifier()));
        }

        return result.compiled();
    }

    public void compileTests(Solution modelSolution)
    {
        ClassPath cp = new ClassPath(classPath);
        cp.add(modelSolution.getDirectory());
        cp.add(configuration.getTestsDir());
        cp.add(configuration.getTestDependenciesDir());

        AntRunner antRunner = new AntRunner(configuration, cp);
        AntProcessResult result = antRunner.compile(configuration.getTestsDir());
        //logger.info(result);
    }

    public void compileDir(Path dir, Solution modelSolution)
    {
        ClassPath cp = new ClassPath(classPath);
        cp.add(dir);
        cp.add(modelSolution.getDirectory());

        AntRunner antRunner = new AntRunner(configuration, cp);
        AntProcessResult result = antRunner.compile(dir);
        //logger.info(result);
    }

}
