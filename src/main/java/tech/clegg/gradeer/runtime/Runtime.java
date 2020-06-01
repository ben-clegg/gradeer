package tech.clegg.gradeer.runtime;

import tech.clegg.gradeer.Gradeer;
import tech.clegg.gradeer.checks.Check;
import tech.clegg.gradeer.checks.checkprocessing.AutoCheckProcessor;
import tech.clegg.gradeer.checks.checkprocessing.CheckProcessor;
import tech.clegg.gradeer.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collection;

public abstract class Runtime
{
    protected Gradeer gradeer;
    protected Configuration configuration;
    protected Collection<Check> checks;

    public Runtime(Gradeer gradeer, Configuration configuration)
    {
        this.gradeer = gradeer;
        this.configuration = configuration;
        this.checks = new ArrayList<>();

        loadChecks();
    }

    protected abstract void loadChecks();

    public Collection<Check> getChecks()
    {
        return checks;
    }

    public void addCheck(Check c)
    {
        checks.add(c);
    }

    public abstract CheckProcessor run();
}
