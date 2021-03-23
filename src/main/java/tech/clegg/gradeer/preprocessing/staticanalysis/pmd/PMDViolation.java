package tech.clegg.gradeer.preprocessing.staticanalysis.pmd;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PMDViolation
{
    private int problemNumber;
    private String packageName;
    private Path javaFile;
    private int priority;
    private int line;
    private String description;
    private String ruleSet;
    private String rule;

    public PMDViolation(String csvLine)
    {
        /*
            PMD CSV report lines are in the format:
            "Problem","Package","File","Priority","Line","Description","Rule set","Rule"
         */
        String[] values = csvLine.replace("\"", "").split(",");

        this.problemNumber = Integer.parseInt(values[0]);
        this.packageName = values[1];
        this.javaFile = Paths.get(values[2]);
        this.priority = Integer.parseInt(values[3]);
        this.line = Integer.parseInt(values[4]);
        this.description = values[5];
        this.ruleSet = values[6];
        this.rule = values[7];
    }

    public int getProblemNumber()
    {
        return problemNumber;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public Path getJavaFile()
    {
        return javaFile;
    }

    public int getPriority()
    {
        return priority;
    }

    public int getLine()
    {
        return line;
    }

    public String getDescription()
    {
        return description;
    }

    public String getRuleSet()
    {
        return ruleSet;
    }

    public String getRule()
    {
        return rule;
    }

    public static boolean isValidCSVLine(String csvLine)
    {
        String[] split = csvLine.replace("\"", "").split(",");

        // Check number of values
        if (split.length != 8)
            return false;

        // Check types
        try
        {
            Integer.valueOf(split[0]);
            Integer.valueOf(split[3]);
            Integer.valueOf(split[4]);
        }
        catch (NumberFormatException numberEx)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "PMDViolation{" +
                "problemNumber=" + problemNumber +
                ", packageName='" + packageName + '\'' +
                ", javaFile=" + javaFile +
                ", priority=" + priority +
                ", line=" + line +
                ", description='" + description + '\'' +
                ", ruleSet='" + ruleSet + '\'' +
                ", rule='" + rule + '\'' +
                '}';
    }
}
