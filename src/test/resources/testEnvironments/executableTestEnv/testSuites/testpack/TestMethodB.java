package testpack;

import task.ExampleTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

public class TestMethodB {
    @Timeout(4)
    @Test
    public void testMethodB() throws Throwable {
        ExampleTask t = new ExampleTask();
        assertEquals(3, t.resultB);
        return;
    }
}