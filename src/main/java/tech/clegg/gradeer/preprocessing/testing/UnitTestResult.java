package tech.clegg.gradeer.preprocessing.testing;

import tech.clegg.gradeer.execution.testing.UnitTest;

public class UnitTestResult
{
    private final UnitTest unitTest;
    private final UnitTestResultFlag resultFlag;
    private final String message;

    public UnitTestResult(UnitTest unitTest, UnitTestResultFlag resultFlag, String message)
    {
        this.unitTest = unitTest;
        this.resultFlag = resultFlag;
        this.message = message;
    }

    public UnitTestResult(UnitTest unitTest, UnitTestResultFlag resultFlag)
    {
        this(unitTest, resultFlag, "");
    }

    public UnitTest getUnitTest()
    {
        return unitTest;
    }

    public UnitTestResultFlag getResultFlag()
    {
        return resultFlag;
    }

    public String getMessage()
    {
        return message;
    }

    public enum UnitTestResultFlag
    {
        PASS,
        ERROR,
        FAIL,
        TIMEOUT,
        PENDING
    }
}
