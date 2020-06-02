package tech.clegg.gradeer.execution.staticanalysis.pmd;

        import tech.clegg.gradeer.configuration.Configuration;
        import tech.clegg.gradeer.solution.Solution;
        import org.apache.logging.log4j.LogManager;
        import org.apache.logging.log4j.Logger;
        import tech.clegg.gradeer.subject.ClassPath;

        import java.io.File;
        import java.io.IOException;
        import java.nio.file.Files;
        import java.nio.file.Path;
        import java.nio.file.Paths;
        import java.util.*;

public class PMDProcess implements Runnable
{
    private static final Logger logger = LogManager.getLogger(PMDProcess.class);

    private final Solution solution;
    private final String ruleSetNames;
    private final Configuration configuration;

    private PMDProcessResults results;

    public PMDProcess(Solution solution, List<String> ruleSetNames, Configuration configuration)
    {
        this.solution = solution;
        this.configuration = configuration;

        StringBuilder sb = new StringBuilder();
        Iterator<String> i = ruleSetNames.iterator();
        while (i.hasNext())
        {
            sb.append(i.next());
            if(i.hasNext())
                sb.append(",");
        }
        this.ruleSetNames = sb.toString();
    }

    public void run()
    {
        ClassPath cp = new ClassPath();
        try
        {
            Files.list(Paths.get(configuration.getPmdLocation().toAbsolutePath().toString() +
                    File.separator + "lib"))
                    .forEach(cp::add);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // Load PMD
        List<String> command = new ArrayList<>();
        command.add("java");

        // Classpath elements...
        command.add("-cp");
        command.add(cp.toString());
        command.add("net.sourceforge.pmd.PMD"); // PMD target class

        // PMD args
        // TODO load additional classpath elements with -auxclasspath
        command.add("-d");  command.add(solution.getDirectory().toString());
        command.add("-R");  command.add(ruleSetNames);
        command.add("-f");  command.add("csv");

        // Run process
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        results = new PMDProcessResults(processBuilder);
    }

    public PMDProcessResults getResults()
    {
        return results;
    }
}
