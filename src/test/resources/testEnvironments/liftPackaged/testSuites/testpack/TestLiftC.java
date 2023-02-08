package testpack;

import pack.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

public class TestLiftC {
	@DisplayName("Display name for test")
	@Timeout(4)
	@Test
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