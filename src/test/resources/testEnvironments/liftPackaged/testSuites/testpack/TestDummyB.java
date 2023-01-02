package testpack;

import pack.*;
import org.junit.*;
import static org.junit.Assert.*;

public class TestDummyB {
	@Test(timeout = 4000)
	public void testAlwaysFails() throws Throwable {
		fail();
	}
	@Test(timeout = 4000)
	public void testAlwaysFails2() throws Throwable {
		fail();
	}
}