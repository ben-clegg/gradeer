package tech.clegg.gradeer.checks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.checks.exceptions.InvalidCheckException;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ManualCheck extends Check
{
    private static Logger logger = LogManager.getLogger(ManualCheck.class);

    private String prompt;
    private int maxRange = 1;
    private boolean arbitraryFeedback = false;

    public ManualCheck(JsonObject jsonObject, Configuration configuration) throws InvalidCheckException
    {
        super(jsonObject, configuration);
        concurrentCompatible = false;

        try
        {
            this.prompt = getOptionalElement(jsonObject, "prompt").get().getAsString();
        } catch (NoSuchElementException noElem)
        {
            throw new InvalidCheckException("ManualCheck " + getName() + " requires a prompt to be used.");
        }


        this.maxRange = getElementOrDefault(jsonObject, "maxRange", JsonElement::getAsInt, maxRange);
        this.arbitraryFeedback = getElementOrDefault(jsonObject, "arbitraryFeedback",
                JsonElement::getAsBoolean, arbitraryFeedback);
    }


    @Override
    public void execute(Solution solution)
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

        // Process
        processSolution(solution);
    }

    @Override
    protected double generateUnweightedScore(Solution solution)
    {
        // Calculate unweighted grade
        double unweightedScore = 1.0; // Default to full score (in case no weight set)
        if(weight > 0)
        {
            System.out.print("Enter a value in the range 0 - " + maxRange);
            if(maxRange == 1)
                System.out.print(" or y(es) / n(o)");
            System.out.println();

            int inputResult = getNumericInputResult();
            unweightedScore = (double) inputResult / maxRange;
            System.out.println("Entered value [" + inputResult + "] " +
                    "(unweighted score of " + unweightedScore + " / 1.0)");
        }
        return unweightedScore;
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
            return getAffirmation();
        }

        String input = scanner.next().trim();

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
            return getNumericInputResult();
        }

        String input = scanner.next().trim();

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
    protected String generateFeedback(double unweightedScore)
    {
        StringBuilder feedback = new StringBuilder();

        // Tiered feedback
        String boundedFeedback = super.generateFeedback(unweightedScore);
        if(!boundedFeedback.isEmpty())
            feedback.append(boundedFeedback);

        // Determine arbitrary feedback to add (if enabled)
        if(arbitraryFeedback)
        {
            System.out.println("Enter feedback:");
            String arbitraryFeedback = getStringInputResult();
            if(!arbitraryFeedback.isEmpty())
            {
                if(!feedback.toString().isEmpty())
                    feedback.append("\n");
                feedback.append(arbitraryFeedback);
            }
        }

        return feedback.toString();
    }


    @Override
    public String toString()
    {
        return "ManualCheck{" +
                "prompt='" + prompt + '\'' +
                ", maxRange=" + maxRange +
                ", (" + super.toString() + ")" +
                '}';
    }
}
