package gradeer.io;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class JavaSource
{
    Path javaFile;
    Path classFile;

    public JavaSource(Path javaSourcePath)
    {
        javaFile = javaSourcePath;
        classFile = Paths.get(javaFile.getParent().toString() + File.separator + getBaseName());
    }

    public boolean isCompiled()
    {
        return Files.exists(classFile);
    }

    public Path getJavaFile()
    {
        return javaFile;
    }

    public Path getClassFile()
    {
        return classFile;
    }

    public String getBaseName()
    {
        return javaFile.getFileName().toString().replace(".java", ".class");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaSource that = (JavaSource) o;
        return Objects.equals(getJavaFile(), that.getJavaFile()) &&
                Objects.equals(getClassFile(), that.getClassFile());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getJavaFile(), getClassFile());
    }
}
