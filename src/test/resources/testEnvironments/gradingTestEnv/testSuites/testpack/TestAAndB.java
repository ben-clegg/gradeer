package testpack;

import task.ExampleTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

public class TestAAndB {
    @Timeout(4)
    @Test
    public void testA() throws Throwable {
        ExampleTask t = new ExampleTask();
        assertEquals(4, t.resultA);
    }
    @Timeout(4)
    @Test
    public void testB() throws Throwable {
        ExampleTask t = new ExampleTask();
        assertEquals(3, t.resultB);
    }
}