package tech.clegg.gradeer.checks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dnl.utils.text.table.TextTable;
import tech.clegg.gradeer.checks.checkresults.CheckResult;
import tech.clegg.gradeer.checks.exceptions.InvalidCheckException;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.preprocessing.JavaBatchExecutorPreProcessor;
import tech.clegg.gradeer.preprocessing.PreProcessor;
import tech.clegg.gradeer.preprocessing.SourceInspectorPreProcessor;
import tech.clegg.gradeer.solution.Solution;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ManualCheck extends Check
{
    private String prompt;
    private int maxRange = 1;
    private boolean arbitraryFeedback = false;

    public ManualCheck(JsonObject jsonObject, Configuration configuration) throws InvalidCheckException
    {
        super(jsonObject, configuration);
        this.concurrentCompatible = false;

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
    public Collection<Class<? extends PreProcessor>> getPreProcessorTypes()
    {
        Collection<Class<? extends PreProcessor>> preProcessorTypes = new HashSet<>();

        preProcessorTypes.add(JavaBatchExecutorPreProcessor.class);
        preProcessorTypes.add(SourceInspectorPreProcessor.class);

        return preProcessorTypes;
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
        generateCheckResultsTable().printTable();

        // Process
        processSolution(solution);
    }

    private TextTable generateCheckResultsTable()
    {
        String[] columnNames = {"Boundary", "(Normalized boundary)", "Mapped feedback"};

        // Load results
        String[][] entries = new String[feedbackForUnweightedScoreBounds.size()][3];
        int i = 0;
        for (double unweightedBound : feedbackForUnweightedScoreBounds.keySet())
        {
            entries[i][0] = ">=" + (maxRange * unweightedBound);
            entries[i][1] = ">=" + unweightedBound;
            entries[i][2] = feedbackForUnweightedScoreBounds.get(unweightedBound);
            i++;
        }

        return new TextTable(columnNames, entries);
    }

    @Override
    protected double generateUnweightedScore(Solution solution)
    {
        // Calculate unweighted grade
        double unweightedScore = 1.0; // Default to full score (in case no weight set ; manual feedback only mode)
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
        System.out.println("Enter feedback:");

        // Get input
        Scanner scanner = new Scanner(System.in);

        if(!scanner.hasNext())
        {
            System.err.println("No input provided.");
            System.err.println("Please re-enter.");
            return getStringInputResult();
        }

        String input = scanner.nextLine().trim();

        if(input.isEmpty())
        {
            System.out.println("Note: no input provided!");
        }
        System.out.println();
        System.out.println("Entered input: ");
        System.out.println(input);
        System.out.println();

        scanner.reset();

        // Check that result approved
        if(!getAffirmation())
            return getStringInputResult();

        return input;
    }

    private boolean getAffirmation()
    {
        System.out.println("Accept? (y / n)");

        Scanner scanner = new Scanner(System.in);

        if(!scanner.hasNext())
        {
            return getAffirmation();
        }

        String input = scanner.next().trim();

        scanner.reset();

        if(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes"))
            return true;
        else if(input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no"))
            return false;

        System.err.println("Invalid confirmation.");
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
