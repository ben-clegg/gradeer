package gradeer.io.compilation;

import gradeer.configuration.Configuration;
import gradeer.execution.AntProcessResult;
import gradeer.execution.AntRunner;
import gradeer.io.JavaSource;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class JavaCompiler
{
    private static Logger logger = LogManager.getLogger(JavaCompiler.class);

    public String classPath;

    public JavaCompiler(Collection<Path> classPathElements)
    {
        StringBuilder sb = new StringBuilder();
        Iterator<Path> cpIter = classPathElements.iterator();
        while (cpIter.hasNext())
        {
            sb.append(cpIter.next());
            if(cpIter.hasNext())
                sb.append(File.pathSeparator);
        }
        classPath = sb.toString();
    }

    public static JavaCompiler createCompiler(Path targetRootDir, Solution modelSolution)
    {
        return createCompiler(targetRootDir, modelSolution, Collections.EMPTY_LIST);
    }

    public static JavaCompiler createCompiler(Path targetRootDir, Solution modelSolution, Collection<Path> auxiliaryClassPathElements)
    {
        List<Path> classPathElements = new ArrayList<>();
        classPathElements.add(targetRootDir);
        classPathElements.add(modelSolution.getDirectory());
        classPathElements.addAll(auxiliaryClassPathElements);

        return new JavaCompiler(classPathElements);
    }

    public void compile(JavaSource javaSource, Configuration configuration)
    {
        AntRunner antRunner = new AntRunner(configuration);
        AntProcessResult result = antRunner.compile(javaSource);
        logger.info(result);
    }
}
