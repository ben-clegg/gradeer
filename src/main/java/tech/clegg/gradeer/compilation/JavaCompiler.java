package tech.clegg.gradeer.compilation;

import tech.clegg.gradeer.configuration.Configuration;
import tech.clegg.gradeer.configuration.Environment;
import tech.clegg.gradeer.results.io.DelayedFileWriter;
import tech.clegg.gradeer.solution.DefaultFlag;
import tech.clegg.gradeer.solution.Solution;
import tech.clegg.gradeer.subject.ClassPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class JavaCompiler
{
    public ClassPath classPath;
    private Configuration configuration;

    public JavaCompiler(ClassPath classPath, Configuration configuration)
    {
        this.classPath = classPath;
        this.configuration = configuration;
    }

    public static JavaCompiler createCompiler(Configuration configuration)
    {
        return createCompiler(configuration, new ArrayList<>());
    }

    public static JavaCompiler createCompiler(Configuration configuration, Collection<Path> auxiliaryClassPathElements)
    {
        ClassPath cp = new ClassPath();
        cp.add(configuration.getSourceDependenciesDir());
        cp.addAll(auxiliaryClassPathElements);

        return new JavaCompiler(cp, configuration);
    }

    /**
     * Compile a Solution
     *
     * @param solutionToCompile the Solution to compile
     * @return true if the Solution compiled (or was already compiled), false if it did not
     */
    public boolean compile(Solution solutionToCompile) {
        if (!configuration.isForceRecompilation()) {
            if (solutionToCompile.isCompiled())
                return true;
        }

        ClassPath cp = new ClassPath(classPath);
        cp.add(solutionToCompile.getDirectory());

        System.out.println("Compiling solution " + solutionToCompile.getIdentifier());

        JavaCompilerResult compilerResult = performCompilation(solutionToCompile.getDirectory(), cp);

        boolean compiled = compilerResult.isCompleted() && !compilerResult.hasError();

        if (!compiled) {
            DelayedFileWriter delayedFileWriter = new DelayedFileWriter();
            delayedFileWriter.addLines(compilerResult.getErrorOutput());

            final Path uncompilableSolutionsDir = Path.of(configuration.getOutputDir().toString(), "uncompilableSolutions");
            uncompilableSolutionsDir.toFile().mkdirs();
            delayedFileWriter.write(Paths.get(uncompilableSolutionsDir + File.separator + solutionToCompile.getIdentifier()));

            // Set flag for Solution
            solutionToCompile.addFlag(DefaultFlag.UNCOMPILABLE);
        }

        return compiled;
    }

    public void compileTests(Solution modelSolution)
    {
        ClassPath cp = new ClassPath(classPath);
        cp.add(modelSolution.getDirectory());
        cp.add(configuration.getTestsDir());
        cp.add(configuration.getSourceDependenciesDir());
        cp.add(configuration.getTestDependenciesDir());

        performCompilation(configuration.getTestsDir(), cp);
    }

    public void compileDir(Path dir, Solution modelSolution) {
        ClassPath cp = new ClassPath(classPath);
        cp.add(dir);
        cp.add(modelSolution.getDirectory());

        performCompilation(dir, cp);
    }

    /**
     * Compile java files in the specified directory
     *
     * @param directory the directory containing the files to compile
     * @param classPath the classpath to use when compiling the java sources
     * @return the result of the compilation
     */
    private JavaCompilerResult performCompilation(Path directory, ClassPath classPath) {
        Set<Path> javaSources = Collections.emptySet();
        try {
            javaSources = getAllSourceFiles(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (javaSources.isEmpty()) {
            return new JavaCompilerResult(new NoSuchElementException("No java sources in directory: " + directory));
        }

        classPath = new ClassPath(classPath);
        classPath.add(configuration.getRuntimeDependenciesDir());
        classPath.add(configuration.getRootDir());
        classPath.add(configuration.getSourceDependenciesDir());
        // TODO auto-load these from a directory (allows for arbitrary junit versions)
        if (configuration.getJUnitVersion().equals(Configuration.JUnitVersion.JUNIT5)) {
            classPath.add(Path.of(Environment.getGradeerHomeDir().toString(), "jars", "junit-jupiter-5.9.2.jar"));
            classPath.add(Path.of(Environment.getGradeerHomeDir().toString(), "jars", "junit-jupiter-api-5.9.2.jar"));
            classPath.add(Path.of(Environment.getGradeerHomeDir().toString(), "jars", "junit-jupiter-engine-5.9.2.jar"));
        } else {
            classPath.add(Path.of(Environment.getGradeerHomeDir().toString(), "jars", "junit-4.13.2.jar"));
            classPath.add(Path.of(Environment.getGradeerHomeDir().toString(), "jars", "hamcrest-all-1.3.jar"));
        }


        List<String> command = new ArrayList<>();
        command.add("javac");
        command.add("--class-path");
        command.add(classPath.toString());
        command.add("--source-path");
        command.add(directory.toString());
        command.add("-d");
        command.add(directory.toString());
        for (Path js : javaSources) {
            command.add(js.toString());
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process p = processBuilder.start();
            return new JavaCompilerResult(p);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to compile classes at " + directory);
            return new JavaCompilerResult(e);
        }
    }

    private Set<Path> getAllSourceFiles(Path directory) throws IOException {
        Set<Path> javaSources = new HashSet<>();
        Files.walkFileTree(directory, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".java")) {
                    javaSources.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return javaSources;
    }
}
