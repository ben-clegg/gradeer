package testpack;

import task.ExampleTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

public class TestMethodC {
    @Timeout(4)
    @Test
    public void testMethodC() throws Throwable {
        ExampleTask t = new ExampleTask();
        assertEquals(7, t.methodC());
        return;
    }
}