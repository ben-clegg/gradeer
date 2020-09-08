package tech.clegg.gradeer.checks;

import tech.clegg.gradeer.checks.generation.json.FeedbackEntry;
import tech.clegg.gradeer.checks.generation.json.ManualCheckJSONEntry;
import tech.clegg.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ManualCheck extends Check
{
    private static Logger logger = LogManager.getLogger(ManualCheck.class);

    private String prompt;
    private int maxRange;
    private boolean arbitraryFeedback;

    private Map<Double, String> feedbackForUnweightedScoreBounds;
    private Map<Solution, String> arbitraryFeedbackPerSolution;

    public ManualCheck(ManualCheckJSONEntry jsonEntry)
    {
        this.name = jsonEntry.getName();
        this.weight = jsonEntry.getWeight();
        this.prompt = jsonEntry.getPrompt();
        this.maxRange = jsonEntry.getMaxRange();
        this.arbitraryFeedback = jsonEntry.isArbitraryFeedback();

        this.feedbackForUnweightedScoreBounds = new TreeMap<>();
        // Map feedback minimum values to the same space as unweighted scores (i.e. 0 - 1)
        for(FeedbackEntry fe : jsonEntry.getFeedbackEntries())
        {
            double unweightedValue = (double) fe.getMinimumScore() / maxRange;
            feedbackForUnweightedScoreBounds.put(unweightedValue, fe.getFeedback());
        }
    }

    @Override
    public void run(Solution solution)
    {
        // Force clear past input to prevent state infection
        try
        {
            System.in.read(new byte[System.in.available()]);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // Start check
        System.out.println("\nManual check " + name + " for Solution " + solution.getIdentifier());
        System.out.println(prompt);

        // Calculate unweighted grade
        if(weight > 0)
        {
            System.out.print("Enter a value in the range 0 - " + maxRange);
            if(maxRange == 1)
                System.out.print(" or y(es) / n(o)");
            System.out.println();

            int inputResult = getNumericInputResult();
            double unweightedScore = (double) inputResult / maxRange;
            unweightedScores.put(solution, unweightedScore);
            System.out.println("Entered value [" + inputResult + "] (unweighted score of " + unweightedScore + " / 1.0)");
        }
        else
        {
            // Skip if no weight for check (check disabled or ungraded)
            unweightedScores.put(solution, 1.0);
        }

        // Determine arbitrary feedback to add (if enabled)
        if(arbitraryFeedback)
        {
            System.out.println("Enter feedback:");
            String feedback = getStringInputResult();
            arbitraryFeedbackPerSolution.put(solution, feedback);
        }

    }

    private String getStringInputResult()
    {
        // Get input
        Scanner scanner = new Scanner(System.in);

        if(!scanner.hasNext())
        {
            System.err.println("No input provided.");
            System.err.println("Please re-enter.");
            return getStringInputResult();
        }

        String input = scanner.next().trim();
        scanner.close();

        if(input.isEmpty())
        {
            System.out.println("Note: no input provided!");
        }
        System.out.println();
        System.out.println("Entered input: ");
        System.out.println(input);
        System.out.println();

        // Check that result approved
        System.out.println("Accept? (y / n)");
        if(!getAffirmation())
            return getStringInputResult();

        return input;
    }

    private boolean getAffirmation()
    {
        Scanner scanner = new Scanner(System.in);

        if(!scanner.hasNext())
        {
            scanner.close();
            return getAffirmation();
        }

        String input = scanner.next().trim();
        scanner.close();

        if(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes"))
            return true;
        else if(input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no"))
            return false;

        return getAffirmation();
    }

    private int getNumericInputResult()
    {
        // Get input
        Scanner scanner = new Scanner(System.in);

        if(!scanner.hasNext())
        {
            System.err.println("No input provided.");
            System.err.println("Please re-enter.");
            scanner.close();
            return getNumericInputResult();
        }

        String input = scanner.next().trim();
        scanner.close();

        if(input.isEmpty())
        {
            System.err.println("No input provided.");
            System.err.println("Please re-enter.");
            return getNumericInputResult();
        }


        // Check if result in correct format
        // Booleans accepted (y / n)
        if(maxRange == 1)
        {
            if(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes"))
                return 1;
            if(input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no"))
                return 0;
        }

        try
        {
            int inputInt = Integer.parseInt(input);

            if(inputInt >= 0 && inputInt <= maxRange)
                return inputInt;

            System.err.println(inputInt + " is out of the range 0 - " + maxRange);
            System.err.println("Please re-enter.");
            return getNumericInputResult();
        }
        catch (NumberFormatException numberFormatException)
        {
            System.err.println(input + " is not a valid integer");
            System.err.println("Please re-enter.");
            return getNumericInputResult();
        }
    }

    @Override
    public String getFeedback(Solution solution)
    {
        StringBuilder feedback = new StringBuilder();

        // Tiered feedback
        String boundedFeedback = getBoundedFeedbackForScore(getUnweightedScore(solution));
        if(!boundedFeedback.isEmpty())
            feedback.append(boundedFeedback);

        // Arbitrary feedback
        String arbitraryFeedback = arbitraryFeedbackPerSolution.getOrDefault(solution, "");
        if(!arbitraryFeedback.isEmpty())
        {
            if(!feedback.toString().isEmpty())
                feedback.append("\n");
            feedback.append(arbitraryFeedback);
        }
        return feedback.toString();
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

    @Override
    public String toString()
    {
        return "ManualCheck{" +
                "prompt='" + prompt + '\'' +
                ", maxRange=" + maxRange +
                ", feedbackForUnweightedScoreBounds=" + feedbackForUnweightedScoreBounds +
                ", weight=" + weight +
                ", name='" + name + '\'' +
                '}';
    }
}
