package tech.clegg.gradeer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestGlobals
{
    public static final Path JSON_CONFIG = Paths.get(System.getProperty("user.dir") + File.separator +
            "test" + File.separator +
            "resources" + File.separator +
            "testEnvironments" + File.separator +
            "liftPackaged" + File.separator +
            "testConfig.json");
}
