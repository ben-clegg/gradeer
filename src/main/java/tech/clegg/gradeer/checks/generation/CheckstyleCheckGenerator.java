package tech.clegg.gradeer.checks.generation;

import com.google.gson.Gson;
import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.CheckstyleCheck;
import tech.clegg.gradeer.checks.generation.json.CheckJSONEntry;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.staticanalysis.checkstyle.CheckstyleExecutor;
import tech.clegg.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CheckstyleCheckGenerator extends CheckGenerator
{
    private static Logger logger = LogManager.getLogger(CheckstyleCheckGenerator.class);

    public CheckstyleCheckGenerator(Configuration configuration, Collection<Solution> modelSolutions)
    {
        super(configuration, modelSolutions);
    }

    @Override
    void generate()
    {
        // Load module names / IDs of checks, and according parameters
        Gson gson = new Gson();
        try
        {
            CheckJSONEntry[] checkJSONEntries =
                    gson.fromJson(new FileReader(getConfiguration().getCheckstyleChecksJSON().toFile()),
                            CheckJSONEntry[].class);
            // Generate checks
            for (CheckJSONEntry j : checkJSONEntries)
            {
                CheckstyleCheck checkstyleCheck = new CheckstyleCheck(j);
                addCheck(checkstyleCheck);
                logger.info("Added check " + checkstyleCheck);
            }
        } catch (FileNotFoundException e)
        {
            logger.error(e);
        }

        // TODO Load per-javafile grace settings, and per-solution grace settings

        // Execute on model solutions, setting results for each solution
        CheckstyleExecutor checkstyleExecutor = new CheckstyleExecutor(getConfiguration(), (Collection) getChecks());
        getModelSolutions().forEach(checkstyleExecutor::execute);
        for (Check c : getChecks())
            getModelSolutions().forEach(c::run);

        // Remove any checks that fail on model solutions and report them
        if(getConfiguration().isRemoveCheckstyleFailuresOnModel())
        {
            for (Solution m : getModelSolutions())
            {
                List<Check> toRemove = getChecks().stream()
                        .filter(c -> c.getUnweightedScore(m) == 0.0)
                        .map(c -> (CheckstyleCheck) c)
                        .collect(Collectors.toList());
                reportRemovedChecks(toRemove, this.getClass().getName());
                getChecks().removeAll(toRemove);
            }
            // TODO log removed checks
        }
    }
}
