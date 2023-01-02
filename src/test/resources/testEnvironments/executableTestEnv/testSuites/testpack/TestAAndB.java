package testpack;

import task.ExampleTask;
import org.junit.*;
import static org.junit.Assert.*;

public class TestAAndB {
    @Test(timeout = 4000)
    public void testA() throws Throwable {
        ExampleTask t = new ExampleTask();
        assertEquals(4, t.resultA);
    }
    @Test(timeout = 4000)
    public void testB() throws Throwable {
        ExampleTask t = new ExampleTask();
        assertEquals(3, t.resultB);
    }
}