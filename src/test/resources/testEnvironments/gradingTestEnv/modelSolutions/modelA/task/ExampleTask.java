package task;
public class ExampleTask
{
    public int resultA = 0;
    public int resultB = 0;

    public ExampleTask()
    {
        methodA();
        methodB();
        System.out.println(resultA);
        System.out.println(resultB);
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