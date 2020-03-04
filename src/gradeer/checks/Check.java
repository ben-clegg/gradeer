package gradeer.checks;

import gradeer.solution.Solution;

import java.util.HashMap;
import java.util.Map;

public abstract class Check
{
    protected double weight = 1.0;
    protected String name;
    protected Map<Solution, Double> unweightedScores = new HashMap<>();

    public abstract void run(Solution solution);

    public double getWeightedScore(Solution solution)
    {
        return weight * getUnweightedScore(solution);
    }

    public double getUnweightedScore(Solution solution)
    {
        return unweightedScores.get(solution);
    }

    public void setWeight(double weight)
    {
        this.weight = weight;
    }

    public double getWeight()
    {
        return weight;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "Check{" +
                "weight=" + weight +
                ", name='" + name +
                '}';
    }
}
