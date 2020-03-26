package gradeer.execution.pmd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PMDProcessResults
{
    private static Logger logger = LogManager.getLogger(PMDProcessResults.class);

    private List<String> errorLines;
    private List<PMDViolation> pmdViolations;

    public PMDProcessResults(Process process)
    {
        BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        this.errorLines = readLines(errorStreamReader);
        this.pmdViolations = loadViolations(readLines(inputStreamReader));
    }

    private List<String> readLines(BufferedReader bufferedReader)
    {
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
}
