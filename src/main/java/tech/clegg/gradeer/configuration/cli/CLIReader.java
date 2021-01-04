package tech.clegg.gradeer.configuration.cli;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.cli.*;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class CLIReader
{
    private CommandLine cli;
    private Options options;

    private void initOptions()
    {
        options = new Options();

        options.addOption(Option.builder("h")
                .longOpt(CLIOptions.HELP)
                .desc("Display this help message.")
                .build());

        options.addOption(Option.builder("c")
                .longOpt(CLIOptions.CONFIGURATION_LOCATION)
                .hasArg()
                .desc("(Required) Path to the configuration JSON file.")
                .build());

        options.addOption(Option.builder("m")
                .longOpt(CLIOptions.MUTANT_SOLUTIONS)
                .hasArg()
                .desc("Path to directory containing mutant solutions to evaluate check performance.")
                .build());

        options.addOption(Option.builder("i")
                .longOpt(CLIOptions.INCLUDE_SOLUTIONS)
                .hasArg()
                .desc("Solutions to exclusively include in processing, as a JSON array; " +
                        "e.g. [\"solution1\",\"solution2\"] (avoid spaces)")
                .build());

        options.addOption(Option.builder("e")
                .longOpt(CLIOptions.EXCLUDE_SOLUTIONS)
                .hasArg()
                .desc("Solutions to skip when processing, as a JSON array; " +
                        "e.g. [\"solution1\",\"solution2\"] (avoid spaces)")
                .build());
    }

    public CLIReader(String[] args)
    {
        initOptions();

        CommandLineParser commandLineParser = new DefaultParser();

        try
        {
            cli = commandLineParser.parse(options, args);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            System.err.println("Command line parsing error, check configuration.");
            System.exit(1);
        }
    }

    public boolean hasOption(String optStr)
    {
        return cli.hasOption(optStr);
    }

    public String getInputValue(String optStr) throws IllegalArgumentException
    {
        if(cli.hasOption(optStr))
        {
            return cli.getOptionValue(optStr);
        }
        Option o = options.getOption(optStr);
        StringBuilder msg = new StringBuilder();
        msg.append("Option not set: (-" + o.getOpt());

        if(o.hasLongOpt())
            msg.append(" / --" + o.getLongOpt());
        msg.append(") - " + o.getDescription());

        throw new IllegalArgumentException(msg.toString());
    }

    public String getInputValueOrEmpty(String optStr)
    {
        if(cli.hasOption(optStr))
            return cli.getOptionValue(optStr);
        return "";
    }

    public Collection<String> getArrayInputOrEmpty(String optStr)
    {
        String arr = getInputValueOrEmpty(optStr);
        if(arr.isEmpty())
            return Collections.emptySet();

        try
        {
            return Arrays.asList(new Gson().fromJson(arr, String[].class));
        } catch (JsonSyntaxException e)
        {
            e.printStackTrace();
            System.err.println("Could not parse array \"" + arr + "\" ; perhaps it is malformed or contains spaces?");
            return Collections.emptySet();
        }
    }

    public void printHelp()
    {
        HelpFormatter formatter = new HelpFormatter();

        final PrintWriter writer = new PrintWriter(System.out);
        formatter.printUsage(writer,80,"Gradeer", options);
        formatter.printOptions(writer, 80, options, 2, 8);
        writer.flush();
    }
}
