package tech.anonymousname.gradeer.results;

import tech.anonymousname.gradeer.configuration.Configuration;
import tech.anonymousname.gradeer.results.io.FileWriter;
import tech.anonymousname.gradeer.solution.Flag;
import tech.anonymousname.gradeer.solution.Solution;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

public class SolutionFlagWriter
{
    private Configuration configuration;

    public SolutionFlagWriter(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void write(Collection<Solution> solutions)
    {
        FileWriter f = new FileWriter();

        // Flagless solutions
        f.addLine("Unflagged");
        f.addLine(solutionsToJsonStringArray(
                solutions.stream().filter(s -> s.getFlags().isEmpty()).collect(Collectors.toSet())
        ));
        f.addLine("");

        // Solutions with each flag
        for (Flag flag : Flag.values())
        {
            f.addLine(flag.name());
            f.addLine(solutionsToJsonStringArray(
                    solutions.stream().filter(s -> s.getFlags().contains(flag)).collect(Collectors.toSet())
            ));
            f.addLine("");

        }

        // Write to location
        Path loc = Paths.get(configuration.getOutputDir() + File.separator + "flaggedSolutions.txt");
        f.write(loc);
    }

    private String solutionsToJsonStringArray(Collection<Solution> solutions)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        Iterator<Solution> iter = solutions.iterator();
        while (iter.hasNext())
        {
            sb.append("\"");
            sb.append(iter.next().getIdentifier());
            sb.append("\"");
            if(iter.hasNext())
                sb.append(",");
        }

        sb.append("]");

        return sb.toString();
    }
}
