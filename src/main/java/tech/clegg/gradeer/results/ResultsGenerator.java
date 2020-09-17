package tech.clegg.gradeer.results;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.checkprocessing.CheckProcessor;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.results.io.CSVWriter;
import tech.clegg.gradeer.results.io.FileWriter;
import tech.clegg.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ResultsGenerator implements Runnable
{
    private static final Logger logger = LogManager.getLogger(ResultsGenerator.class);

    private final Collection<Solution> studentSolutions;
    private List<CheckProcessor> checkProcessors;
    private Configuration configuration;

    public ResultsGenerator(Collection<Solution> studentSolutions, List<CheckProcessor> checkProcessors, Configuration configuration)
    {
        this.studentSolutions = studentSolutions;
        this.checkProcessors = checkProcessors;
        this.configuration = configuration;
    }

    public ResultsGenerator(Collection<Solution> studentSolutions, Configuration configuration)
    {
        this.studentSolutions = studentSolutions;
        this.checkProcessors = new ArrayList<>();
        this.configuration = configuration;
    }

    public void addCheckProcessor(CheckProcessor checkProcessor)
    {
        checkProcessors.add(checkProcessor);
    }

    @Override
    public void run()
    {
        int solutionNumber = 1;
        for (Solution s : studentSolutions)
        {
            System.out.println("\nProcessing solution " + s.getIdentifier() +
                    " ( " + solutionNumber + " / " + studentSolutions.size() + " ) ");

            processSolution(s);

            solutionNumber++;
        }

        writeSolutionsFailingAllUnitTests();
        writeIndividualCheckResults();
        writeCombinedCheckResults();
        writeGrades();
        writeFeedback();
        writeSplitResultsWithWeights();
    }

    /**
     * Process the checks for an individual Solution.
     * If resuming is enabled (default behaviour), the method
     * first attempts to restore stored grading state (i.e. CheckResults) from existing files for the Solution.
     * Next executes any checks that have no existing results.
     * Finally writes the Collection of CheckResults for the Solution to a file to allow for future restoring.
     * @param solution the Solution to process Checks for.
     */
    private void processSolution(Solution solution)
    {
        CheckResultsStorage checkResultsStorage = new CheckResultsStorage(configuration);

        // Attempt load of stored CheckResults for solution; allow for skipping
        if(configuration.isCheckResultRecoveryEnabled())
            checkResultsStorage.recoverCheckResults(solution, checkProcessors);

        // Run Checks for solution
        for (CheckProcessor checkProcessor : checkProcessors)
        {
            checkProcessor.runChecks(solution);
        }

        // Store CheckResults of solution
        checkResultsStorage.storeCheckResults(solution);
    }

    private void writeSolutionsFailingAllUnitTests()
    {
        Collection<Solution> failAllUnitTests = new ArrayList<>();
        for (Solution s : studentSolutions)
        {
            for (CheckProcessor cp : checkProcessors)
            {
                if(cp.failsAllUnitTests(s))
                {
                    failAllUnitTests.add(s);
                    break;
                }
            }
        }

        if(failAllUnitTests.isEmpty())
            return;

        FileWriter f = new FileWriter();
        for(Solution s : failAllUnitTests)
            f.addLine(s.getIdentifier());
        f.write(Paths.get(configuration.getOutputDir() + File.separator + "SolutionsFailingAllUnitTests"));
    }

    private void writeIndividualCheckResults()
    {
        if(configuration.getCheckResultsDir() == null)
            return;

        configuration.getCheckResultsDir().toFile().mkdirs();

        for (Solution s : studentSolutions)
        {
            // Individual file
            FileWriter f = new FileWriter();
            for (CheckProcessor checkProcessor : checkProcessors)
            {
                for (Check c : checkProcessor.getChecks())
                {
                    f.addLine(
                            c.getClass().getSimpleName() +
                            " - " +
                            c.getName() +
                            ": " +
                            s.calculateWeightedScore(c) +
                            " / " +
                            c.getWeight()
                    );
                }
            }

            f.write(Paths.get(configuration.getCheckResultsDir() + File.separator + s.getIdentifier()));
        }
    }



    /**
     * Creates a matrix of unweighted scores for each check on each solution, stored as a CSV
     */
    private void writeCombinedCheckResults()
    {
        List<Check> allChecks = getAllChecks();

        List<String> headers = new ArrayList<>();
        headers.add("Solution");
        headers.addAll(allChecks.stream()
                .map(c -> c.getClass().getSimpleName() + "-" + c.getName())
                .collect(Collectors.toList()));


        CSVWriter w = new CSVWriter(headers);

        for (Solution s : studentSolutions)
        {
            List<String> row = new ArrayList<>();
            row.add(s.getIdentifier());
            for (Check c : allChecks)
            {
                row.add(String.valueOf(s.getCheckResult(c).getUnweightedScore()));
            }
            w.addEntry(row);
        }

        w.write(Paths.get(configuration.getOutputDir() + File.separator + "allCheckResults.csv"));

    }


    private void writeGrades()
    {
        GradeGenerator gradeGenerator = new GradeGenerator(checkProcessors);
        CSVWriter gradeWriter = new CSVWriter(Arrays.asList("Username", "Grade", "Feedback"));
        for (Solution s : studentSolutions)
        {
            double grade = gradeGenerator.generateGrade(s);

            String[] line = {s.getIdentifier(), String.valueOf(grade), generateFeedback(s)};
            gradeWriter.addEntry(Arrays.asList(line));
            logger.info("Grade Generated: " + Arrays.toString(line));
        }
        gradeWriter.write(Paths.get(configuration.getOutputDir() + File.separator + "AssignmentMarks.csv"));
    }

    /**
     * Generate the complete feedback text for a Solution.
     * @param solution the Solution to generate the feedback text for.
     * @return the Solution's feedback text block.
     */
    private String generateFeedback(Solution solution)
    {
        StringBuilder sb = new StringBuilder();

        for (CheckProcessor checkProcessor : checkProcessors)
        {
            for (Check c : checkProcessor.getChecks())
            {
                String feedback = solution.getCheckResult(c).getFeedback();
                if(!feedback.isEmpty())
                    sb.append(feedback + "\n");
            }
        }

        return sb.toString();

    }

    private void writeFeedback()
    {
        // TODO implement

        for (Solution s : studentSolutions)
        {
            FileWriter file = new FileWriter();
            file.addLine(generateFeedback(s));
            file.write(Paths.get(configuration.getOutputDir() + File.separator + "feedback" + File.separator + s.getIdentifier() + "_feedback.txt"));

        }
    }

    /**
     * Writes a SCSV file that contains:
     * - a row of starting weights for each check
     * - unweighted scores for each check
     * - feedback for each check
     * - combined feedback at the end
     */
    private void writeSplitResultsWithWeights()
    {
        GradeGenerator gradeGenerator = new GradeGenerator(checkProcessors);
        List<Check> allChecks = getAllChecks();

        // Setup headers
        List<String> headers = new ArrayList<>();
        headers.add("Solution");
        for (Check c : allChecks)
        {
            String checkName =  c.getClass().getSimpleName() + "-" + c.getName();
            headers.add("UnweightedScore_" + checkName);
            headers.add("Feedback_" + checkName);
        }
        headers.add("CombinedGeneratedFeedback");
        headers.add("GeneratedGrade");

        // Add row of weights
        List<String> weightsRow = new ArrayList<>();
        weightsRow.add("Weights"); // "Solution"
        for (Check c : allChecks)
        {
            weightsRow.add(String.valueOf(c.getWeight()));
            weightsRow.add("-");
        }
        weightsRow.add("-");
        weightsRow.add("-");

        // Initialise SCSVWriter
        CSVWriter w = new CSVWriter(headers);
        w.addEntry(weightsRow);

        // Handle each solution
        for (Solution s : studentSolutions)
        {
            List<String> row = new ArrayList<>();
            row.add(s.getIdentifier());

            for (Check c : allChecks)
            {
                row.add(String.valueOf(s.getCheckResult(c).getUnweightedScore()));
                row.add(s.getCheckResult(c).getFeedback());
            }

            row.add(generateFeedback(s));
            row.add(String.valueOf(gradeGenerator.generateGrade(s)));
            w.addEntry(row);
        }

        w.write(Paths.get(configuration.getOutputDir() + File.separator +
                "splitStudentResultsForPostprocessing.csv"));

    }

    private List<Check> getAllChecks()
    {
        List<Check> allChecks = new ArrayList<>();
        for (CheckProcessor checkProcessor : checkProcessors)
            allChecks.addAll(checkProcessor.getChecks());

        return allChecks;
    }
}
