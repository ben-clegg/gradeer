package task;
public class ExampleTask
{
    public int resultA = 0;
    public int resultB = 0;

    public static void main(String[] args) {
        new ExampleTask();
    }

    public ExampleTask()
    {
        methodA();
        methodB();
    }

    public void methodA()
    {
        resultA = 1;
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