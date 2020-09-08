package tech.clegg.gradeer.checks.generation.json;

public class ManualCheckJSONEntry
{
    String name;
    String prompt;
    double weight;
    int maxRange;
    FeedbackEntry[] feedbackEntries;
    boolean arbitraryFeedback = false;

    public String getName()
    {
        return name;
    }

    public String getPrompt()
    {
        return prompt;
    }

    public double getWeight()
    {
        return weight;
    }

    public int getMaxRange()
    {
        return maxRange;
    }

    public FeedbackEntry[] getFeedbackEntries()
    {
        return feedbackEntries;
    }

    public boolean isArbitraryFeedback()
    {
        return arbitraryFeedback;
    }
}

