package tech.clegg.gradeer.execution.testing;

import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.input.TestSourceFile;
import tech.clegg.gradeer.solution.Solution;

import java.util.Collection;

public abstract class TestEngine
{
    private Configuration configuration;

    public TestEngine(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Execute every test that can be executed by the current TestEngine on the target solution
     * @param solutionUnderTest the solution to run tests on
     */
    public void execute(Solution solutionUnderTest)
    {
        // Retrieve test sources that match the current engine and execute each on the solution
        Collection<TestSourceFile> testSources = configuration.getTestSourceFilesMap().get(this.getClass());
        execute(solutionUnderTest, testSources);
    }

    private void execute(Solution solutionUnderTest, Collection<TestSourceFile> testSources)
    {
        for (TestSourceFile testSourceFile : testSources)
        {
            execute(solutionUnderTest, testSourceFile);
        }
    }

    protected abstract void execute(Solution solutionUnderTest, TestSourceFile testSourceFile);

    protected Configuration getConfiguration()
    {
        return configuration;
    }

}
