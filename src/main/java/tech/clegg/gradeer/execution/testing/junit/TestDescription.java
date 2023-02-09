package tech.clegg.gradeer.execution.testing.junit;

public class TestDescription {

    private String className = "";
    private String methodName  = "";
    private String displayName = "";

    public TestDescription() {}

    public TestDescription(String className, String methodName, String displayName) {
        this.className = className;
        this.methodName = methodName;
        this.displayName = displayName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setClassName(Class<?> className) {
        this.className = className.getName();
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        String str = className + "." + methodName;
        if ( ! displayName.equals(methodName)) {
            str += " - " + displayName;
        }
        return str;
    }
}