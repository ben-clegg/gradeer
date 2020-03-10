package gradeer.checks.generation;

import gradeer.configuration.Configuration;
import gradeer.solution.Solution;

import java.util.Collection;

public class CheckstyleCheckGenerator extends CheckGenerator
{
    public CheckstyleCheckGenerator(Configuration configuration, Collection<Solution> modelSolutions)
    {
        super(configuration, modelSolutions);
    }

    @Override
    void generate()
    {
        // TODO Parse XML for module names
        // TODO Make check for each module name

    }

    @Override
    void setWeights()
    {

    }
}
