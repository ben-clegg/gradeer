package tech.clegg.gradeer.compilation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class JavaCompilerResult {

    private boolean completed;
    private List<String> errorOutput = new ArrayList<>();
    private List<String> standardOutput = new ArrayList<>();

    public JavaCompilerResult(Exception e) {
        completed = false;
        addExceptionToErrorOutput(e);
    }

    public JavaCompilerResult(Process javacProcess) {
        this(javacProcess, 60);
    }

    public JavaCompilerResult(Process javacProcess, int timeoutSeconds) {
        try {
            completed = javacProcess.waitFor(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            completed = false;
            addExceptionToErrorOutput(e);
        }
        errorOutput.addAll(getLinesFromInputStream(javacProcess.getErrorStream()));
        standardOutput.addAll(getLinesFromInputStream(javacProcess.getInputStream())); // std out
    }

    private void addExceptionToErrorOutput(Exception e) {
        errorOutput.addAll(Arrays.stream(
                e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList()));
        errorOutput.add(e.getMessage());
    }

    private List<String> getLinesFromInputStream(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.toList());
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean hasError() {
        return !errorOutput.isEmpty() && errorOutput.stream().anyMatch(line -> !line.trim().isBlank());
    }

    public List<String> getErrorOutput() {
        return errorOutput;
    }

    public List<String> getStandardOutput() {
        return standardOutput;
    }
}
