package tech.clegg.gradeer.subject;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class ClassPath extends HashSet<Path>
{
    public ClassPath(Collection<Path> elements)
    {
        super();
        this.addAll(elements);
    }

    public ClassPath(ClassPath toCopy)
    {
        super();
        this.addAll(toCopy);
    }

    public ClassPath()
    {
        super();
    }

    public String toString()
    {
        this.remove(null); // Do not allow null classpath items
        StringBuilder sb = new StringBuilder();
        Iterator<Path> elemIter = this.iterator();
        while (elemIter.hasNext())
        {
            sb.append(elemIter.next());
            if(elemIter.hasNext())
                sb.append(File.pathSeparator);
        }
        return sb.toString();
    }
}
