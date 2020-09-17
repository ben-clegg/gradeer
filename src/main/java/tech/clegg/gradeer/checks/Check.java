package tech.clegg.gradeer.checks;

import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.solution.Solution;

import java.util.Objects;

public abstract class Check
{
    protected double weight = 1.0;
    protected String name;
    protected String feedbackCorrect = "";
    protected String feedbackIncorrect = "";

    protected abstract void execute(Solution solution);

    public void run(Solution solution)
    {
        // Skip if CheckResult exists for the Solution for this Check
        if(!solution.hasCheckResult(this))
            execute(solution);
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
        solution.addCheckResult(new CheckResult(
                this, 0.0, feedbackIncorrect
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

    public String identifier()
    {
        return this.getClass().getSimpleName() + "_" +
                getName() + "_" +
                feedbackCorrect.hashCode() + "_" +
                feedbackIncorrect.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Check check = (Check) o;
        return Double.compare(check.getWeight(), getWeight()) == 0 &&
                getName().equals(check.getName()) &&
                Objects.equals(feedbackCorrect, check.feedbackCorrect) &&
                Objects.equals(feedbackIncorrect, check.feedbackIncorrect);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getWeight(), getName(), feedbackCorrect, feedbackIncorrect);
    }
}
