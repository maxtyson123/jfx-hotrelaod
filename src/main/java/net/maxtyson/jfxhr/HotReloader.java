package net.maxtyson.jfxhr;

import javafx.scene.layout.Pane;
import net.maxtyson.jfxhr.compiler.CompileResult;
import net.maxtyson.jfxhr.compiler.DynamicCompiler;
import net.maxtyson.jfxhr.watcher.SourceWatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public class HotReloader {


    /// HotReloader specific logger
    private static final Logger log = LogManager.getLogger(HotReloader.class);

    private final ReloaderConfig config;
    private final Pane mountPoint;

    private final SourceWatcher watcher;
    private final DynamicCompiler compiler = new DynamicCompiler();

    public HotReloader(ReloaderConfig config, Pane mountPoint) {

        // Construct
        this.config = config;
        this.mountPoint = mountPoint;

        // First load
        onSourceChanged(Path.of("watched-test/net/maxtyson/jfxhr/demo/DemoButton.java"));

        // Listen for source code changes
        watcher = new SourceWatcher(config.watchDir(), p -> this.onSourceChanged(p));
        watcher.start();
    }

    public static HotReloader attach(ReloaderConfig config, Pane mountPoint) {

        return new HotReloader(config, mountPoint);
    }

    private void onSourceChanged(Path sourceFile) {
        log.info("Hot reloading file: '{}'", sourceFile);

        // Compile
        CompileResult compiled = compiler.compile(sourceFile, Path.of(config.watchDir() + "/bin/"));

        // Compilation failed
        if(!compiled.success()){
            log.error("Failed to compile '{}' - {}", sourceFile, compiled.diagnostics());
            return;
        }

        // load
        // reflective newInstance
        // UiSwapper.swap

        // on compile failure log
    }

}
