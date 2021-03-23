package tech.clegg.gradeer.results.io;

import java.util.Iterator;
import java.util.List;

public class CSVWriter extends DelayedFileWriter
{
    private static final String SEP = ",";

    public CSVWriter(List<String> headerColumns)
    {
        super();
        addEntry(headerColumns);
    }

    public void addEntry(List<String> values)
    {
        if(values.isEmpty())
            return;

        Iterator<String> valuesIter = values.iterator();
        StringBuilder sb = new StringBuilder();

        while (valuesIter.hasNext())
        {
            String value = "\"" + valuesIter.next() + "\"";
            sb.append(value);
            if(valuesIter.hasNext())
                sb.append(SEP);
        }
        addLine(sb.toString());
    }
}
