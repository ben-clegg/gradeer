package gradeer.execution;

/*
 * Copyright (C) 2016-2019 Code Defenders contributors
 *
 * This file is part of Code Defenders.
 *
 * Code Defenders is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Code Defenders is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Code Defenders. If not, see <http://www.gnu.org/licenses/>.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jose Rojas
 * Original : https://github.com/CodeDefenders/CodeDefenders/blob/master/src/main/java/org/codedefenders/execution/AntProcessResult.java
 */
public class AntProcessResult
{

    private static final Logger logger = LogManager.getLogger(AntProcessResult.class);

    private String inputStreamText = "";
    private String errorStreamText = "";
    private String exceptionText = "";
    private String compilerOutput = "";
    private String testOutput = "";
    private boolean compiled;
    private boolean hasFailure;
    private boolean hasError;

    private int testsRun = 0;
    private int testsFailures = 0;
    private int testsErrors = 0;
    private int testsSkipped = 0;

    public static final String COMPILER_PREFIX = "[javac] ";
    public static final String TEST_PREFIX = "[junit] ";
    public static final String JUNIT_RESULT_REGEX = "Tests run: (\\d+), Failures: (\\d+), Errors: (\\d+), Skipped: (\\d+), Time elapsed: ((\\d+)(\\.\\d+)?) sec";

    void setInputStream(BufferedReader reader) {
        StringBuilder isLog = new StringBuilder();
        StringBuilder compilerOutputBuilder = new StringBuilder();
        final String COMPILER_PREFIX = "[javac] ";

        StringBuilder testOutputBuilder = new StringBuilder();
        final String TEST_PREFIX = "[junit] ";
        final String JUNIT_RESULT_REGEX = "Tests run: (\\d+), Failures: (\\d+), Errors: (\\d+), Skipped: (\\d+), Time elapsed: ((\\d+)(\\.\\d+)?) sec";

        String line;
        try {
            Pattern pattern = Pattern.compile(JUNIT_RESULT_REGEX, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                isLog.append(line).append(System.lineSeparator());

                if (line.startsWith(COMPILER_PREFIX)) {
                    compilerOutputBuilder.append(line).append(System.lineSeparator());
                } else if (line.startsWith(TEST_PREFIX)) {
                    testOutputBuilder.append(line).append(System.lineSeparator());
                    Matcher m = pattern.matcher(line);
                    if (m.find()) {
                        hasFailure = Integer.parseInt(m.group(2)) > 0;
                        hasError = Integer.parseInt(m.group(3)) > 0;

                        testsRun = Integer.parseInt(m.group(1));
                        testsFailures = Integer.parseInt(m.group(2));
                        testsErrors = Integer.parseInt(m.group(3));
                        testsSkipped = Integer.parseInt(m.group(4));
                    }
                } else if (line.equalsIgnoreCase("BUILD SUCCESSFUL"))
                    compiled = true;
            }
            compilerOutput = sanitize(compilerOutputBuilder.toString());
            testOutput = testOutputBuilder.toString();
            inputStreamText = isLog.toString();
        } catch (IOException e) {
            logger.error("Error while reading input stream", e);
        }
    }

    /**
     * Sanitize the compiler gradeer.output by identifying the gradeer.output folder and
     * removing it from the compiler gradeer.output before sending it over the clients.
     *
     * @param fullCompilerOutput
     * @return
     */
    private String sanitize(String fullCompilerOutput) {
        String outputFolder = null; // This is what we shall remove from the log
        StringBuffer sanitized = new StringBuffer();
        for (String line : fullCompilerOutput.split("\n")) {
            // This might not work with dependencies, but we should always
            // mutate one file at time, right?
            if (line.startsWith("[javac] Compiling 1 source file to ")) {
                // Get the value of the gradeer.output folder
                outputFolder = line.replace("[javac] Compiling 1 source file to ", "");
            } else {
                if (outputFolder != null && line.contains(outputFolder)) {
                    line = line.replace(outputFolder + File.separator, "");
                }
                // Pass it along the gradeer.output
                sanitized.append(line).append("\n");
            }
        }
        return sanitized.toString();
    }

    void setErrorStreamText(String errorStreamText) {
        this.errorStreamText = errorStreamText;
    }

    void setExceptionText(String exceptionText) {
        this.exceptionText = exceptionText;
    }

    public boolean compiled() {
        return compiled;
    }

    public boolean hasFailure() {
        return hasFailure;
    }

    public boolean hasError() {
        return hasError;
    }

    public String getCompilerOutput() {
        return compilerOutput;
    }

    public String getJUnitMessage() {
        return testOutput;
    }

    public String getErrorMessage() {
        return inputStreamText + " " + errorStreamText + " " + exceptionText;
    }

    public int getTestsRun()
    {
        return testsRun;
    }

    public int getTestsFailures()
    {
        return testsFailures;
    }

    public int getTestsErrors()
    {
        return testsErrors;
    }

    public int getTestsSkipped()
    {
        return testsSkipped;
    }

    @Override
    public String toString() {
        return "AntProcessResult{" +
                "inputStreamText='" + inputStreamText + '\'' +
                ", errorStreamText='" + errorStreamText + '\'' +
                ", exceptionText='" + exceptionText + '\'' +
                ", compilerOutput='" + compilerOutput + '\'' +
                ", testOutput='" + testOutput + '\'' +
                '}';
    }

}
