package task;
public class ExampleTask
{
    public int resultA = 0;
    public int resultB = 0;

    public static void main(String[] args) {
        System.out.println("Running ExampleTask");
        new ExampleTask();
    }

    public ExampleTask()
    {
        methodA();
        methodB();
        System.out.println("resultA: " + resultA);
        System.out.println("resultB: " + resultB);
    }

    public void methodA()
    {
        resultA = 4;
    }

    public void methodB()
    {
        resultB = 3;
    }

    public int methodC()
    {
        return resultA + resultB;
    }
}