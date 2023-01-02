package tech.clegg.gradeer.subject;

import tech.clegg.gradeer.input.SourceFile;
import tech.clegg.gradeer.solution.Solution;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaSource implements SourceFile
{
    Path javaFile;
    private Path rootDir;
    Path classFile;
    String fullPackage;
    String baseName;

    public static Collection<JavaSource> loadAllSourcesInRootDir(Path rootDir)
    {
        Collection<JavaSource> generated = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(rootDir))
        {
            Collection<Path> relevantPaths =
                    walk
                            .filter(p -> p.endsWith(".java"))
                            .collect(Collectors.toList());
            for (Path p : relevantPaths)
            {
                JavaSource js = new JavaSource(p, rootDir);
                generated.add(js);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return generated;
    }

    public JavaSource(Path javaSourcePath, Path rootDir)
    {
        javaFile = javaSourcePath;
        this.rootDir = rootDir;
        classFile = Paths.get(javaFile.getParent().toString() + File.separator + getClassName());
        fullPackage = javaFile.toString().replace(rootDir.toString(), "")
                .split("/" + getBaseName())[0]
                .replaceFirst("/", "")
                .replace("/", ".");
    }

    public JavaSource(JavaSource toCopy, Path newJavaFile, Path newClassFile)
    {
        this.fullPackage = toCopy.getPackage();
        this.baseName = toCopy.getBaseName();
        this.javaFile = newJavaFile;
        this.classFile = newClassFile;
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

    public boolean sharesRelativeIdentifier(JavaSource other)
    {
        if(this.equals(other))
            return true;
        if(this.getBaseName().equals(other.getBaseName()) && this.getPackage().equals(other.getPackage()))
            return true;
        return false;
    }

    public boolean sharesRelativeIdentifier(Collection<JavaSource> others)
    {
        return others.stream().anyMatch(this::sharesRelativeIdentifier);
    }

    public void copyToDifferentSolution(Solution originalOwner, Solution newOwner)
    {
        // TODO implement
        String relativeJavaFile = javaFile.toString().replace(originalOwner.getDirectory().toString(), "");
        String relativeClassFile = classFile.toString().replace(originalOwner.getDirectory().toString(), "");
        Path newJavaFile = Paths.get(newOwner.getDirectory().toString() + relativeJavaFile);
        Path newClassFile = Paths.get(newOwner.getDirectory().toString() + relativeClassFile);

        // Set up new entry
        JavaSource newJavaSource = new JavaSource(this, newJavaFile, newClassFile);

        // Copy .class and .java files to new directory
        try
        {
            Files.createDirectories(newJavaFile.getParent());
            Files.copy(javaFile, newJavaFile);
        }
        catch (FileAlreadyExistsException ignore) {}
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            Files.createDirectories(newClassFile.getParent());
            Files.copy(classFile, newClassFile);
        }
        catch (FileAlreadyExistsException ignore) {}
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Also copy .class files with the same basename plus $xxx (e.g. enums and member classes)
        try
        {
            Files.list(getClassFile().getParent())
                    .filter(f -> com.google.common.io.Files.getFileExtension(f.toString()).equals("class"))
                    .forEach(
                    f ->
                    {
                        String fileBaseName = f.getFileName().toString().replace(".class", "");
                        if(fileBaseName.startsWith(this.getBaseName() + "$"))
                        {
                            // Should copy; will skip if already existing in target
                            Path newFile = Paths.get(newClassFile.getParent().toString() +
                                    "/" + fileBaseName + ".class");
                            try
                            {
                                Files.copy(f, newFile);
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
            );
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // Add new entry to new owner Solution
        newOwner.addSource(newJavaSource);
    }

    public Path getRootDir()
    {
        return rootDir;
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
