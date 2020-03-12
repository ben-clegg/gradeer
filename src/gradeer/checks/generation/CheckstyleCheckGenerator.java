package gradeer.checks.generation;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import gradeer.checks.CheckstyleCheck;
import gradeer.checks.exceptions.NoCheckException;
import gradeer.configuration.Configuration;
import gradeer.execution.checkstyle.CheckstyleExecutor;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
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
        // TODO Load module names and IDs from config
        Gson gson = new Gson();

        try
        {
            CheckstyleCheckJSONEntry[] checkJSONEntries =
                    gson.fromJson(new FileReader(getConfiguration().getCheckstyleChecksJSON().toFile()),
                            CheckstyleCheckJSONEntry[].class);
            for (CheckstyleCheckJSONEntry j : checkJSONEntries)
            {
                CheckstyleCheck checkstyleCheck = new CheckstyleCheck(j.name, j.feedback, j.weight);
                addCheck(checkstyleCheck);
                logger.info("Added check " + checkstyleCheck);
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        // TODO Load according weights, per-javafile grace settings, and per-solution grace settings
        // TODO Make and add check for each module name

        // Execute on model solutions
        CheckstyleExecutor checkstyleExecutor = new CheckstyleExecutor(getConfiguration(), (Collection) getChecks());
        getModelSolutions().forEach(checkstyleExecutor::execute);
        // TODO remove any checks that fail on model solutions and report them
        for (Solution m : getModelSolutions())
        {

        }
    }

    @Override
    void setWeights()
    {

    }

}

class CheckstyleCheckJSONEntry
{
    String name;
    String feedback;
    double weight;
}
