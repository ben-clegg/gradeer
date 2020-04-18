package tech.clegg.gradeer.results;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.CheckProcessor;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.results.io.SCSVWriter;
import tech.clegg.gradeer.results.io.FileWriter;
import tech.clegg.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
            studentSolutions.forEach(checkProcessor::runChecks);
        }

        writeSolutionsFailingAllUnitTests();
        writeCheckResults();
        writeGrades();
        writeFeedback();
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

    private void writeCheckResults()
    {
        if(configuration.getCheckResultsDir() == null)
            return;

        configuration.getCheckResultsDir().toFile().mkdirs();

        for (Solution s : studentSolutions)
        {
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

    private void writeGrades()
    {
        GradeGenerator gradeGenerator = new GradeGenerator(checkProcessors);
        SCSVWriter gradeWriter = new SCSVWriter(Arrays.asList("Username", "Grade", "Feedback"));
        for (Solution s : studentSolutions)
        {
            double grade = gradeGenerator.generateGrade(s);

            String[] line = {s.getIdentifier(), String.valueOf(grade), "\"" + generateFeedback(s) + "\""};
            gradeWriter.addEntry(Arrays.asList(line));
            logger.info("Grade Generated: " + Arrays.toString(line));
        }
        gradeWriter.write(Paths.get(configuration.getOutputDir() + File.separator + "AssignmentMarks.scsv"));
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
}
