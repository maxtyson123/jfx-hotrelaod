package net.maxtyson.reactfx.hotreload.compiler;


import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.nio.file.Path;
import java.util.List;

/**
 * The output of a compile task
 *
 * @param success If the class compiled with no errors
 * @param outputDir Where the bytecode was placed
 * @param diagnostics Errors the occured duing compilation
 */
public record CompileResult(
        boolean success,
        Path outputDir,

        List<Diagnostic<? extends JavaFileObject>> diagnostics
){}