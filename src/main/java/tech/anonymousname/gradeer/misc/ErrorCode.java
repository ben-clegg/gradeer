package tech.anonymousname.gradeer.misc;

public enum ErrorCode
{
    NONE(0),
    NO_CONFIG_FILE(10)
    ;

    private int code;

    ErrorCode(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}
