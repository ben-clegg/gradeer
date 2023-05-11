package task;
public class ExampleTaskWithArg {
    public String input = "";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Argument expected");
        }
        String a = args[0];
        System.out.println("Running ExampleTaskWithArg:" + a);
        new ExampleTaskWithArg(a);
    }

    public ExampleTaskWithArg(String a) {
        foo(a);
        System.out.println("input:" + input);
    }

    public void foo(String a) {
        input = a;
    }

}