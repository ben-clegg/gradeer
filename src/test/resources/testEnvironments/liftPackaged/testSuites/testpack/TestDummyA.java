package testpack;

import pack.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

public class TestDummyA {
	@DisplayName("Display name for test")
	@Timeout(4)
	@Test
	public void test() throws Throwable {
		return;
	}
	@DisplayName("Display name for test2")
	@Timeout(4)
	@Test
	public void test2() throws Throwable {
		return;
	}
}