package testpack;

import task.ExampleTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

public class TestMethodA {
    @Timeout(4)
    @Test
    public void testMethodA() throws Throwable {
        ExampleTask t = new ExampleTask();
        assertEquals(4, t.resultA);
        return;
    }
}