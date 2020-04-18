package tech.clegg.gradeer.runtime;

import tech.clegg.gradeer.Gradeer;
import tech.clegg.gradeer.checks.CheckProcessor;
import tech.clegg.gradeer.checks.generation.CheckstyleCheckGenerator;
import tech.clegg.gradeer.checks.generation.PMDCheckGenerator;
import tech.clegg.gradeer.checks.generation.TestSuiteCheckGenerator;
import tech.clegg.gradeer.configuration.Configuration;

import java.nio.file.Files;

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
            if(configuration.getPmdChecksJSON() != null && Files.exists(configuration.getPmdChecksJSON()))
            {
                PMDCheckGenerator pmdCheckGenerator = new PMDCheckGenerator(configuration, gradeer.getModelSolutions());
                checks.addAll(pmdCheckGenerator.getChecks());
            }
        }

        if(configuration.isCheckstyleEnabled() && configuration.getCheckstyleXml() != null)
        {

            if(configuration.getCheckstyleChecksJSON() != null && Files.exists(configuration.getCheckstyleChecksJSON()))
            {
                CheckstyleCheckGenerator checkstyleCheckGenerator = new CheckstyleCheckGenerator(configuration, gradeer.getModelSolutions());
                checks.addAll(checkstyleCheckGenerator.getChecks());
            }
        }

        if(configuration.isTestSuitesEnabled())
        {
            if(configuration.getUnittestChecksJSON() != null && Files.exists(configuration.getUnittestChecksJSON()))
            {
                TestSuiteCheckGenerator testSuiteCheckGenerator = new TestSuiteCheckGenerator(configuration, gradeer.getModelSolutions());
                checks.addAll(testSuiteCheckGenerator.getChecks());
            }
        }
    }

    @Override
    public CheckProcessor run()
    {
        return new CheckProcessor(checks, configuration);
    }
}
