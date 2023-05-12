package uk.ac.sheffield;

import uk.ac.sheffield.codeprovided.AbstractCatalog;

public class Catalog extends AbstractCatalog
{
    public Catalog(String filename) {
        super(filename);
    }

    @Override
    public String parseLine(String line) throws IllegalArgumentException {
        return line.substring(0, line.indexOf(","));
    }
}
