package tech.clegg.gradeer.results;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.checkprocessing.AutoCheckProcessor;
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
        for (CheckProcessor checkProcessor : checkProcessors)
        {
            int solutionNumber = 1;
            for (Solution s : studentSolutions)
            {
                System.out.println("\nProcessing solution " + s.getIdentifier() +
                        " ( " + solutionNumber + " / " + studentSolutions.size() + " ) ");
                checkProcessor.runChecks(s);
                solutionNumber++;
            }
        }

        writeSolutionsFailingAllUnitTests();
        writeIndividualCheckResults();
        writeCombinedCheckResults();
        writeGrades();
        writeFeedback();
        writeSplitResultsWithWeights();
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
                            c.getWeightedScore(s) +
                            " / " +
                            c.getWeight()
                    );
                }
            }

            f.write(Paths.get(configuration.getCheckResultsDir() + File.separator + s.getIdentifier()));
        }
    }

    /**
     * Creates a matrix of unweighted scores for each check on each solution
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
                row.add(String.valueOf(c.getUnweightedScore(s)));
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

            String[] line = {s.getIdentifier(), String.valueOf(grade), "\"" + generateFeedback(s) + "\""};
            gradeWriter.addEntry(Arrays.asList(line));
            logger.info("Grade Generated: " + Arrays.toString(line));
        }
        gradeWriter.write(Paths.get(configuration.getOutputDir() + File.separator + "AssignmentMarks.csv"));
    }

    private String generateFeedback(Solution solution)
    {
        StringBuilder sb = new StringBuilder();

        for (CheckProcessor checkProcessor : checkProcessors)
        {
            for (Check c : checkProcessor.getChecks())
            {
                String feedback = c.getFeedback(solution);
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
                row.add(String.valueOf(c.getUnweightedScore(s)));
                row.add(c.getFeedback(s));
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
