package net.maxtyson.jfxhr.compiler;


import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.nio.file.Path;
import java.util.List;

public record CompileResult(
        boolean success,
        Path outputDir,

        List<Diagnostic<? extends JavaFileObject>> diagnostics
){}