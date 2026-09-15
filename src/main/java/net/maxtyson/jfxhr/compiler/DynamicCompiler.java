package net.maxtyson.jfxhr.compiler;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import net.maxtyson.jfxhr.HotReloader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.tools.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class DynamicCompiler {

    /// DynamicCompiler specific logger
    private static final Logger log = LogManager.getLogger(DynamicCompiler.class);

    private JavaCompiler compiler;

    public DynamicCompiler(){

        // Instantiate a copy of the java compiler
        compiler = ToolProvider.getSystemJavaCompiler();

        // Cant compile on a JRE
        if (compiler == null) {
            System.err.println("JDK is required.");
            return;
        }
    }

    public CompileResult compile(Path sourceFile, String fqn, Path sourceDir, Path outputDir) throws IOException {

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        log.info("Compiling '{}' into '{}'", sourceFile, outputDir);

        // Cant compile without a compiler
        if(compiler == null)
            return new CompileResult(false, null, diagnostics.getDiagnostics());

        // Fetch compiler options
        String classpath = System.getProperty("java.class.path");
        String outPath = outputDir.toAbsolutePath().toString();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFile);

        // Load the compilation config on the compiler
        List<String> options = List.of(
            "-classpath", classpath,
            "-sourcepath", sourceDir.toString(),
            "-parameters",
            "-d", outPath
        );
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

        // Compile the class
        boolean success = task.call();
        if(success)
            injectInternalCode(outputDir, fqn);

        fileManager.close();
        return new CompileResult(success, outputDir, diagnostics.getDiagnostics());
    }

    private void injectInternalCode(Path outputDir, String fqn) throws IOException {

        // Load the classpath of all the dynamiclly compiled classes
        ClassFileLocator folderLocator = new ClassFileLocator.ForFolder(outputDir.toFile());

        // Load the rest of the linking that the project uses by default
        ClassFileLocator classLoaderLocator = ClassFileLocator.ForClassLoader.of(Thread.currentThread().getContextClassLoader());

        // Merge the old classes and hotfixed classes
        ClassFileLocator compoundLocator = new ClassFileLocator.Compound(folderLocator, classLoaderLocator);
        TypeDescription type = TypePool.Default.of(compoundLocator).describe(fqn).resolve();

        // Inject bytecode containing metadata needed to help scene injection
        new ByteBuddy()
            .redefine(type, compoundLocator)
            .defineField("__jfxhr_args", Object[].class, Visibility.PUBLIC)
            .defineField("__jfxhr_args_types", Object[].class, Visibility.PUBLIC)
            .visit(Advice.to(ConstructorArgsObserver.class).on(ElementMatchers.isConstructor()))
            .make()
            .saveIn(outputDir.toFile());
    }
}
