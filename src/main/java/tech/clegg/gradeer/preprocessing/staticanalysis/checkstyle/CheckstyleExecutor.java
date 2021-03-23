package tech.clegg.gradeer.preprocessing.staticanalysis.checkstyle;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.solution.Solution;

public class CheckstyleExecutor
{
    private Configuration configuration;

    public CheckstyleExecutor(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public CheckstyleProcessResults execute(Solution solution) throws CheckstyleException
    {
        CheckstyleProcess checkstyleProcess = new CheckstyleProcess(solution, configuration);
        checkstyleProcess.run();
        return checkstyleProcess.getResults();
    }
}

