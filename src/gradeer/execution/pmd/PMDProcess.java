package gradeer.execution.pmd;

        import gradeer.configuration.Configuration;
        import gradeer.solution.Solution;
        import gradeer.subject.JavaSource;
        import net.sourceforge.pmd.*;
        import net.sourceforge.pmd.processor.PmdRunnable;
        import net.sourceforge.pmd.renderers.CSVRenderer;
        import net.sourceforge.pmd.renderers.Renderer;
        import net.sourceforge.pmd.renderers.XMLRenderer;
        import net.sourceforge.pmd.stat.Metric;
        import net.sourceforge.pmd.util.ClasspathClassLoader;
        import net.sourceforge.pmd.util.ResourceLoader;
        import net.sourceforge.pmd.util.datasource.DataSource;
        import net.sourceforge.pmd.util.datasource.FileDataSource;
        import org.apache.logging.log4j.LogManager;
        import org.apache.logging.log4j.Logger;

        import java.io.File;
        import java.io.IOException;
        import java.io.StringWriter;
        import java.nio.file.Path;
        import java.util.*;
        import java.util.stream.Collectors;

public class PMDProcess implements Runnable
{
    private static final Logger logger = LogManager.getLogger(PMDProcess.class);

    private final Solution solution;
    private final String ruleSetNames;
    private final Configuration configuration;

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
            PMDProcessResults res = new PMDProcessResults(p);
            logger.info(res.getPmdViolations().toString());
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
        }
    }

    @Deprecated
    public void runWithAPI()
    {
        /*
        NOTE: The current version of PMD has a severe conflict with checkstyle due to using a very old version of Saxon
        As such, this method is deprecated, but may be fixed in the future.
         */
        logger.info("Running PMD on Solution" + solution.getIdentifier());

        // https://pmd.github.io/pmd/pmd_userdocs_tools_java_api.html
        PMDConfiguration conf = new PMDConfiguration();
        conf.setInputPaths(solution.getDirectory().toAbsolutePath().toString());
        conf.setRuleSets(ruleSetNames);
        //conf.setRuleSets("rulesets/java/quickstart.xml");
        conf.setMinimumPriority(RulePriority.LOW);
        conf.setReportFormat("xml");

        // TODO implement classpath priors; needed for type resolution - load from Config
        /*
        try
        {
            conf.prependClasspath(solution.getDirectory().toAbsolutePath().toString());
            conf.prependClasspath(configuration.getRuntimeDependenciesDirPath().toAbsolutePath().toString());
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            logger.error("Could not add entry to classpath, PMD execution may encounter issues.");
        }

         */

        StringWriter renderOutput = new StringWriter();
        //CSVRenderer renderer = new CSVRenderer();
        //XMLRenderer renderer = new XMLRenderer();
        Renderer renderer = conf.createRenderer();
        renderer.setWriter(renderOutput);
        try
        {
            renderer.start();
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
        }

        List<DataSource> dataSources = Arrays.asList(new FileDataSource(solution.getDirectory().toFile()));
        logger.info(dataSources);

        //PMD.doPMD(conf);

        for (JavaSource j : solution.getSources())
        {
            RuleContext ruleContext = new RuleContext();
            ruleContext.setSourceCodeFile(j.getJavaFile().toFile());

            RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(conf, new ResourceLoader());
            try
            {
                RuleSets ruleSets = ruleSetFactory.createRuleSets(ruleSetNames);

                SourceCodeProcessor sourceCodeProcessor = new SourceCodeProcessor(conf);

                DataSource d = new FileDataSource(j.getJavaFile().toFile());
                Report report = new PmdRunnable(d, j.getJavaFile().getFileName().toString(), Arrays.asList(renderer), ruleContext, ruleSets, sourceCodeProcessor)
                        .call();

                logger.info(report.getSummary());
            }
            catch (RuleSetNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        //PMD.processFiles(conf, rsf, dataSources, context, Collections.singletonList(renderer));

        try
        {
            renderer.end();
            renderer.flush();
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
        }

        logger.info(renderOutput.toString());
    }

    private static ThreadSafeReportListener createReportListener() {
        return new ThreadSafeReportListener() {
            @Override
            public void ruleViolationAdded(RuleViolation ruleViolation) {
                System.out.printf("%-20s:%d %s%n", ruleViolation.getFilename(),
                        ruleViolation.getBeginLine(), ruleViolation.getDescription());
            }

            @Override
            public void metricAdded(Metric metric) {}
        };
    }


}
