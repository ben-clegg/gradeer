package tech.anonymousname.gradeer.subject.compilation;

import tech.anonymousname.gradeer.solution.Flag;
import tech.anonymousname.gradeer.subject.ClassPath;
import tech.anonymousname.gradeer.configuration.Configuration;
import tech.anonymousname.gradeer.execution.AntProcessResult;
import tech.anonymousname.gradeer.execution.AntRunner;
import tech.anonymousname.gradeer.results.io.FileWriter;
import tech.anonymousname.gradeer.solution.Solution;
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
        return createCompiler(configuration, new ArrayList<>());
    }

    public static JavaCompiler createCompiler(Configuration configuration, Collection<Path> auxiliaryClassPathElements)
    {
        ClassPath cp = new ClassPath();
        cp.addAll(auxiliaryClassPathElements);

        return new JavaCompiler(cp, configuration);
    }

    /**
     * Compile a Solution
     * @param solutionToCompile the Solution to compile
     * @return true if the Solution compiled (or was already compiled), false if it did not
     */
    public boolean compile(Solution solutionToCompile)
    {
        if(!configuration.isForceRecompilation())
        {
            if(solutionToCompile.isCompiled())
                return true;
        }

        ClassPath cp = new ClassPath(classPath);
        cp.add(solutionToCompile.getDirectory());

        System.out.println("Compiling solution " + solutionToCompile.getIdentifier());
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

            // Set flag for Solution
            solutionToCompile.addFlag(Flag.UNCOMPILABLE);
        }

        return result.compiled();
    }

    public void compileTests(Solution modelSolution)
    {
        ClassPath cp = new ClassPath(classPath);
        cp.add(modelSolution.getDirectory());
        cp.add(configuration.getTestsDir());
        cp.add(configuration.getSourceDependenciesDir());
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
