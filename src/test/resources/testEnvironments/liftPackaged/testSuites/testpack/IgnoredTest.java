package testpack;

import pack.*;
import org.junit.*;
import static org.junit.Assert.*;

public class IgnoredTest {
	@Test(timeout = 4000)
	public void test() throws Throwable {
		return;
	}
	@Test(timeout = 4000)
	public void test2() throws Throwable {
		return;
	}
}