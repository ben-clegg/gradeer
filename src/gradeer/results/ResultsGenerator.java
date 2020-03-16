package gradeer.results;

import gradeer.checks.Check;
import gradeer.checks.CheckExecutor;
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
    private CheckExecutor checkExecutor;
    private Configuration configuration;

    public ResultsGenerator(Collection<Solution> studentSolutions, CheckExecutor checkExecutor, Configuration configuration)
    {
        this.studentSolutions = studentSolutions;
        this.checkExecutor = checkExecutor;
        this.configuration = configuration;
    }

    @Override
    public void run()
    {
        studentSolutions.forEach(s -> checkExecutor.runChecks(s));

        writeGrades();
        writeFeedback();
    }

    private void writeGrades()
    {
        GradeGenerator gradeGenerator = new GradeGenerator(checkExecutor);
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
