package tech.clegg.gradeer.solution;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.execution.staticanalysis.checkstyle.CheckstyleProcessResults;
import tech.clegg.gradeer.execution.staticanalysis.pmd.PMDProcessResults;
import tech.clegg.gradeer.subject.JavaSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class Solution
{
    private static Logger logger = LogManager.getLogger(Solution.class);

    Path directory;
    Collection<JavaSource> sources;

    private CheckstyleProcessResults checkstyleProcessResults;
    private PMDProcessResults pmdProcessResults;

    private Collection<String> flags = new HashSet<>();
    private Map<Check, CheckResult> checkResultsMap = new HashMap<>();

    public Solution(Path locationDir)
    {
        this.directory = locationDir;
        try
        {
            sources = Files.walk(directory)
                    .filter(p -> com.google.common.io.Files.getFileExtension(p.toString()).equals("java"))
                    // Remove the hidden files generated by OSX, can't be parsed correctly.
                    .filter(p -> !p.toString().contains("__MACOSX"))
                    .map(p -> new JavaSource(p, locationDir))
                    .collect(Collectors.toList());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void checkForMissingSources(Collection<String> complexClassNames)
    {
        if (complexClassNames == null || complexClassNames.isEmpty())
            return;

        Collection<String> presentClasses = sources.stream()
                .map(JavaSource::getComplexClassName)
                .collect(Collectors.toSet());

        Collection<String> missing = new HashSet<>();

        for (String c : complexClassNames)
        {
            if(!presentClasses.contains(c))
            {
                missing.add(c);
            }
        }

        if(!missing.isEmpty())
        {
            addFlag(DefaultFlag.MISSING_CLASS);
            System.err.println("Solution " + getIdentifier() + " has missing required class(es):");
            System.err.println(missing);
            System.err.println("\n");
        }

    }

    public void setCheckstyleProcessResults(CheckstyleProcessResults checkstyleProcessResults)
    {
        this.checkstyleProcessResults = checkstyleProcessResults;
    }

    public void setPmdProcessResults(PMDProcessResults pmdProcessResults)
    {
        this.pmdProcessResults = pmdProcessResults;
    }

    public CheckstyleProcessResults getCheckstyleProcessResults()
    {
        return checkstyleProcessResults;
    }

    public PMDProcessResults getPmdProcessResults()
    {
        return pmdProcessResults;
    }

    public Path getDirectory()
    {
        return directory;
    }

    public Collection<JavaSource> getSources()
    {
        return sources;
    }

    public String getIdentifier()
    {
        return directory.getFileName().toString();
    }

    public boolean isCompiled()
    {
        for (JavaSource s : sources)
        {
            if(!s.isCompiled())
                return false;
        }
        return true;
    }

    public void addFlags(Collection<String> flagCollection)
    {
        flags.addAll(flagCollection);
    }

    public void addFlag(DefaultFlag flag)
    {
        addFlag(flag.name());
    }

    public void addFlag(String flag)
    {
        flags.add(flag);
    }

    public Collection<String> getFlags()
    {
        return flags;
    }

    public boolean containsFlag(String flag)
    {
        return flags.contains(flag);
    }

    public void addCheckResult(CheckResult result)
    {
        checkResultsMap.put(result.getCheck(), result);
    }

    public void addAllCheckResults(Collection<CheckResult> checkResults, boolean overwrite)
    {
        if (overwrite)
            checkResults.forEach(this::addCheckResult);
        else
        {
            for (CheckResult checkResult : checkResults)
            {
                if (!this.checkResultsMap.containsKey(checkResult.getCheck()))
                    addCheckResult(checkResult);
            }
        }
    }

    public CheckResult getCheckResult(Check check)
    {
        return checkResultsMap.get(check);
    }

    public double calculateWeightedScore(Check check)
    {
        return getCheckResult(check).getUnweightedScore() * check.getWeight();
    }

    public boolean hasCheckResult(Check check)
    {
        return checkResultsMap.containsKey(check);
    }

    public Collection<CheckResult> getAllCheckResults()
    {
        return checkResultsMap.values();
    }

    public void clearChecks(Collection<Check> checks)
    {
        for (Check c : checks)
            this.checkResultsMap.remove(c);
    }
}
