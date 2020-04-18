package tech.clegg.gradeer.checks;

import tech.clegg.gradeer.checks.generation.json.FeedbackEntry;
import tech.clegg.gradeer.checks.generation.json.ManualCheckJSONEntry;
import tech.clegg.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ManualCheck extends Check
{
    private static Logger logger = LogManager.getLogger(ManualCheck.class);

    private String prompt;
    private int maxRange;

    private Map<Double, String> feedbackForUnweightedScoreBounds;

    public ManualCheck(ManualCheckJSONEntry jsonEntry)
    {
        this.name = jsonEntry.getName();
        this.weight = jsonEntry.getWeight();
        this.prompt = jsonEntry.getPrompt();
        this.maxRange = jsonEntry.getMaxRange();

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
        System.out.println("\nManual check " + name + " for Solution " + solution.getIdentifier());
        System.out.println(prompt);
        System.out.print("Enter a value in the range 0 - " + maxRange);
        if(maxRange == 1)
            System.out.print(" or y(es) / n(o)");
        System.out.println();

        int inputResult = getInputResult();
        double unweightedScore = (double) inputResult / maxRange;
        unweightedScores.put(solution, unweightedScore);

        System.out.println("Entered value [" + inputResult + "] (unweighted score of " + unweightedScore + " / 1.0)");
    }

    private int getInputResult()
    {
        // Get input
        Scanner scanner = new Scanner(System.in);

        if(!scanner.hasNext())
        {
            System.err.println("No input provided.");
            System.err.println("Please re-enter.");
            return getInputResult();
        }

        String input = scanner.next().trim();

        if(input.isEmpty())
        {
            System.err.println("No input provided.");
            System.err.println("Please re-enter.");
            return getInputResult();
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
            return getInputResult();
        }
        catch (NumberFormatException numberFormatException)
        {
            System.err.println(input + " is not a valid integer");
            System.err.println("Please re-enter.");
            return getInputResult();
        }
    }

    @Override
    public String getFeedback(Solution solution)
    {
        double score = this.getUnweightedScore(solution);

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
