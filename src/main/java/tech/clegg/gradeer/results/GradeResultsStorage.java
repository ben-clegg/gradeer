package tech.clegg.gradeer.results;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

public class GradeResultsStorage
{
    private Configuration configuration;

    public GradeResultsStorage(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Store a Solution's CheckResults as a JSON file
     * @param solution the Solution to store the CheckResults for
     */
    public void store(Solution solution)
    {
        Collection<CheckResult> checkResults = solution.getAllCheckResults();

        // Create CheckResultEntry array (from feedback, unweighted score and Check's identifier)
        CheckResultEntry[] entries = checkResults.stream()
                .map(CheckResultEntry::new)
                .collect(Collectors.toList())
                .toArray(new CheckResultEntry[]{});

        // Store CheckResultEntry array as JSON
        try
        {
            // Make storage dir
            Path dir = configuration.getSolutionCheckResultsStoragePath();
            if(Files.notExists(dir))
                Files.createDirectories(dir);
            // Create Path for output file
            Path solutionOutput = Paths.get(dir + File.separator + solution.getIdentifier() + ".json");
            // Store entries as JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter w = new FileWriter(solutionOutput.toFile());
            gson.toJson(entries, w);
            w.flush();
            w.close();
            System.out.println("Stored CheckResult entries as JSON for Solution " + solution.getIdentifier());

        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            System.err.println("Failed to store check results for Solution " + solution.getIdentifier());
        }

    }
}

/**
 * Single entry representing a CheckResult to store in / read from JSON array.
 */
class CheckResultEntry
{
    double unweightedScore;
    String feedback;
    String checkIdentifier;

    CheckResultEntry(CheckResult checkResult)
    {
        this.unweightedScore = checkResult.getUnweightedScore();
        this.feedback = checkResult.getFeedback();
        this.checkIdentifier = checkResult.getCheck().identifier();
    }

}
