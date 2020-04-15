package gradeer.auxiliaryprocesses;

import gradeer.configuration.Configuration;
import gradeer.results.io.FileWriter;
import gradeer.solution.Solution;
import gradeer.subject.JavaSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MergedSolutionWriter implements Runnable
{
    private static Logger logger = LogManager.getLogger(MergedSolutionWriter.class);

    private Collection<Solution> solutions;
    private Path outputDir;

    public MergedSolutionWriter(Configuration configuration, Collection<Solution> solutions)
    {
        this.solutions = solutions;

        outputDir = configuration.getMergedSolutionsDir();
        outputDir.toFile().mkdirs();
    }

    public void run()
    {
        for (Solution s : solutions)
            writeMerged(s);
    }

    private void writeMerged(Solution solution)
    {
        FileWriter w = new FileWriter();


        for (JavaSource source : solution.getSources())
        {
            Collection<String> sourceLines = new ArrayList<>();

            sourceLines.add("==============");
            sourceLines.add(source.getJavaFile().getFileName().toString());
            sourceLines.add("==============");
            sourceLines.add("");

            try
            {
                Files.lines(source.getJavaFile(), StandardCharsets.UTF_8).forEach(sourceLines::add);
            } catch (UncheckedIOException uncheckedIOException) {
                IOException e = uncheckedIOException.getCause();
                if(e.getClass().equals(MalformedInputException.class))
                    logger.error("Bad charset for " + source.getJavaFile().toString());
                logger.error(e);

            } catch (IOException e)
            {
                logger.error(e);
            }

            sourceLines.add("");

            w.addLines(sourceLines);
        }

        w.write(Paths.get(outputDir + File.separator + solution.getIdentifier() + ".java"));
        logger.info("Wrote merged solution for " + solution.getIdentifier());
    }
}
