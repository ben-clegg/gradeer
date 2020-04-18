package tech.clegg.gradeer.checks.generation;

import com.google.gson.Gson;
import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.PMDCheck;
import tech.clegg.gradeer.checks.generation.json.CheckJSONEntry;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.staticanalysis.pmd.PMDExecutor;
import tech.clegg.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PMDCheckGenerator extends CheckGenerator
{
    private static Logger logger = LogManager.getLogger(PMDCheckGenerator.class);

    public PMDCheckGenerator(Configuration configuration, Collection<Solution> modelSolutions)
    {
        super(configuration, modelSolutions);
    }

    @Override
    void generate()
    {
        // Load checks from config
        Gson gson = new Gson();
        try
        {
            CheckJSONEntry[] checkJSONEntries =
                    gson.fromJson(new FileReader(getConfiguration().getPmdChecksJSON().toFile()),
                            CheckJSONEntry[].class);
            // Generate checks
            for (CheckJSONEntry j : checkJSONEntries)
            {
                PMDCheck pmdCheck = new PMDCheck(j);
                addCheck(pmdCheck);
                logger.info("Added check " + pmdCheck);
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        // TODO Load per-javafile grace settings, and per-solution grace settings

        // Execute on model solutions
        PMDExecutor pmdExecutor = new PMDExecutor(getConfiguration());
        getModelSolutions().forEach(pmdExecutor::execute);
        for (Check c : getChecks())
            getModelSolutions().forEach(c::run);

        // Remove failures on model solution
        if(getConfiguration().isRemovePmdFailuresOnModel())
        {
            for (Solution m : getModelSolutions())
            {
                List<Check> toRemove = getChecks().stream()
                        .filter(c -> c.getUnweightedScore(m) == 0.0)
                        .map(c -> (PMDCheck) c)
                        .collect(Collectors.toList());
                reportRemovedChecks(toRemove, this.getClass().getName());
                getChecks().removeAll(toRemove);
            }
            // TODO log removed checks
        }

    }
}