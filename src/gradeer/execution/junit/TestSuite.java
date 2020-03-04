package gradeer.execution.junit;

import gradeer.configuration.Configuration;
import gradeer.io.JavaSource;

import java.nio.file.Path;

public class TestSuite extends JavaSource
{
    public TestSuite(Path testSrcPath, Path testRootDir)
    {
        super(testSrcPath, testRootDir);
    }
}
