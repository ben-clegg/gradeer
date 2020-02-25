package gradeer.execution.junit;

import java.nio.file.Path;
import java.util.Objects;

public class UnitTest
{
    Path srcPath;

    public UnitTest(Path testSrcPath)
    {
        srcPath = testSrcPath;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnitTest unitTest = (UnitTest) o;
        return Objects.equals(srcPath, unitTest.srcPath);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(srcPath);
    }
}
