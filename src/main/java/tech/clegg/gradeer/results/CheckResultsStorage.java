package tech.clegg.gradeer.results;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.checkprocessing.CheckProcessor;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;

import javax.annotation.CheckForNull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class CheckResultsStorage
{
    private Configuration configuration;

    public CheckResultsStorage(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Store a Solution's CheckResults as a JSON file
     * @param solution the Solution to store the CheckResults for
     */
    public void storeCheckResults(Solution solution)
    {

        // Load current execution's CheckResults & their associated Checks
        Collection<CheckResult> newCheckResults = solution.getAllCheckResults();
        Collection<Check> newCheckResultsChecks = newCheckResults.stream()
                .map(CheckResult::getCheck)
                .collect(Collectors.toList());

        // Load existing stored CheckResultEntries that do NOT share Checks with the new CheckResults
        Collection<CheckResultEntry> checkResultEntries = Arrays.stream(loadExistingCheckResultEntries(solution))
                .filter(e -> e.findMatchingCheck(newCheckResultsChecks) == null)
                .collect(Collectors.toList());

        // Convert new CheckResults to CheckResultEntries & add to checkResultEntries
        checkResultEntries.addAll(newCheckResults.stream()
                .map(CheckResultEntry::new)
                .collect(Collectors.toList())
        );

        // Convert to array
        CheckResultEntry[] entries = checkResultEntries.toArray(new CheckResultEntry[]{});

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

    /**
     * Recover all CheckResults from a Solution's JSON file.
     * This solutions' missing CheckResults are populated from this file, where available.
     * @param solution the Solution to recover check results for.
     */
    public void recoverCheckResults(Solution solution, Collection<CheckProcessor> checkProcessors)
    {
        CheckResultEntry[] entries = loadExistingCheckResultEntries(solution);
        // Match each check to each available CheckResultEntry; construct CheckResults
        Collection<CheckResult> checkResults = matchedCheckResults(getAllChecks(checkProcessors), entries);

        // Populate Solution with matched CheckResults; don't overwrite existing entries
        if(!checkResults.isEmpty())
        {
            solution.addAllCheckResults(checkResults, false);
            System.out.println("Sucessfully recovered " + checkResults.size() +
                    " Check results for Solution " + solution.getIdentifier());
        }

    }

    private CheckResultEntry[] loadExistingCheckResultEntries(Solution solution)
    {
        // Read JSON file
        Path jsonPath = Paths.get(configuration.getSolutionCheckResultsStoragePath() + File.separator +
                solution.getIdentifier() + ".json");
        // Skip if no file
        if(Files.notExists(jsonPath))
        {
            System.out.println("No check results JSON file present for Solution " + solution.getIdentifier() +
                    "; skipping recovery...");
            return new CheckResultEntry[]{};
        }

        // File exists; attempt load
        try
        {
            Gson gson = new Gson();
            Reader jsonReader = new FileReader(jsonPath.toFile());

            return gson.fromJson(jsonReader, CheckResultEntry[].class);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return new CheckResultEntry[]{};
        }
    }

    private Collection<CheckResult> matchedCheckResults(Collection<Check> checksToMatch,
                                                        CheckResultEntry[] checkResultEntries)
    {
        Collection<CheckResult> checkResults = new HashSet<>();
        Collection<Check> unmatched = new HashSet<>(checksToMatch);

        for (CheckResultEntry entry : checkResultEntries)
        {
            Check matchedCheck = entry.findMatchingCheck(unmatched);
            if(matchedCheck != null)
            {
                checkResults.add(entry.toCheckResult(matchedCheck));
                unmatched.remove(matchedCheck);
            }
            else
            {
                System.err.println("No matching check for CheckResultEntry " + entry.toString());
            }
        }

        return checkResults;
    }

    private Collection<Check> getAllChecks(Collection<CheckProcessor> checkProcessors)
    {
        Collection<Check> checks = new HashSet<>();
        checkProcessors.forEach(cp -> checks.addAll(cp.getAllChecks()));
        return checks;
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

    /**
     * Convert this CheckResultEntry to a CheckResult
     * @param matchingCheck the Check that matches the checkIdentifier
     * @return the converted CheckResult
     */
    CheckResult toCheckResult(Check matchingCheck)
    {
        return new CheckResult(matchingCheck, this.unweightedScore, this.feedback);
    }

    /**
     * Find the Check with the matching identifier to this entry
     * @param checks the Checks to search
     * @return the matching Check, or null if no Checks match
     */
    @CheckForNull
    Check findMatchingCheck(Collection<Check> checks)
    {
        for(Check c : checks)
        {
            if (c.identifier().equals(this.checkIdentifier))
                return c;
        }
        return null;
    }

    @Override
    public String toString()
    {
        return checkIdentifier;
    }

}
