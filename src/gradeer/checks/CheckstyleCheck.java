package gradeer.checks;

import gradeer.solution.Solution;

public class CheckstyleCheck extends Check
{
    public CheckstyleCheck(String name, String feedback, double weight)
    {
        super();

        this.name = name;
        this.feedback = feedback;
        setWeight(weight);
        // TODO load name
    }

    @Override
    public void run(Solution solution)
    {

    }

    @Override
    public String toString()
    {
        return "CheckstyleCheck{" +
                "name='" + name + "'" +
                ", feedback='" + feedback + "'" +
                ", weight=" + weight +
                '}';
    }
}
