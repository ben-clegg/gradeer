package tech.clegg.gradeer.checks.checkresults;

import tech.clegg.gradeer.checks.Check;

/**
 * The results of a Check on an individual Solution.
 * Includes the unweighted grade and any relevant feedback.
 */
public class CheckResult
{
    private final double unweightedScore;
    private final String feedback;
    private final Check check;

    public CheckResult(Check check, double unweightedScore, String feedback)
    {
        this.check = check;
        this.unweightedScore = unweightedScore;
        this.feedback = feedback;
    }

    public CheckResult(Check check, double unweightedScore)
    {
        this.check = check;
        this.unweightedScore = unweightedScore;
        this.feedback = "";
    }

    public double getUnweightedScore()
    {
        return unweightedScore;
    }

    public String getFeedback()
    {
        return feedback;
    }

    public Check getCheck()
    {
        return check;
    }
}
