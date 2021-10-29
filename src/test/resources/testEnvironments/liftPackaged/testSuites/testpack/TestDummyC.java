package testpack;

import pack.*;
import org.junit.*;
import static org.junit.Assert.*;

public class TestDummyC {
	@Test(timeout = 4000)
	public void testAlwaysFails() throws Throwable {
		fail();
	}
	@Test(timeout = 4000)
	public void test2() throws Throwable {
		return;
	}
}