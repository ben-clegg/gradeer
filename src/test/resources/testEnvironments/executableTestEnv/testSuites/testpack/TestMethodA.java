package testpack;

import task.ExampleTask;
import org.junit.*;
import static org.junit.Assert.*;

public class TestMethodA {
    @Test(timeout = 4000)
    public void testMethodA() throws Throwable {
        ExampleTask t = new ExampleTask();
        assertEquals(4, t.resultA);
        return;
    }
}