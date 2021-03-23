package tech.clegg.gradeer.preprocessing.staticanalysis.pmd;

import tech.clegg.gradeer.preprocessing.PreProcessorResults;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PMDProcessResults extends PreProcessorResults
{
    private List<String> errorLines;
    private List<PMDViolation> pmdViolations;

    private static final int PMD_PROCESS_TIME = 15;

    public PMDProcessResults(ProcessBuilder processBuilder)
    {
        super();
        try
        {
            Process process = processBuilder.start();

            BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            this.pmdViolations = loadViolations(readLines(inputStreamReader));
            // The line below must not be executed before the line above or PMD will not terminate.
            // I don't know why.
            this.errorLines = readLines(errorStreamReader);

            process.waitFor(PMD_PROCESS_TIME, TimeUnit.SECONDS);
            process.destroy();

            process.getInputStream().close();
            process.getErrorStream().close();

        } catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }

    }

    private List<String> readLines(BufferedReader bufferedReader)
    {
        if(bufferedReader == null)
            return new ArrayList<>();

        List<String> lines = new ArrayList<>();
        String line;
        try
        {
            while ((line = bufferedReader.readLine()) != null)
            {
                lines.add(line);
            }
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
        }
        return lines;
    }

    private List<PMDViolation> loadViolations(List<String> csvOutput)
    {
        List<PMDViolation> violations = new ArrayList<>();

        for (String line : csvOutput)
        {
            if(PMDViolation.isValidCSVLine(line))
            {
                violations.add(new PMDViolation(line));
            }
        }
        return violations;
    }

    public List<String> getErrorLines()
    {
        return errorLines;
    }

    public List<PMDViolation> getPmdViolations()
    {
        return pmdViolations;
    }

    public Collection<PMDViolation> getViolations(String ruleName)
    {
        return pmdViolations.stream()
                .filter(v -> v.getRule().toLowerCase().equals(ruleName.toLowerCase()))
                .collect(Collectors.toList());
    }
}
