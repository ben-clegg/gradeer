package tech.clegg.gradeer.checks.checkresults;

/**
 * The results of a Check on an individual Solution.
 * Includes the unweighted grade and any relevant feedback.
 */
public class CheckResult
{
    private final double unweightedScore;
    private final String feedback;

    public CheckResult(double unweightedScore, String feedback)
    {
        this.unweightedScore = unweightedScore;
        this.feedback = feedback;
    }

    public CheckResult(double unweightedScore)
    {
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
}
