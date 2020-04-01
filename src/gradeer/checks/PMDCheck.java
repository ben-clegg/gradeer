package gradeer.checks;

import gradeer.execution.staticanalysis.pmd.PMDViolation;
import gradeer.solution.Solution;

import java.util.Collection;
import java.util.Optional;

public class PMDCheck extends Check
{

    private int maximumViolations = 5;
    private int minimumViolations = 0;

    public PMDCheck(String name, double weight)
    {
        super();
        this.name = name;
        this.feedback = ""; // Feedback will be automatically loaded from PMD violation descriptions
        this.weight = weight;
    }

    @Override
    public void run(Solution solution)
    {
        if(solution.getPmdProcessResults() == null)
            return;

        Collection<PMDViolation> violations = solution.getPmdProcessResults().getViolations(name);

        if(violations == null || violations.isEmpty())
        {
            unweightedScores.put(solution, 1.0);
            return;
        }

        int totalTrackedViolations = violations.size();

        // Derive score from maximum & minimum violations
        if(totalTrackedViolations >= maximumViolations)
        {
            unweightedScores.put(solution, 0.0);
            return;
        }
        if(totalTrackedViolations <= minimumViolations)
        {
            unweightedScores.put(solution, 1.0);
            return;
        }

        totalTrackedViolations -= minimumViolations;
        double score = 1.0 - (double) totalTrackedViolations / (maximumViolations - minimumViolations);
        unweightedScores.put(solution, score);

    }

    private void updateFeedback(Collection<PMDViolation> pmdViolations)
    {
        Optional<PMDViolation> violation = pmdViolations.stream()
                .filter(v -> v.getRule().toLowerCase().equals(this.name.toLowerCase())).findFirst();
        if(!violation.isPresent())
            return;
        feedback = violation.get().getDescription();
    }
}
