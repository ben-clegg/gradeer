package gradeer.configuration;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest
{
    @Test
    public void testGenerateConfiguration()
    {
        try
        {
            File jsonFile = new File(System.getProperty("user.dir") + "/test/resources/testConfig.json");
            System.out.println(jsonFile);
            ConfigurationJSON json = ConfigurationJSON.loadJSON(jsonFile);
            System.out.println(json.rootDirPath);
            System.out.println(json.studentSolutionsDirPath);
        }
        catch (IOException ioEx)
        {
            ioEx.printStackTrace();
            fail();
        }
    }
    @Test
    public void testGenerateConfigurationBadFile()
    {
        try
        {
            ConfigurationJSON.loadJSON(new File("test/resources/notARealFile.json"));
        }
        catch (IOException ioEx)
        {
            return;
        }
        fail();
    }


}