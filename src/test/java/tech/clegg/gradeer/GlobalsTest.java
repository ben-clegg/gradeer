package tech.clegg.gradeer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class GlobalsTest
{
    public static final Path JSON_CONFIG_DEPENDENCIES = Paths.get(System.getProperty("user.dir") + File.separator +
            "src" + File.separator +
            "test" + File.separator +
            "resources" + File.separator +
            "testEnvironments" + File.separator +
            "exampleWithDependencies" + File.separator +
            "gconfig-manual.json");

    public static final Path JSON_CONFIG_LIFT = Paths.get(System.getProperty("user.dir") + File.separator +
            "src" + File.separator +
            "test" + File.separator +
            "resources" + File.separator +
            "testEnvironments" + File.separator +
            "liftPackaged" + File.separator +
            "gconfig-liftpackaged.json");

    public static final Path JSON_CONFIG_LIFT_JUNIT4 = Paths.get(System.getProperty("user.dir") + File.separator +
            "src" + File.separator +
            "test" + File.separator +
            "resources" + File.separator +
            "testEnvironments" + File.separator +
            "liftPackaged" + File.separator +
            "gconfig-liftpackaged-junit4.json");

    public static final Path JSON_CONFIG_PS = Paths.get(System.getProperty("user.dir") + File.separator +
            "src" + File.separator +
            "test" + File.separator +
            "resources" + File.separator +
            "testEnvironments" + File.separator +
            "PS" + File.separator +
            "getStuck_gconfig-JUNIT.json");

    public static final Path JSON_CONFIG_GRADING_TEST_ENV = Paths.get(System.getProperty("user.dir") + File.separator +
            "src" + File.separator +
            "test" + File.separator +
            "resources" + File.separator +
            "testEnvironments" + File.separator +
            "gradingTestEnv" + File.separator +
            "gconfig-auto.json");

    public static final Path JSON_CONFIG_EXECUTABLE_TEST_ENV = Paths.get(System.getProperty("user.dir") + File.separator + "src" + File.separator +
            "test" + File.separator +
            "resources" + File.separator +
            "testEnvironments" + File.separator +
            "executableTestEnv" + File.separator +
            "gconfig-auto.json");

    public static void deleteOutputDir(Path configJSONLocation) {
        Path outputDir = Paths.get(configJSONLocation.getParent() + File.separator + "output");
        if (Files.isDirectory(outputDir)) {
            try (Stream<Path> walk = Files.walk(outputDir)) {
                walk
                        .sorted(Comparator.reverseOrder())
                        .forEach(path ->
                        {
                            try
                            {
                                Files.delete(path);
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        });
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
