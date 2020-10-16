package tech.clegg.gradeer.subject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class JavaSource
{
    Path javaFile;
    Path classFile;
    String fullPackage;
    String baseName;

    public JavaSource(Path javaSourcePath, Path rootDir)
    {
        javaFile = javaSourcePath;
        classFile = Paths.get(javaFile.getParent().toString() + File.separator + getClassName());
        fullPackage = javaFile.toString().replace(rootDir.toString(), "")
                .split("/" + getBaseName())[0]
                .replaceFirst("/", "")
                .replace("/", ".");
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

    public String getClassName()
    {
        return getBaseName() + ".class";
    }

    public String getBaseName()
    {
        if(baseName == null)
            baseName = javaFile.getFileName().toString().replace(".java", "");
        return baseName;
    }

    public String getPackage()
    {
        return fullPackage;
    }

    public String getComplexClassName()
    {
        return getPackage() + "." + getBaseName();
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

    @Override
    public String toString()
    {
        return "JavaSource{" +
                "javaFile=" + javaFile +
                ", classFile=" + classFile +
                '}';
    }
}
