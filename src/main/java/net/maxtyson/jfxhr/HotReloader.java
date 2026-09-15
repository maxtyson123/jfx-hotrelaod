package net.maxtyson.jfxhr;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import net.maxtyson.jfxhr.compiler.CompileResult;
import net.maxtyson.jfxhr.compiler.DynamicCompiler;
import net.maxtyson.jfxhr.compiler.FullyQualifiedName;
import net.maxtyson.jfxhr.loader.ClassLoader;
import net.maxtyson.jfxhr.swapper.FXSwapper;
import net.maxtyson.jfxhr.watcher.SourceWatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;

public class HotReloader {


    /// HotReloader specific logger
    private static final Logger log = LogManager.getLogger(HotReloader.class);

    private final ReloaderConfig config;

    private final SourceWatcher watcher;
    private final FXSwapper swapper;
    private final DynamicCompiler compiler = new DynamicCompiler();

    public HotReloader(ReloaderConfig config, Pane root) {

        // Listen for source code changes
        watcher = new SourceWatcher(config.watchDir(), p -> this.onSourceChanged(p));
        watcher.start();

        // Construct
        this.config = config;
        swapper = new FXSwapper(root, watcher);

        // First load
        loadDemo();

    }

    public static HotReloader attach(ReloaderConfig config, Pane mountPoint) {

        return new HotReloader(config, mountPoint);
    }

    private Class<?> onSourceChanged(Path sourceFile) {
        log.info("Hot reloading file: '{}'", sourceFile);

        try {

             // Compile
            String fqn = FullyQualifiedName.fromFile(sourceFile.subpath(1, sourceFile.getNameCount()));
            CompileResult compiled = compiler.compile(sourceFile, fqn, config.watchDir(), config.binDir());

            // Compilation failed
            if(!compiled.success()){
                log.error("Failed to compile '{}' - {}", sourceFile, compiled.diagnostics());
                return null;
            }

            // Load into instantiateable object
            Class<?> loadedClass = ClassLoader.load(compiled.outputDir(), fqn);
            swapper.swapAll(fqn, loadedClass);

            return loadedClass;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to load ' {}", e.getMessage());
            return null;
        }
    }

    private void loadDemo() {

        Class<?> rootClass = onSourceChanged(Path.of("watched-test/net/maxtyson/jfxhr/demo/DemoScene.java"));

        try{
            swapper.setInRoot((Node)rootClass.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
