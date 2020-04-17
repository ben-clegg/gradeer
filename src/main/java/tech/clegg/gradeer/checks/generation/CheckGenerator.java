package tech.clegg.gradeer.checks.generation;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.results.io.FileWriter;
import tech.clegg.gradeer.solution.Solution;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;

public abstract class CheckGenerator
{
    private Configuration configuration;
    private Collection<Solution> modelSolutions;
    private Collection<Check> checks;

    public CheckGenerator(Configuration configuration, Collection<Solution> modelSolutions)
    {
        this.configuration = configuration;
        this.modelSolutions = modelSolutions;

        this.checks = new HashSet<>();
        generate();
    }

    protected void addCheck(Check check)
    {
        // Only add checks with a non-zero weight (0 weight checks are disabled)
        if(check.getWeight() != 0)
            checks.add(check);
    }

    abstract void generate();

    protected Configuration getConfiguration()
    {
        return configuration;
    }

    protected Collection<Solution> getModelSolutions()
    {
        return modelSolutions;
    }

    public Collection<Check> getChecks()
    {
        return checks;
    }

    protected void reportRemovedChecks(Collection<Check> toRemove, String generatorType)
    {
        FileWriter fileWriter = new FileWriter();
        for (Check c : toRemove)
            fileWriter.addLine(c.toString());

        final Path removedChecksDir = Paths.get(configuration.getOutputDir() + File.separator + "removedChecks");

        if(Files.notExists(removedChecksDir))
            removedChecksDir.toFile().mkdirs();
        fileWriter.write(Paths.get(removedChecksDir + File.separator + generatorType));

    }
}
