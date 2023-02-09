package testpack;

import pack.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

public class TestLiftA {
	@DisplayName("Display name for test")
	@Timeout(4)
	@Test
	public void test() throws Throwable {
		Lift lift = new Lift(10);
		assertEquals(10, lift.getTopFloor());
	}
	@DisplayName("Display name for test2")
	@Timeout(4)
	@Test
	public void test2() throws Throwable {
		Lift test = new Lift(5);
		assertEquals(5, test.getTopFloor());
	}
}