package net.maxtyson.jfxhr.compiler;

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

    public CompileResult compile(Path sourceFile,  Path sourceDir, Path outputDir) {

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


        try{
            fileManager.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new CompileResult(success, outputDir, diagnostics.getDiagnostics());
    }
}
