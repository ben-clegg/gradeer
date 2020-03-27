package gradeer.results;

import gradeer.checks.CheckProcessor;
import gradeer.configuration.Configuration;
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

        writeGrades();
        writeFeedback();
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
    }
}
