package tech.clegg.gradeer.preprocessing.testing;

import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.execution.testing.TestEngine;
import tech.clegg.gradeer.input.TestSourceFile;
import tech.clegg.gradeer.preprocessing.PreProcessor;
import tech.clegg.gradeer.solution.Solution;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

public class UnitTestPreProcessor extends PreProcessor
{
    public UnitTestPreProcessor(Solution solution, Configuration configuration)
    {
        super(solution, configuration);
    }

    @Override
    public void start()
    {
        // Run every test engine on the solution
        Map<Class<? extends TestEngine>, Collection<TestSourceFile>> testSourceFilesMap =
                getConfiguration().getTestSourceFilesMap();
        testSourceFilesMap.entrySet().forEach(et -> {
            try
            {
                TestEngine testEngine = et.getKey().getConstructor(Configuration.class).newInstance(getConfiguration());
                testEngine.execute(getSolution());

            } catch (InstantiationException |
                    IllegalAccessException |
                    InvocationTargetException |
                    NoSuchMethodException instantiationException)
            {
                instantiationException.printStackTrace();
            }
        });
    }

    @Override
    public void stop()
    {
        // Should be able to safely ignore
    }
}
