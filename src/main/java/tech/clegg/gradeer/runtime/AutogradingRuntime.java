package tech.clegg.gradeer.runtime;

import tech.clegg.gradeer.Gradeer;
import tech.clegg.gradeer.checks.CheckProcessor;
import tech.clegg.gradeer.checks.generation.CheckstyleCheckGenerator;
import tech.clegg.gradeer.checks.generation.PMDCheckGenerator;
import tech.clegg.gradeer.checks.generation.TestSuiteCheckGenerator;
import tech.clegg.gradeer.configuration.Configuration;

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
