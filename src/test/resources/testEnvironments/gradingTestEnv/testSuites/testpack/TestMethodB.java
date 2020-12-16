package testpack;

import task.ExampleTask;
import org.junit.*;
import static org.junit.Assert.*;

public class TestMethodB {
    @Test(timeout = 4000)
    public void testMethodB() throws Throwable {
        ExampleTask t = new ExampleTask();
        assertEquals(3, t.resultB);
        return;
    }
}