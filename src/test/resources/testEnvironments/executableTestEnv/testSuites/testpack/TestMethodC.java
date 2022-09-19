package testpack;

import task.ExampleTask;
import org.junit.*;
import static org.junit.Assert.*;

public class TestMethodC {
    @Test(timeout = 4000)
    public void testMethodC() throws Throwable {
        ExampleTask t = new ExampleTask();
        assertEquals(7, t.methodC());
        return;
    }
}