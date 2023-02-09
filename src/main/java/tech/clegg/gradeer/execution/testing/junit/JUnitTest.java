package tech.clegg.gradeer.execution.testing.junit;

import tech.clegg.gradeer.execution.testing.UnitTest;

import java.util.Objects;

public class JUnitTest implements UnitTest
{
    private String fullyQualifiedClassName;
    private String methodName;
    private String displayName;

    public JUnitTest(JUnitTestSource testSource, String methodName)
    {
        this.fullyQualifiedClassName = testSource.getComplexClassName();
        this.methodName = methodName;
        this.displayName = "";
    }

    public JUnitTest(JUnitTestSource testSource, String methodName, String displayName)
    {
        this.fullyQualifiedClassName = testSource.getComplexClassName();
        this.methodName = methodName;
        this.displayName = displayName;
    }

    public JUnitTest(TestDescription testDescription)
    {
        this.fullyQualifiedClassName = testDescription.getClassName();
        this.methodName = testDescription.getMethodName();
        this.displayName = testDescription.getDisplayName();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JUnitTest other = (JUnitTest) o;
        return Objects.equals(fullyQualifiedClassName, other.fullyQualifiedClassName) && Objects.equals(methodName, other.methodName) && Objects.equals(displayName, other.displayName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(fullyQualifiedClassName, methodName, displayName);
    }

    @Override
    public String toString()
    {
        return fullyQualifiedClassName + "::" + methodName + ((displayName.isEmpty() || displayName.equals(methodName)) ? "" : "::" + displayName);
    }
}
