package tech.clegg.gradeer.results.io;

import java.util.Iterator;
import java.util.List;

public class CSVWriter extends FileWriter
{

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
            sb.append(valuesIter.next());
            if(valuesIter.hasNext())
                sb.append(",");
        }
        addLine(sb.toString());
    }
}
