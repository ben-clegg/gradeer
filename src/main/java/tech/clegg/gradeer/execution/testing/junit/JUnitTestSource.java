package tech.clegg.gradeer.execution.testing.junit;

import tech.clegg.gradeer.input.TestSourceFile;
import tech.clegg.gradeer.subject.JavaSource;

import java.nio.file.Path;

public class JUnitTestSource extends JavaSource implements TestSourceFile
{
    public JUnitTestSource(Path testSrcPath, Path testRootDir)
    {
        super(testSrcPath, testRootDir);
    }
}
