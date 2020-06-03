package tech.clegg.gradeer.checks.generation.json;

public class StaticAnalysisCheckJSONEntry extends CheckJSONEntry
{
    // Defaults are -1 ; allow for the value to be undefined without causing an NPE
    // Note: values must be checked before using.
    int maxViolations = -1;
    int minViolations = -1;

    public int getMaxViolations()
    {
        return maxViolations;
    }

    public int getMinViolations()
    {
        return minViolations;
    }
}


