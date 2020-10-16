package tech.clegg.gradeer.checks;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.checks.exceptions.InvalidCheckException;
import tech.clegg.gradeer.checks.generation.FeedbackEntry;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;

import java.util.*;
import java.util.function.Function;

public abstract class Check
{
    protected boolean concurrentCompatible = true;
    private final Configuration configuration;
    protected String name;
    protected double weight = 1.0;
    protected Map<Double, String> feedbackForUnweightedScoreBounds = new TreeMap<>();

    protected Check(String name, Configuration configuration)
    {
        this.name = name;
        this.configuration = configuration;
    }

    public Check(JsonObject jsonObject, Configuration configuration) throws InvalidCheckException
    {
        this.configuration = configuration;

        // Load name
        try
        {
            // Purposefully unsafe; names are required and have no default
            this.name = getOptionalElement(jsonObject, "name").get().getAsString();
        }
        catch (NoSuchElementException noElem)
        {
            throw new InvalidCheckException("No name defined for Check " + jsonObject.getAsString() + " , skipping.");
        }

        // Load weight
        this.weight = getElementOrDefault(jsonObject, "weight", JsonElement::getAsDouble, weight);

        // Load Feedback
        Gson gson = new Gson();
        try
        {
            FeedbackEntry[] feedbackValues = gson.fromJson(getOptionalElement(jsonObject, "feedbackValues").get(), FeedbackEntry[].class);
            for (FeedbackEntry f : feedbackValues)
            {
                feedbackForUnweightedScoreBounds.put(f.getScore(), f.getFeedback());
            }
        }
        catch (NoSuchElementException ignored)
        {
            // Allow, because no feedback is valid, but warn.
            System.err.println("No feedback defined for " + identifier());
        }
        catch (JsonSyntaxException e)
        {
            e.printStackTrace();
        }

    }

    protected static <T> T getElementOrDefault(JsonObject jsonObject,
                                               String memberName,
                                               Function<JsonElement, ? extends T>  elementConversion,
                                               T defaultValue)
    {
        Optional<JsonElement> optionalElem = getOptionalElement(jsonObject, memberName);
        if(optionalElem.isPresent())
        {
            Optional<T> optionalT = optionalElem.map(elementConversion);
            if(optionalT.isPresent())
                return optionalT.get();
        }

        return defaultValue;
    }

    protected static Optional<JsonElement> getOptionalElement(JsonObject jsonObject, String memberName)
    {
        return Optional.ofNullable(jsonObject.get(memberName));
    }

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

    protected Configuration getConfiguration()
    {
        return configuration;
    }

    public double getWeight()
    {
        return weight;
    }

    public String getName()
    {
        return name;
    }

    public boolean isConcurrentCompatible()
    {
        return concurrentCompatible;
    }

    protected String generateFeedback(double unweightedScore)
    {
        if(feedbackForUnweightedScoreBounds.isEmpty())
            return "";

        return getBoundedFeedbackForScore(unweightedScore);
    }

    private String getBoundedFeedbackForScore(double score)
    {
        Iterator<Double> keysInterator = feedbackForUnweightedScoreBounds.keySet().iterator();

        double lastKey = -1;

        // Assuming ascending order of TreeMap, get the key just below or equal to the actual score
        while (keysInterator.hasNext()) {
            double k = keysInterator.next();
            if(score >= k)
                lastKey = k;
            else
                break;
        }

        return feedbackForUnweightedScoreBounds.get(lastKey);
    }

    public void setSolutionAsFailed(Solution solution)
    {
        solution.addCheckResult(new CheckResult(
                this, 0.0, generateFeedback(0.0)
        ));
    }

    protected CheckResult generateCheckResult(double unweightedScore)
    {
        return new CheckResult(this, unweightedScore, generateFeedback(unweightedScore));
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
                getName();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Check check = (Check) o;
        return Double.compare(check.getWeight(), getWeight()) == 0 &&
                getName().equals(check.getName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getWeight(), getName());
    }

}
