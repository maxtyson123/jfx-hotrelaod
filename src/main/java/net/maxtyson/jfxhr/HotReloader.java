package net.maxtyson.jfxhr;

import javafx.scene.layout.Pane;
import net.maxtyson.jfxhr.watcher.SourceWatcher;

public class HotReloader {

    private final ReloaderConfig config;
    private final Pane mountPoint;

    private final SourceWatcher watcher;

    public HotReloader(ReloaderConfig config, Pane mountPoint) {

        // Construct
        this.config = config;
        this.mountPoint = mountPoint;

        // First load
        onSourceChanged();

        // Listen for source code changes
        watcher = new SourceWatcher(config.watchDir(), () -> this.onSourceChanged());
        watcher.start();
    }

    public static HotReloader attach(ReloaderConfig config, Pane mountPoint) {

        return new HotReloader(config, mountPoint);
    }

    private void onSourceChanged() {
        // compile
        // load
        // reflective newInstance
        // UiSwapper.swap

        // on compile failure log
    }

}
