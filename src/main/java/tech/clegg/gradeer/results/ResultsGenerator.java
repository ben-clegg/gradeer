package tech.clegg.gradeer.results;

import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.CheckProcessor;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.results.io.CSVWriter;
import tech.clegg.gradeer.results.io.FileWriter;
import tech.clegg.gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
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

        writeCheckResults();
        writeGrades();
        writeFeedback();
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
        CSVWriter gradeWriter = new CSVWriter(Arrays.asList("Username", "Grade"));
        for (Solution s : studentSolutions)
        {
            double grade = gradeGenerator.generateGrade(s);
            String[] line = {s.getIdentifier(), String.valueOf(grade)};
            gradeWriter.addEntry(Arrays.asList(line));
            logger.info("Grade Generated: " + Arrays.toString(line));
        }
        gradeWriter.write(Paths.get(configuration.getOutputDir() + File.separator + "AssignmentMarks.csv"));
    }

    private void writeFeedback()
    {
        // TODO implement

        for (Solution s : studentSolutions)
        {
            FileWriter file = new FileWriter();
            for (CheckProcessor checkProcessor : checkProcessors)
            {
                for (Check c : checkProcessor.getChecks())
                {
                    String feedback = c.getFeedback(s);
                    if(!feedback.isEmpty())
                        file.addLine(feedback);
                }
            }
            file.write(Paths.get(configuration.getOutputDir() + File.separator + "feedback" + File.separator + s.getIdentifier() + "_feedback.txt"));

        }
    }
}
