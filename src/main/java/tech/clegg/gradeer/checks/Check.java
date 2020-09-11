package tech.clegg.gradeer.checks;

import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.solution.Solution;

public abstract class Check
{
    protected double weight = 1.0;
    protected String name;
    protected String feedbackCorrect = "";
    protected String feedbackIncorrect = "";

    public abstract void run(Solution solution);

    public void setWeight(double weight)
    {
        this.weight = weight;
    }

    public double getWeight()
    {
        return weight;
    }

    public String getName()
    {
        return name;
    }

    protected String generateFeedback(double unweightedScore)
    {
        if(unweightedScore < 1)
        {
            if(feedbackIncorrect.isEmpty())
                return "";
            return feedbackIncorrect; // Provide feedback for incorrect case
        }
        if(feedbackCorrect.isEmpty())
            return "";
        return feedbackCorrect; // Feedback for correct case
    }

    public void setFeedback(String feedbackCorrect, String feedbackIncorrect)
    {
        this.feedbackCorrect = feedbackCorrect;
        this.feedbackIncorrect = feedbackIncorrect;
    }

    public void setSolutionAsFailed(Solution solution)
    {
        solution.addCheckResult(this, new CheckResult(
                0.0, feedbackIncorrect
        ));
    }

    @Override
    public String toString()
    {
        return "Check{" +
                "weight=" + weight +
                ", name='" + name +
                '}';
    }
}
