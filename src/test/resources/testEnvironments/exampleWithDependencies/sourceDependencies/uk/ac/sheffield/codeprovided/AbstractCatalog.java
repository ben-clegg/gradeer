package uk.ac.sheffield.codeprovided;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCatalog {
    String file;
    List<String> entries;

    public AbstractCatalog(String filename) {
        file = filename;
        entries = readDataFile(filename);
    }

    public List<String> readDataFile(String dataFile) throws IllegalArgumentException {
        List<String> list = new ArrayList<>();
        int count = 1;
        dataFile = dataFile.replaceAll(" ", "");

        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line = br.readLine();
            if (line == null) {
                throw new IllegalArgumentException("File is empty. Provide a valid dataset.");
            }
            while ((line = br.readLine()) != null) {
                try {
                    int id = count;
                    String entry = parseLine(line);
                    list.add(entry);
                    count++;

                } catch (NumberFormatException e) {
                    System.err.println("File format is incorrect; only double values are allowed.");
                } catch (IllegalArgumentException e) {
                    System.err.println("Malformed line: " + line);
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(dataFile + " could not be found. Provide a correct filename." + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public abstract String parseLine(String line) throws IllegalArgumentException;

    public List<String> getEntries() {
        return entries;
    }

}
