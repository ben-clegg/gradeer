package testpack;

import pack.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

public class TestDummyC {
	@DisplayName("Display name for testAlwaysFails")
	@Timeout(4)
	@Test
	public void testAlwaysFails() throws Throwable {
		fail();
	}
	@DisplayName("Display name for test2")
	@Timeout(4)
	@Test
	public void test2() throws Throwable {
		return;
	}
}