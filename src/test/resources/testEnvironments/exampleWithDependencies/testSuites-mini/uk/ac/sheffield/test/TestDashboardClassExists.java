package uk.ac.sheffield.test;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestDashboardClassExists  {

    @Test(timeout = 5000)
    public void testAppClassExists() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("uk.ac.sheffield.Dashboard");
        assertNotNull(clazz);
    }
}

