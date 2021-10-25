package tech.clegg.gradeer.configuration;

import tech.clegg.gradeer.GlobalsTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest
{
    @Test
    public void testGenerateConfigurationJSON()
    {
        try
        {
            System.out.println(GlobalsTest.JSON_CONFIG_LIFT);
            ConfigurationJSON json = ConfigurationJSON.loadJSON(GlobalsTest.JSON_CONFIG_LIFT);
            System.out.println(json.rootDirPath);
            System.out.println(json.studentSolutionsDirPath);
            System.out.println(json.modelSolutionsDirPath);
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            fail();
        }
    }
    @Test
    public void testGenerateConfigurationJSONBadFile()
    {
        try
        {
            ConfigurationJSON.loadJSON(Paths.get("test/resources/notARealFile.json"));
        }
        catch (IOException ioEx)
        {
            return;
        }
        fail();
    }


}