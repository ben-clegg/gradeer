package tech.clegg.gradeer.checks.generation;

import com.google.gson.Gson;
import tech.clegg.gradeer.checks.ManualCheck;
import tech.clegg.gradeer.checks.generation.json.ManualCheckJSONEntry;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;

public class ManualCheckGenerator extends CheckGenerator
{
    private static Logger logger = LogManager.getLogger(ManualCheckGenerator.class);

    public ManualCheckGenerator(Configuration configuration, Collection<Solution> modelSolutions)
    {
        super(configuration, modelSolutions);
    }

    @Override
    void generate()
    {
        Gson gson = new Gson();
        try
        {
            // Load JSON
            ManualCheckJSONEntry[] checkJSONEntries = gson.fromJson(new FileReader(
                    getConfiguration().getManualChecksJSON().toFile()
            ), ManualCheckJSONEntry[].class);

            // Generate Checks
            for (ManualCheckJSONEntry jsonEntry : checkJSONEntries)
            {
                ManualCheck check = new ManualCheck(jsonEntry);
                addCheck(check);
                logger.info("Added check " + check);
            }

        } catch (FileNotFoundException e)
        {
            logger.error(e);
        }
    }
}
