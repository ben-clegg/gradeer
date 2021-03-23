package tech.clegg.gradeer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestGlobals
{
    public static final Path JSON_CONFIG_LIFT = Paths.get(System.getProperty("user.dir") + File.separator +
            "src" + File.separator +
            "test" + File.separator +
            "resources" + File.separator +
            "testEnvironments" + File.separator +
            "liftPackaged" + File.separator +
            "gconfig-liftpackaged.json");

    public static final Path JSON_CONFIG_GRADING_TEST_ENV = Paths.get(System.getProperty("user.dir") + File.separator +
            "src" + File.separator +
            "test" + File.separator +
            "resources" + File.separator +
            "testEnvironments" + File.separator +
            "gradingTestEnv" + File.separator +
            "gconfig-auto.json");
}
