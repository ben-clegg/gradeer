package gradeer.checks;

import gradeer.solution.Solution;

public abstract class Check
{
    protected double weight = 1.0;

    abstract double run(Solution solution);

    public double getWeight()
    {
        return weight;
    }
}
