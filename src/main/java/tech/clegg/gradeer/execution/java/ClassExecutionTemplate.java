package tech.clegg.gradeer.execution.java;

public class ClassExecutionTemplate
{
    private String fullClassName;
    private String[] args;
    private String[] additionalCPElems;
    private int waitAfterExecutionTime = 0;

    public String getFullClassName()
    {
        return fullClassName;
    }

    public void setFullClassName(String fullClassName)
    {
        this.fullClassName = fullClassName;
    }

    public String[] getArgs()
    {
        return args;
    }

    public void setArgs(String[] args)
    {
        this.args = args;
    }

    public String[] getAdditionalCPElems()
    {
        return additionalCPElems;
    }

    public void setAdditionalCPElems(String[] additionalCPElems)
    {
        this.additionalCPElems = additionalCPElems;
    }

    public int getWaitAfterExecutionTime()
    {
        return waitAfterExecutionTime;
    }
}
