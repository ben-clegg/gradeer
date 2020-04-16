package gradeer.results;

import gradeer.checks.Check;
import gradeer.checks.CheckProcessor;
import gradeer.configuration.Configuration;
import gradeer.results.io.CSVWriter;
import gradeer.results.io.FileWriter;
import gradeer.solution.Solution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

public class ResultsGenerator implements Runnable
{
    private static final Logger logger = LogManager.getLogger(ResultsGenerator.class);

    private final Collection<Solution> studentSolutions;
    private CheckProcessor checkProcessor;
    private Configuration configuration;

    public ResultsGenerator(Collection<Solution> studentSolutions, CheckProcessor checkProcessor, Configuration configuration)
    {
        this.studentSolutions = studentSolutions;
        this.checkProcessor = checkProcessor;
        this.configuration = configuration;
    }

    @Override
    public void run()
    {
        studentSolutions.forEach(s -> checkProcessor.runChecks(s));

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
            for (Check c : checkProcessor.getChecks())
            {
                StringBuilder sb = new StringBuilder();
                sb.append(c.getClass().getName());
                sb.append(" - ");
                sb.append(c.getName());
                sb.append(": ");
                sb.append(c.getWeightedScore(s));
                sb.append(" / ");
                sb.append(c.getWeight());

                f.addLine(sb.toString());
            }
            f.write(Paths.get(configuration.getCheckResultsDir() + File.separator + s.getIdentifier()));
        }
    }

    private void writeGrades()
    {
        GradeGenerator gradeGenerator = new GradeGenerator(checkProcessor);
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
            for (Check c : checkProcessor.getChecks())
            {
                String feedback = c.getFeedback(s);
                if(!feedback.isEmpty())
                    file.addLine(feedback);
            }
            file.write(Paths.get(configuration.getOutputDir() + File.separator + "feedback" + File.separator + s.getIdentifier() + "_feedback.txt"));

        }
    }
}
