package tech.clegg.gradeer.checks.generation.json;

public class CheckJSONEntry
{
    String name;
    String feedbackCorrect = "";
    String feedbackIncorrect = "";
    double weight;

    public String getName()
    {
        return name;
    }

    public String getFeedbackCorrect()
    {
        return feedbackCorrect;
    }

    public String getFeedbackIncorrect()
    {
        return feedbackIncorrect;
    }

    public double getWeight()
    {
        return weight;
    }
}


