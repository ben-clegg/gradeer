package gradeer.execution.staticanalysis.pmd;

        import gradeer.configuration.Configuration;
        import gradeer.solution.Solution;
        import org.apache.logging.log4j.LogManager;
        import org.apache.logging.log4j.Logger;

        import java.io.File;
        import java.io.IOException;
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
        List<String> command = new ArrayList<>();

        // Load PMD
        command.add("java");
        command.add("-cp"); // Classpath elements...
        command.add(configuration.getPmdLocation().toAbsolutePath().toString()
                + File.separator + "lib" + File.separator + "*"); // PMD .jars
        command.add("net.sourceforge.pmd.PMD"); // PMD target class

        // PMD args
        // TODO load additional classpath elements with -auxclasspath
        command.add("-d");  command.add(solution.getDirectory().toString());
        command.add("-R");  command.add(ruleSetNames);
        command.add("-f");  command.add("csv");

        // Run process
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        try
        {
            Process p = processBuilder.start();
            results = new PMDProcessResults(p);
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
        }
    }

    public PMDProcessResults getResults()
    {
        return results;
    }
}
