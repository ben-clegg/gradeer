package tech.anonymousname.gradeer.execution.junit;

import tech.anonymousname.gradeer.subject.JavaSource;

import java.nio.file.Path;

public class TestSuite extends JavaSource
{
    public TestSuite(Path testSrcPath, Path testRootDir)
    {
        super(testSrcPath, testRootDir);
    }
}
