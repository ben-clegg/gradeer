package tech.clegg.gradeer.error;

public enum ErrorCode
{
    NONE(0),
    NO_CONFIG_FILE(10),
    HELP_DISPLAYED(20),
    MUTANTS_UNDETECTED(100)
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
