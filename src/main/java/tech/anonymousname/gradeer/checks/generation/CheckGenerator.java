package tech.anonymousname.gradeer.checks.generation;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import tech.anonymousname.gradeer.checks.Check;
import tech.anonymousname.gradeer.configuration.Configuration;
import tech.anonymousname.gradeer.results.io.FileWriter;
import tech.anonymousname.gradeer.solution.Solution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;

public class CheckGenerator
{
    private Configuration configuration;
    private Collection<Solution> modelSolutions;
    private Collection<Check> checks;

    public CheckGenerator(Configuration configuration, Collection<Solution> modelSolutions)
    {
        this.configuration = configuration;
        this.modelSolutions = modelSolutions;

        this.checks = new HashSet<>();
        generate();
    }

    public void generate()
    {
        // Load each element of every
        Collection<JsonElement> jsonElements = new HashSet<>();
        for (Path checksJson : configuration.getCheckJSONs())
        {
            try
            {
                JsonArray jsonArray = JsonParser.parseReader(new JsonReader(new FileReader(String.valueOf(checksJson)))).getAsJsonArray();
                for (JsonElement jsonElement : jsonArray)
                    jsonElements.add(jsonElement);

            } catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        for (JsonElement e : jsonElements)
        {
            try
            {
                // Get target Check class with reflection
                JsonElement typeEntry = e.getAsJsonObject().get("type");
                if (typeEntry == null)
                {
                    System.err.println("No type defined for Check entry " + e.toString());
                    continue;
                }

                Class<Check> clazz = (Class<Check>) Class.forName(Check.class.getPackage().getName() + "." + e.getAsJsonObject().get("type").getAsString());

                Check c = clazz.getConstructor(JsonObject.class, Configuration.class).newInstance(e.getAsJsonObject(), configuration);

                checks.add(c);

            } catch (ClassNotFoundException | NoSuchMethodException ex)
            {
                ex.printStackTrace();
            } catch (IllegalAccessException illegalAccessException)
            {
                illegalAccessException.printStackTrace();
            } catch (InstantiationException instantiationException)
            {
                instantiationException.printStackTrace();
            } catch (InvocationTargetException invocationTargetException)
            {
                invocationTargetException.printStackTrace();
            }
        }

    }

    protected Configuration getConfiguration()
    {
        return configuration;
    }

    protected Collection<Solution> getModelSolutions()
    {
        return modelSolutions;
    }

    protected void reportRemovedChecks(Collection<Check> toRemove, String generatorType)
    {
        FileWriter fileWriter = new FileWriter();
        for (Check c : toRemove)
            fileWriter.addLine(c.toString());

        final Path removedChecksDir = Paths.get(configuration.getOutputDir() + File.separator + "removedChecks");

        if(Files.notExists(removedChecksDir))
            removedChecksDir.toFile().mkdirs();
        fileWriter.write(Paths.get(removedChecksDir + File.separator + generatorType));

    }
    public Collection<Check> getChecks()
    {
        return checks;
    }
}
