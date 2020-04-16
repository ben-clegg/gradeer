package gradeer.runtime;

import gradeer.Gradeer;
import gradeer.auxiliaryprocesses.MergedSolutionWriter;
import gradeer.checks.CheckProcessor;
import gradeer.checks.generation.CheckstyleCheckGenerator;
import gradeer.checks.generation.PMDCheckGenerator;
import gradeer.checks.generation.TestSuiteCheckGenerator;
import gradeer.configuration.Configuration;
import gradeer.results.ResultsGenerator;

public class AutogradingRuntime extends Runtime
{
    public AutogradingRuntime(Gradeer gradeer, Configuration configuration)
    {
        super(gradeer, configuration);
    }

    @Override
    protected void loadChecks()
    {
        if(configuration.isPmdEnabled())
        {
            PMDCheckGenerator pmdCheckGenerator = new PMDCheckGenerator(configuration, gradeer.getModelSolutions());
            checks.addAll(pmdCheckGenerator.getChecks());
        }

        if(configuration.isCheckstyleEnabled() && configuration.getCheckstyleXml() != null)
        {
            CheckstyleCheckGenerator checkstyleCheckGenerator = new CheckstyleCheckGenerator(configuration, gradeer.getModelSolutions());
            checks.addAll(checkstyleCheckGenerator.getChecks());
        }

        if(configuration.isTestSuitesEnabled())
        {
            TestSuiteCheckGenerator testSuiteCheckGenerator = new TestSuiteCheckGenerator(configuration, gradeer.getModelSolutions());
            checks.addAll(testSuiteCheckGenerator.getChecks());
        }
    }

    @Override
    public CheckProcessor run()
    {
        return new CheckProcessor(checks, configuration);
    }
}
