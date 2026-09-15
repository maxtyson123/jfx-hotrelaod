package net.maxtyson.jfxhr;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import net.maxtyson.jfxhr.compiler.CompileResult;
import net.maxtyson.jfxhr.compiler.DynamicCompiler;
import net.maxtyson.jfxhr.loader.ClassLoader;
import net.maxtyson.jfxhr.swapper.FXSwapper;
import net.maxtyson.jfxhr.watcher.SourceWatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
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
        CompileResult compiled = compiler.compile(sourceFile, config.binDir());

        // Compilation failed
        if(!compiled.success()){
            log.error("Failed to compile '{}' - {}", sourceFile, compiled.diagnostics());
            return;
        }

        // Load
        Class<?> loaded;
        try {
            loaded = ClassLoader.load(compiled.outputDir(), config.rootClassName());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to load '{}' - {}", config.rootClassName(), e.getMessage());
            return;
        }

        // Construct a new instance
        try{
            Object instance = loaded.getDeclaredConstructor().newInstance();
            FXSwapper.swap(mountPoint, (Node) instance);

        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e ) {
            e.printStackTrace();
            log.error("Failed to construct '{}' - {}", config.rootClassName(), e.getMessage());
            return;
        }

        // reflective newInstance
        // UiSwapper.swap
    }

}
