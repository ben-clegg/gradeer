package testpack;

import pack.*;
import org.junit.*;
import static org.junit.Assert.*;

public class TestLiftC {
	@Test(timeout = 4000)
	public void test() throws Throwable {
		// test here!
		Lift lift = new Lift(4);
		assertEquals(0, lift.getNumRiders());
		lift.addRiders(2);
		assertEquals(2, lift.getNumRiders());
		// Full
		lift.addRiders(100);
		assertEquals(10, lift.getNumRiders());
	}
}