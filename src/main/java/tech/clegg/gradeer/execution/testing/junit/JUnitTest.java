package tech.clegg.gradeer.execution.testing.junit;

import org.junit.runner.Description;
import tech.clegg.gradeer.execution.testing.UnitTest;

import java.util.Objects;

public class JUnitTest implements UnitTest
{
    private String fullyQualifiedClassName;
    private String methodName;

    public JUnitTest(JUnitTestSource testSource, String methodName)
    {
        this.fullyQualifiedClassName = testSource.getComplexClassName();
        this.methodName = methodName;
    }

    public JUnitTest(Description junitDescription)
    {
        this.fullyQualifiedClassName = junitDescription.getClassName();
        this.methodName = junitDescription.getMethodName();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JUnitTest other = (JUnitTest) o;
        return Objects.equals(fullyQualifiedClassName, other.fullyQualifiedClassName) && Objects.equals(methodName, other.methodName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(fullyQualifiedClassName, methodName);
    }

    @Override
    public String toString()
    {
        return fullyQualifiedClassName + "::" + methodName;
    }
}
