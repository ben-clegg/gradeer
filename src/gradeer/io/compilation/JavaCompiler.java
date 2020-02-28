package gradeer.io.compilation;

import gradeer.configuration.Configuration;
import gradeer.execution.AntProcessResult;
import gradeer.execution.AntRunner;
import gradeer.io.ClassPath;
import gradeer.io.JavaSource;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.*;

public class JavaCompiler
{
    private static Logger logger = LogManager.getLogger(JavaCompiler.class);

    public ClassPath classPath;

    public JavaCompiler(ClassPath classPath)
    {
        this.classPath = classPath;
    }

    public static JavaCompiler createCompiler(Solution solutionForCompilation)
    {
        return createCompiler(solutionForCompilation, Collections.EMPTY_LIST);
    }

    public static JavaCompiler createCompiler(Solution solutionForCompilation, Collection<Path> auxiliaryClassPathElements)
    {
        ClassPath cp = new ClassPath();
        cp.add(solutionForCompilation.getDirectory());
        cp.addAll(auxiliaryClassPathElements);

        return new JavaCompiler(cp);
    }

    public void compile(JavaSource javaSource, Configuration configuration)
    {
        AntRunner antRunner = new AntRunner(configuration, classPath);
        AntProcessResult result = antRunner.compile(javaSource);
        logger.info(result);
    }
}
