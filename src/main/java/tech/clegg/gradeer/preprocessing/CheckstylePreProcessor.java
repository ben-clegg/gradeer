package tech.clegg.gradeer.preprocessing;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import tech.clegg.gradeer.checks.CheckstyleCheck;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.preprocessing.staticanalysis.checkstyle.CheckstyleExecutor;
import tech.clegg.gradeer.preprocessing.staticanalysis.checkstyle.CheckstyleProcessResults;
import tech.clegg.gradeer.solution.Solution;

public class CheckstylePreProcessor extends PreProcessor
{
    public CheckstylePreProcessor(Solution solution, Configuration configuration)
    {
        super(solution, configuration);
    }

    @Override
    public void start()
    {
        CheckstyleExecutor checkstyleExecutor = new CheckstyleExecutor(getConfiguration());
        try
        {
            CheckstyleProcessResults results = checkstyleExecutor.execute(solution);
            solution.setCheckstyleProcessResults(results);
        }
        catch (CheckstyleException checkstyleException)
        {
            // Log the file
            getConfiguration().getLogFile().writeMessage("Checkstyle process error on solution " + solution.getIdentifier());
            getConfiguration().getLogFile().writeException(checkstyleException);
        }
    }

    @Override
    public void stop()
    {

    }
}
