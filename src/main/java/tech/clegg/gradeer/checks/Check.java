package tech.clegg.gradeer.checks;

import tech.clegg.gradeer.solution.Solution;

import java.util.HashMap;
import java.util.Map;

public abstract class Check
{
    protected double weight = 1.0;
    protected String name;
    protected String feedbackCorrect = "";
    protected String feedbackIncorrect = "";

    protected Map<Solution, Double> unweightedScores = new HashMap<>();

    public abstract void run(Solution solution);

    public double getWeightedScore(Solution solution)
    {
        return weight * getUnweightedScore(solution);
    }

    public double getUnweightedScore(Solution solution)
    {
        return unweightedScores.get(solution);
    }

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

    public String getFeedback(Solution solution)
    {
        double unweightedScore = this.unweightedScores.get(solution);
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

    public boolean wasSolutionExecuted(Solution solution)
    {
        return unweightedScores.containsKey(solution);
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
