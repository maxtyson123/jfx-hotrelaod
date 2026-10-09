package net.maxtyson.reactfx.hotreload;

import javafx.scene.Node;
import javafx.scene.Parent;
import net.maxtyson.reactfx.hotreload.compiler.CompileResult;
import net.maxtyson.reactfx.hotreload.compiler.DynamicCompiler;
import net.maxtyson.reactfx.hotreload.compiler.FullyQualifiedName;
import net.maxtyson.reactfx.hotreload.loader.SourceLoader;
import net.maxtyson.reactfx.hotreload.swapper.FXSwapper;
import net.maxtyson.reactfx.hotreload.watcher.SourceWatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

import static net.maxtyson.reactfx.hotreload.compiler.FullyQualifiedName.getFQNPart;

public class HotReloader {


    /// HotReloader specific logger
    private static final Logger log = LogManager.getLogger(HotReloader.class);

    private final ReloaderConfig config;

    private final SourceWatcher watcher;
    private final FXSwapper swapper;
    private final DynamicCompiler compiler = new DynamicCompiler();

    public HotReloader(ReloaderConfig config, Parent root) {

        // Listen for source code changes
        watcher = new SourceWatcher(config.watchDir(), p -> this.onSourceChanged(p));
        watcher.start();

        // Construct
        this.config = config;
        swapper = new FXSwapper(root, watcher);
    }

    public static HotReloader attach(ReloaderConfig config, Parent mountPoint) {

        return new HotReloader(config, mountPoint);
    }

    private Class<?> onSourceChanged(Path sourceFile) {
        log.info("Hot reloading file: '{}'", sourceFile);
        String fqn = FullyQualifiedName.fromFile(getFQNPart(config.watchDir(), sourceFile));

        try {

             // Compile
            CompileResult compiled = compiler.compile(sourceFile, fqn, config.watchDir(), config.binDir());

            // Compilation failed
            if(!compiled.success()){
                log.error("Failed to compile '{}' - {}", sourceFile, compiled.diagnostics());
                return null;
            }

            // Load into instantiateable object
            Class<?> loadedClass = SourceLoader.load(compiled.outputDir(), fqn);
            swapper.swapAll(fqn, loadedClass);

            return loadedClass;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to load '{}' {}", fqn, e.getMessage());
            return null;
        }
    }

    public void loadDemo() {

        Class<?> rootClass = onSourceChanged(Path.of("watched-test/net/maxtyson/jfxhr/demo/DemoScene.java"));

        try{
            swapper.setTheRoot((Node)rootClass.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
