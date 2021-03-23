package tech.clegg.gradeer.results;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.checkprocessing.CheckProcessor;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.results.io.CSVWriter;
import tech.clegg.gradeer.results.io.DelayedFileWriter;
import tech.clegg.gradeer.solution.Solution;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ResultsGenerator implements Runnable
{
    private final Collection<Solution> studentSolutions;
    protected List<CheckProcessor> checkProcessors;
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
        configuration.getTimer().split("Completed initialisation stage.");
        int solutionNumber = 1;
        for (Solution s : studentSolutions)
        {
            System.out.println("\nProcessing solution " + s.getIdentifier() +
                    " ( " + solutionNumber + " / " + studentSolutions.size() + " ) ");

            processSolution(s);

            solutionNumber++;
        }

        writeSolutionsFailingAllUnitTests();
        writeCombinedCheckResults();
        writeGrades();
        writeFeedback();
        writeSplitResultsWithWeights();

        // Write summary of solution flags
        SolutionFlagWriter solutionFlagWriter = new SolutionFlagWriter(configuration);
        solutionFlagWriter.write(studentSolutions);
    }

    /**
     * Process the checks for an individual Solution.
     * If resuming is enabled (default behaviour), the method
     * first attempts to restore stored grading state (i.e. CheckResults) from existing files for the Solution.
     * Next executes any checks that have no existing results.
     * Finally writes the Collection of CheckResults for the Solution to a file to allow for future restoring.
     * @param solution the Solution to process Checks for.
     */
    protected void processSolution(Solution solution)
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

        DelayedFileWriter f = new DelayedFileWriter();
        for(Solution s : failAllUnitTests)
            f.addLine(s.getIdentifier());
        f.write(Paths.get(configuration.getOutputDir() + File.separator + "SolutionsFailingAllUnitTests"));
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
            System.out.println("Grade Generated for " + s.getIdentifier() + ": " + grade);
        }
        gradeWriter.write(Paths.get(configuration.getOutputDir() + File.separator + "AssignmentMarks.csv"));
    }

    /**
     * Generate the complete feedback text for a Solution.
     * This uses the checkGroup of each Check to group outputs together. This can also generate summative grades for each part
     * @param solution the Solution to generate the feedback text for.
     * @return the Solution's feedback text block.
     */
    private String generateFeedback(Solution solution)
    {
        StringBuilder sb = new StringBuilder();

        Collection<Check> checks = new HashSet<>();
        for (CheckProcessor checkProcessor : checkProcessors)
            checks.addAll(checkProcessor.getAllChecks());

        Set<String> checkGroups = uniqueCheckGroups(checks);

        for (String cg : checkGroups)
        {
            Collection<Check> checksInGroup = checks.stream().filter(c -> c.getCheckGroup().equals(cg))
                    .collect(Collectors.toList());

            if(cg.isEmpty())
                sb.append("Other Criteria: ");
            else
                sb.append(cg).append(": ");

            sb.append(GradeGenerator.generateGrade(solution, checksInGroup));
            sb.append("\n");

            for (Check c : checksInGroup)
            {
                String feedback = solution.getCheckResult(c).getFeedback();
                if(!feedback.isEmpty())
                    sb.append(feedback).append("\n");
            }

            sb.append("\n");
        }

        return sb.toString();

    }

    private Set<String> uniqueCheckGroups(Collection<Check> checks)
    {
        return checks.stream().map(Check::getCheckGroup).collect(Collectors.toSet());
    }

    private void writeFeedback()
    {
        for (Solution s : studentSolutions)
        {
            DelayedFileWriter file = new DelayedFileWriter();
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
            allChecks.addAll(checkProcessor.getAllChecks());

        return allChecks;
    }
}
