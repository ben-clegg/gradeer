package tech.clegg.gradeer.checks;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.checks.exceptions.InvalidCheckException;
import tech.clegg.gradeer.checks.generation.FeedbackEntry;
import tech.clegg.gradeer.checks.generation.FlagsEntry;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.preprocessing.PreProcessor;
import tech.clegg.gradeer.solution.Solution;

import java.util.*;
import java.util.function.Function;

public abstract class Check
{
    protected boolean concurrentCompatible = true;
    private final Configuration configuration;
    protected String name;
    protected double weight = 1.0;
    private int priority = 10;
    private String checkGroup = "";

    protected Map<Double, String> feedbackForUnweightedScoreBounds = new TreeMap<>();
    protected Map<Double, String[]> flagMap = new TreeMap<>();

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

        // Default checkGroup
        checkGroup = defaultCheckGroupForType();
        // Load custom check group if present
        Optional<JsonElement> checkGroupOpt = getOptionalElement(jsonObject, "checkGroup");
        checkGroupOpt.ifPresent(jsonElement -> this.checkGroup = jsonElement.getAsString());

        // Load weight
        this.weight = getElementOrDefault(jsonObject, "weight", JsonElement::getAsDouble, weight);

        // Load priority
        this.priority = getElementOrDefault(jsonObject, "priority", JsonElement::getAsInt, priority);

        // Load concurrency compatibility
        this.concurrentCompatible = getElementOrDefault(jsonObject, "concurrentCompatible",
                JsonElement::getAsBoolean, concurrentCompatible);

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

        // Load flags
        try
        {
            FlagsEntry[] flagValues = gson.fromJson(getOptionalElement(jsonObject, "flags").get(), FlagsEntry[].class);
            for (FlagsEntry f : flagValues)
            {
                flagMap.put(f.getScore(), f.getFlags());
            }
        }
        catch (NoSuchElementException ignored)
        {
            // Allow, because including no flags is valid
        }
        catch (JsonSyntaxException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Each check class can have a default checkGroup
     * Override in the subclass to change the default
     * @return the default checkGroup
     */
    protected String defaultCheckGroupForType()
    {
        return "";
    }

    public abstract Collection<Class<? extends PreProcessor>> getPreProcessorTypes();

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
        else
        {
            // Still need to add any relevant flags to the Solution
            solution.addFlags(generateFlags(solution.getCheckResult(this).getUnweightedScore()));
        }
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

    /**
     * Conventional run of any given Check.
     * The called methods can be overridden by individual Check classes.
     * Individual Check classes may also run some pre-processing or alternate executions for edge cases.
     * In such events, it is strongly recommended to generate a CheckResult; such as one with a score of 0 when a Check
     * fails to run.
     * @param solution the Solution to run this Check on
     */
    public void processSolution(Solution solution)
    {
        double unweightedScore = generateUnweightedScore(solution);
        String feedback = generateFeedback(unweightedScore);
        solution.addCheckResult(generateCheckResult(unweightedScore, feedback));
        solution.addFlags(generateFlags(unweightedScore));
    }

    protected abstract double generateUnweightedScore(Solution solution);

    protected String generateFeedback(double unweightedScore)
    {
        return getBoundedFeedbackForScore(unweightedScore);
    }

    private <T> T getBoundedMapValueForScore(double unweightedScore, Map<Double, T> map, T defaultIfEmpty)
    {
        if (map.isEmpty())
            return defaultIfEmpty;

        Iterator<Double> keysIterator = map.keySet().iterator();

        double lastKey = -1;

        // Assuming ascending order of TreeMap, get the key just below or equal to the actual score
        while (keysIterator.hasNext()) {
            double k = keysIterator.next();
            if(unweightedScore >= k)
                lastKey = k;
            else
                break;
        }

        return map.get(lastKey);
    }

    private String getBoundedFeedbackForScore(double unweightedScore)
    {
        return getBoundedMapValueForScore(unweightedScore, feedbackForUnweightedScoreBounds, "");
    }

    private String[] getBoundedFlagForScore(double unweightedScore)
    {
        return getBoundedMapValueForScore(unweightedScore, flagMap, new String[]{});
    }

    public void setSolutionAsFailed(Solution solution)
    {
        solution.addCheckResult(
                generateCheckResult(0.0, generateFeedback(0.0))
        );
    }

    protected CheckResult generateCheckResult(double unweightedScore, String feedback)
    {
        return new CheckResult(this, unweightedScore, feedback);
    }

    protected CheckResult generateCheckResult(double unweightedScore)
    {
        return new CheckResult(this, unweightedScore);
    }

    public int getPriority()
    {
        return priority;
    }

    /**
     * Determine the flags associated with a Solution from a given unweighted score
     * @param unweightedScore the unweighted score to determine the flags from
     * @return the identified flags
     */
    protected Collection<String> generateFlags(double unweightedScore)
    {
        String[] flags = getBoundedFlagForScore(unweightedScore);
        return new HashSet<>(Arrays.asList(flags));
    }

    public String getCheckGroup()
    {
        return checkGroup;
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
