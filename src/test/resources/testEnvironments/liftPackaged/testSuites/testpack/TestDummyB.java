package testpack;

import pack.*;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TestDummyB {
	@Timeout(4)
	@Test
	public void test_Always_Fails() throws Throwable {
		fail();
	}
	@Timeout(4)
	@Test
	public void test_Always_Fails2() throws Throwable {
		fail();
	}
}