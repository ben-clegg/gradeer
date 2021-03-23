package testpack;

import pack.*;
import org.junit.*;
import static org.junit.Assert.*;

public class TestLiftA {
	@Test(timeout = 4000)
	public void test() throws Throwable {
		Lift lift = new Lift(10);
		assertEquals(10, lift.getTopFloor());
	}
	@Test(timeout = 4000)
	public void test2() throws Throwable {
		Lift test = new Lift(5);
		assertEquals(5, test.getTopFloor());
	}
}