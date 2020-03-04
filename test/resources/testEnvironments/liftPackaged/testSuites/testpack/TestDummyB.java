package testpack;

import pack.*;
import org.junit.*;
import static org.junit.Assert.*;

public class TestDummyB {
	@Test(timeout = 4000)
	public void test() throws Throwable {
		fail();
	}
	@Test(timeout = 4000)
	public void test2() throws Throwable {
		fail();
	}
}