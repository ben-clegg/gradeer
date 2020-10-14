    package tech.anonymousname.gradeer.checks.checkprocessing;

import tech.anonymousname.gradeer.checks.Check;
import tech.anonymousname.gradeer.checks.CheckstyleCheck;
import tech.anonymousname.gradeer.configuration.Configuration;
import tech.anonymousname.gradeer.solution.Solution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class CheckValidator
{
    private final Collection<Solution> modelSolutions;
    private final Configuration configuration;

    public CheckValidator(Collection<Solution> modelSolutions, Configuration configuration)
    {
        this.modelSolutions = modelSolutions;
        this.configuration = configuration;
    }

    public Collection<Check> filterValidChecks(Collection<Check> checks)
    {
        Collection<String> checkIssues = new ArrayList<>();
        Collection<Check> checkPool = new ArrayList<>(checks);
        Collection<Check> validChecks = new HashSet<>();

        // Find checks that do not achieve a score of 1.0 on any model solution
        for (Solution m : modelSolutions)
        {
            Collection<Check> toRemove = new ArrayList<>();
            for(Check c : checkPool)
            {
                c.run(m);
                // Any non-perfect score is an invalid check
                if (m.getCheckResult(c).getUnweightedScore() < 1.0)
                {
                    checkIssues.add(c.identifier() + " is invalid (base score = " +
                            m.getCheckResult(c).getUnweightedScore() + ")");
                    toRemove.add(c);
                }
            }
            checkPool.removeAll(toRemove);
        }

        // Add remaining checks, report duplicates
        for (Check c : checkPool)
        {
            if(validChecks.stream().anyMatch(v -> v.identifier().equals(c.identifier())))
                checkIssues.add("There are multiple checks called " + c.identifier() + "; it is possible only one will be used if invalid checks are removed.");
            validChecks.add(c);
        }

        // Only return valid checks
        if(configuration.isRemoveInvalidChecks())
        {
            return validChecks;
        }
        // Only report invalid checks and not remove them
        return checks;
    }
}
